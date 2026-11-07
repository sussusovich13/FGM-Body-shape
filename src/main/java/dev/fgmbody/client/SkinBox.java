package dev.fgmbody.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Minimal textured box that samples explicit pixel rectangles of the 64x64 skin.
 * Coordinates are in model pixels (the caller scales the PoseStack by 1/16).
 * UV rect = {u0, v0, u1, v1} in skin pixels; null = face is not drawn.
 */
final class SkinBox {
    private SkinBox() {}

    static void draw(PoseStack ps, VertexConsumer vc, int light, int overlay, float alpha,
                     float x0, float y0, float z0, float x1, float y1, float z1,
                     int[] back, int[] front, int[] left, int[] right, int[] top, int[] bottom) {
        PoseStack.Pose p = ps.last();
        if (back != null)   quad(p, vc, light, overlay, alpha, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1,  0, 0, 1, back);
        if (front != null)  quad(p, vc, light, overlay, alpha, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0,  0, 0,-1, front);
        if (right != null)  quad(p, vc, light, overlay, alpha, x1,y0,z0, x1,y0,z1, x1,y1,z1, x1,y1,z0,  1, 0, 0, right);
        if (left != null)   quad(p, vc, light, overlay, alpha, x0,y0,z1, x0,y0,z0, x0,y1,z0, x0,y1,z1, -1, 0, 0, left);
        if (top != null)    quad(p, vc, light, overlay, alpha, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1,  0,-1, 0, top);   // model Y points down
        if (bottom != null) quad(p, vc, light, overlay, alpha, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0,  0, 1, 0, bottom);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float nx, float ny, float nz, int[] uv) {
        float u0 = uv[0] / 64f, v0 = uv[1] / 64f, u1 = uv[2] / 64f, v1 = uv[3] / 64f;
        vert(pose, vc, light, overlay, alpha, ax, ay, az, u0, v0, nx, ny, nz);
        vert(pose, vc, light, overlay, alpha, bx, by, bz, u1, v0, nx, ny, nz);
        vert(pose, vc, light, overlay, alpha, cx, cy, cz, u1, v1, nx, ny, nz);
        vert(pose, vc, light, overlay, alpha, dx, dy, dz, u0, v1, nx, ny, nz);
    }

    private static void vert(PoseStack.Pose pose, VertexConsumer vc, int light, int overlay, float alpha,
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
