package dev.fgmbody.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;

/**
 * Replacement torso (8 x 12 x 4 px) made of 12 one-pixel-high rings. The waist narrows the middle rings.
 * round = 0: blocky stepped waist, rectangular cross-section (like before).
 * round = 1: smooth hourglass profile and an elliptical cross-section.
 */
final class TorsoMesh {
    private static final float[] STEP = {0, 0, 0, 0, 0, 0.5f, 1, 1, 0.5f, 0, 0, 0};
    // front, +X side, back, -X side: {startCx, dCx, startCz, dCz, startU, dU, segments, nx, nz}
    private static final float[][] SIDES = {
            {-1, 2, -1, 0, 20, 8, 4, 0, -1},
            { 1, 0, -1, 2, 28, 4, 2, 1, 0},
            { 1, -2, 1, 0, 32, 8, 4, 0, 1},
            {-1, 0, 1, -2, 16, 4, 2, -1, 0}};

    private TorsoMesh() {}

    static void draw(PoseStack ps, VertexConsumer vc, int light, int overlay, float alpha,
                     float waist, float round, boolean jacket) {
        layer(ps, vc, light, overlay, alpha, waist, round, 0f, 20, 16);
        if (jacket) layer(ps, vc, light, overlay, alpha, waist, round, 0.25f, 36, 32);
    }

    private static float smooth(float y) {
        float d = (y - 7f) / 4f;
        return Math.abs(d) >= 1f ? 0f : 0.5f * (1f + (float) Math.cos(Math.PI * d));
    }

    private static void layer(PoseStack ps, VertexConsumer vc, int light, int overlay, float alpha,
                              float waist, float round, float g, int vBody, int vCap) {
        PoseStack.Pose pose = ps.last();
        for (int i = 0; i < 12; i++) {
            float kT = Mth.lerp(round, STEP[i], smooth(i));
            float kB = Mth.lerp(round, STEP[i], smooth(i + 1));
            float hxT = 4f - waist * 1.5f * kT + g, hzT = 2f - waist * 0.6f * kT + g;
            float hxB = 4f - waist * 1.5f * kB + g, hzB = 2f - waist * 0.6f * kB + g;
            float yT = i == 0 ? -g : i;
            float yB = i == 11 ? 12f + g : i + 1;
            float vT = vBody + i, vB = vBody + i + 1;

            for (float[] sd : SIDES) {
                int n = (int) sd[6];
                for (int k = 0; k < n; k++) {
                    float f0 = (float) k / n, f1 = (float) (k + 1) / n;
                    float ax = sd[0] + sd[1] * f0, az = sd[2] + sd[3] * f0;
                    float bx = sd[0] + sd[1] * f1, bz = sd[2] + sd[3] * f1;
                    float u0 = sd[4] + sd[5] * f0, u1 = sd[4] + sd[5] * f1;
                    ring(pose, vc, light, overlay, alpha, ax, az, hxT, hzT, yT, u0, vT, round, sd[7], sd[8]);
                    ring(pose, vc, light, overlay, alpha, bx, bz, hxT, hzT, yT, u1, vT, round, sd[7], sd[8]);
                    ring(pose, vc, light, overlay, alpha, bx, bz, hxB, hzB, yB, u1, vB, round, sd[7], sd[8]);
                    ring(pose, vc, light, overlay, alpha, ax, az, hxB, hzB, yB, u0, vB, round, sd[7], sd[8]);
                }
            }
            if (i == 0) cap(pose, vc, light, overlay, alpha, hxT, hzT, yT, round, true, vCap);
            if (i == 11) cap(pose, vc, light, overlay, alpha, hxB, hzB, yB, round, false, vCap);
        }
    }

    private static void ring(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
                             float cx, float cz, float hx, float hz, float y, float u, float v,
                             float round, float nsx, float nsz) {
        float l = (float) Math.sqrt(cx * cx + cz * cz);
        float qx = cx + (cx / l - cx) * round, qz = cz + (cz / l - cz) * round;
        float rx = qx / hx, rz = qz / hz;
        float rl = (float) Math.sqrt(rx * rx + rz * rz);
        float nx = (1f - round) * nsx + round * rx / rl;
        float nz = (1f - round) * nsz + round * rz / rl;
        float nl = (float) Math.sqrt(nx * nx + nz * nz);
        SkinBox.vert(pose, vc, light, overlay, alpha, hx * qx, y, hz * qz, u / 64f, v / 64f, nx / nl, 0f, nz / nl);
    }

    private static void cap(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
                            float hx, float hz, float y, float round, boolean top, int vCap) {
        float ny = top ? -1f : 1f;
        for (float[] sd : SIDES) {
            int n = (int) sd[6];
            for (int k = 0; k < n; k++) {
                float f0 = (float) k / n, f1 = (float) (k + 1) / n;
                float ax = sd[0] + sd[1] * f0, az = sd[2] + sd[3] * f0;
                float bx = sd[0] + sd[1] * f1, bz = sd[2] + sd[3] * f1;
                capVert(pose, vc, light, overlay, alpha, ax, az, hx, hz, y, round, top, vCap, ny);
                capVert(pose, vc, light, overlay, alpha, bx, bz, hx, hz, y, round, top, vCap, ny);
                capVert(pose, vc, light, overlay, alpha, 0f, 0f, hx, hz, y, 0f, top, vCap, ny);
                capVert(pose, vc, light, overlay, alpha, 0f, 0f, hx, hz, y, 0f, top, vCap, ny);
            }
        }
    }

    private static void capVert(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
                                float cx, float cz, float hx, float hz, float y, float round,
                                boolean top, int vCap, float ny) {
        float l = (float) Math.sqrt(cx * cx + cz * cz);
        float qx = l > 0f ? cx + (cx / l - cx) * round : 0f;
        float qz = l > 0f ? cz + (cz / l - cz) * round : 0f;
        float u = top ? 20f + (cx + 1f) * 4f : 28f + (cx + 1f) * 4f;
        float v = top ? vCap + (1f - cz) * 2f : vCap + (cz + 1f) * 2f;
        SkinBox.vert(pose, vc, light, overlay, alpha, hx * qx, y, hz * qz, u / 64f, v / 64f, 0f, ny, 0f);
    }
}
