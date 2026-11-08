package dev.fgmbody;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/** Per-player shape. All values are normalized. */
public record BodyShape(float glutesSize, float glutesHeight, float glutesRound, float glutesPhysics,
                        float hipsWidth, float waist, float waistRound) {
    public static final BodyShape DEFAULT = new BodyShape(0f, 0f, 0f, 0f, 0f, 0f, 0f);

    public BodyShape clamped() {
        return new BodyShape(
                Mth.clamp(glutesSize, 0f, 1f),
                Mth.clamp(glutesHeight, -1f, 1f),
                Mth.clamp(glutesRound, 0f, 1f),
                Mth.clamp(glutesPhysics, 0f, 1f),
                Mth.clamp(hipsWidth, 0f, 1f),
                Mth.clamp(waist, 0f, 1f),
                Mth.clamp(waistRound, 0f, 1f));
    }

    public boolean isDefault() {
        return glutesSize < 0.02f && hipsWidth < 0.02f && waist < 0.02f;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(glutesSize);
        buf.writeFloat(glutesHeight);
        buf.writeFloat(glutesRound);
        buf.writeFloat(glutesPhysics);
        buf.writeFloat(hipsWidth);
        buf.writeFloat(waist);
        buf.writeFloat(waistRound);
    }

    public static BodyShape read(FriendlyByteBuf buf) {
        return new BodyShape(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), buf.readFloat()).clamped();
    }
}
