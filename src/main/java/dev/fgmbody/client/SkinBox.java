package dev.fgmbody.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Box (optionally rounded into an ellipsoid) textured from explicit pixel rectangles of the 64x64 skin.
 * Coordinates are model pixels (caller scales the PoseStack by 1/16). Model Y points down, front = -Z.
 * UV rect = {u0, v0, u1, v1}; null = face not drawn.
 *
 * Skin orientation (standard layout [right 16-20][front 20-28][left 28-32][back 32-40]):
 * front u grows toward +x, back u grows toward -x, +x side u grows toward +z, -x side u grows toward -z.
 */
final class SkinBox {
    static final int BACK = 0, FRONT = 1, LEFT = 2, RIGHT = 3, TOP = 4, BOTTOM = 5;
    private static final float[][] NORMAL = {{0, 0, 1}, {0, 0, -1}, {-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}};

    private SkinBox() {}

    static void draw(PoseStack ps, VertexConsumer vc, int light, int overlay, float alpha,
                     float x0, float y0, float z0, float x1, float y1, float z1,
                     int[] back, int[] front, int[] left, int[] right, int[] top, int[] bottom) {
        drawRounded(ps, vc, light, overlay, alpha, x0, y0, z0, x1, y1, z1,
                new int[][]{back, front, left, right, top, bottom}, 0f, 1);
    }

    static void drawRounded(PoseStack ps, VertexConsumer vc, int light, int overlay, float alpha,
                            float x0, float y0, float z0, float x1, float y1, float z1,
                            int[][] uv, float round, int seg) {
        PoseStack.Pose pose = ps.last();
        float cx = (x0 + x1) * 0.5f, cy = (y0 + y1) * 0.5f, cz = (z0 + z1) * 0.5f;
        float hx = Math.max(0.01f, (x1 - x0) * 0.5f);
        float hy = Math.max(0.01f, (y1 - y0) * 0.5f);
        float hz = Math.max(0.01f, (z1 - z0) * 0.5f);
        for (int f = 0; f < 6; f++) {
            int[] r = uv[f];
            if (r == null) continue;
            for (int i = 0; i < seg; i++) {
                float s0 = -1f + 2f * i / seg, s1 = -1f + 2f * (i + 1) / seg;
                for (int j = 0; j < seg; j++) {
                    float t0 = -1f + 2f * j / seg, t1 = -1f + 2f * (j + 1) / seg;
                    emit(pose, vc, light, overlay, alpha, f, s0, t0, r, cx, cy, cz, hx, hy, hz, round);
                    emit(pose, vc, light, overlay, alpha, f, s1, t0, r, cx, cy, cz, hx, hy, hz, round);
                    emit(pose, vc, light, overlay, alpha, f, s1, t1, r, cx, cy, cz, hx, hy, hz, round);
                    emit(pose, vc, light, overlay, alpha, f, s0, t1, r, cx, cy, cz, hx, hy, hz, round);
                }
            }
        }
    }

    private static void emit(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
                             int f, float s, float t, int[] r,
                             float cx, float cy, float cz, float hx, float hy, float hz, float round) {
        float px, py, pz;
        switch (f) {
            case BACK   -> { px = -s; py = t;  pz = 1; }
            case FRONT  -> { px = s;  py = t;  pz = -1; }
            case LEFT   -> { px = -1; py = t;  pz = -s; }
            case RIGHT  -> { px = 1;  py = t;  pz = s; }
            case TOP    -> { px = s;  py = -1; pz = -t; }
            default     -> { px = s;  py = 1;  pz = -t; }
        }
        float nx = NORMAL[f][0], ny = NORMAL[f][1], nz = NORMAL[f][2];
        if (round > 0f) {
            float len = (float) Math.sqrt(px * px + py * py + pz * pz);
            float sx = px / len, sy = py / len, sz = pz / len;
            px += (sx - px) * round;
            py += (sy - py) * round;
            pz += (sz - pz) * round;
            float ex = sx / hx, ey = sy / hy, ez = sz / hz;
            float el = (float) Math.sqrt(ex * ex + ey * ey + ez * ez);
            nx = nx * (1f - round) + ex / el * round;
            ny = ny * (1f - round) + ey / el * round;
            nz = nz * (1f - round) + ez / el * round;
            float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= nl; ny /= nl; nz /= nl;
        }
        float u = (r[0] + (r[2] - r[0]) * (s + 1f) * 0.5f) / 64f;
        float v = (r[1] + (r[3] - r[1]) * (t + 1f) * 0.5f) / 64f;
        vert(pose, vc, light, overlay, alpha, cx + hx * px, cy + hy * py, cz + hz * pz, u, v, nx, ny, nz);
    }

    static void vert(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
                     float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        vc.vertex(pose.pose(), x, y, z)
          .color(1f, 1f, 1f, alpha)
          .uv(u, v)
          .overlayCoords(overlay)
          .uv2(light)
          .normal(pose.normal(), nx, ny, nz)
          .endVertex();
    }
}
