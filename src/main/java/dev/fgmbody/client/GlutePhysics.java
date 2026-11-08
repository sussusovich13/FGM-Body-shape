package dev.fgmbody.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

/**
 * Damped spring for the glutes (vertical wobble), computed client-side per viewed player.
 * Inputs: the player's vertical acceleration (jumps, landings, falling) and the walking/running stride.
 * Only the strength slider is synced; the motion itself is derived from what each client sees.
 */
final class GlutePhysics {
    private static final Map<UUID, GlutePhysics> STATES = new HashMap<>();

    private static final float K = 0.45f;     // spring stiffness
    private static final float C = 0.25f;     // damping
    private static final float G = 3.0f;      // reaction to vertical acceleration
    private static final float STRIDE = 0.9f; // px of stride-driven bounce at full speed

    private float y, vy;
    private float lastAge = Float.NaN, lastY, lastVin;

    static GlutePhysics get(UUID id) {
        return STATES.computeIfAbsent(id, k -> new GlutePhysics());
    }

    static void clear() {
        STATES.clear();
    }

    /** @return vertical offset in model pixels (positive = down) */
    float update(AbstractClientPlayer p, float partial, float age, float limbSwing, float limbAmount, float strength) {
        float posY = Mth.lerp(partial, (float) p.yo, (float) p.getY());
        float dt = age - lastAge;
        if (Float.isNaN(lastAge) || dt < 0f || dt > 4f) {
            lastAge = age; lastY = posY; lastVin = 0f; y = 0f; vy = 0f;
            return 0f;
        }
        if (dt < 0.0001f) return clampOut(strength);

        float vin = (posY - lastY) / dt;
        float acc = Mth.clamp((vin - lastVin) / dt, -1.0f, 1.0f);
        lastAge = age; lastY = posY; lastVin = vin;

        float drive = limbAmount > 0.05f ? STRIDE * limbAmount * (float) Math.cos(limbSwing * 1.3324f) : 0f;
        int steps = Math.max(1, (int) Math.ceil(dt / 0.5f));
        float h = dt / steps;
        for (int i = 0; i < steps; i++) {
            float force = K * (drive - y) - C * vy + G * acc;
            vy += force * h;
            y += vy * h;
        }
        y = Mth.clamp(y, -4f, 4f);
        return clampOut(strength);
    }

    private float clampOut(float strength) {
        return Mth.clamp(y * strength * 1.5f, -3f, 3f);
    }
}
