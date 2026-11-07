package dev.fgmbody.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.fgmbody.BodyShape;
import dev.fgmbody.BodyShapeStore;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

/**
 * Extra body geometry textured from the player's skin:
 *  - glutes and hips: extra boxes on top of the vanilla torso;
 *  - waist: the vanilla torso (and jacket) is hidden by TorsoEvents and redrawn here as 5 slices,
 *    the middle ones narrower.
 * Independent from FGM's GenderLayer (separate layer, separate data).
 */
public class BodyShapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // glutes: skin rects {u0, v0, u1, v1}; torso back = u 32..40, rows 20..32
    private static final int[] BACK_L = {32, 28, 36, 32};
    private static final int[] BACK_R = {36, 28, 40, 32};
    private static final int[] EDGE_L = {32, 28, 33, 32};
    private static final int[] EDGE_R = {39, 28, 40, 32};
    private static final int[] TOP_L  = {32, 28, 36, 29};
    private static final int[] TOP_R  = {36, 28, 40, 29};
    private static final int[] BOT_L  = {32, 31, 36, 32};
    private static final int[] BOT_R  = {36, 31, 40, 32};
    // hips (side flare). Front face: u increases toward -x, so +x edge = u 20, -x edge = u 27..28
    private static final int[] SIDE_POS  = {28, 26, 32, 32};
    private static final int[] SIDE_NEG  = {16, 26, 20, 32};
    private static final int[] FRONT_POS = {20, 26, 21, 32};
    private static final int[] FRONT_NEG = {27, 26, 28, 32};
    private static final int[] BACK_POS  = {39, 26, 40, 32};
    private static final int[] BACK_NEG  = {32, 26, 33, 32};
    private static final int[] HIP_TOP_POS = {28, 26, 32, 27};
    private static final int[] HIP_TOP_NEG = {16, 26, 20, 27};

    // waist torso slices: {yTop, yBottom, narrowing factor}; integer rows so UVs stay on pixel borders
    private static final float[][] SLICES = {
            {0f, 5f, 0f}, {5f, 6f, 0.5f}, {6f, 8f, 1f}, {8f, 9f, 0.5f}, {9f, 12f, 0f}};

    public BodyShapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    /** Shared by the layer and TorsoEvents so the vanilla torso is hidden exactly when we redraw it. */
    static boolean replacesTorso(AbstractClientPlayer player) {
        BodyShape s = BodyShapeStore.CLIENT.get(player.getUUID());
        return s != null && s.waist() >= 0.02f && isVisible(player);
    }

    private static boolean isVisible(AbstractClientPlayer player) {
        if (player.isSpectator()) return false;
        Player viewer = Minecraft.getInstance().player;
        return viewer == null || !player.isInvisibleTo(viewer);
    }

    @Override
    public void render(@Nonnull PoseStack ps, @Nonnull MultiBufferSource buf, int light,
                       @Nonnull AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!isVisible(player)) return;
        BodyShape s = BodyShapeStore.CLIENT.get(player.getUUID());
        if (s == null || s.isDefault()) return;

        try {
            VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent(getTextureLocation(player)));
            int overlay = LivingEntityRenderer.getOverlayCoords(player, 0);
            float alpha = player.isInvisible() ? 0.15f : 1f;

            ps.pushPose();
            getParentModel().body.translateAndRotate(ps); // follows sneaking / body rotation
            ps.scale(1f / 16f, 1f / 16f, 1f / 16f);       // from here on: model pixels

            if (s.waist() >= 0.02f) {
                drawTorso(ps, vc, light, overlay, alpha, s.waist(), player.isModelPartShown(PlayerModelPart.JACKET));
            }

            if (s.hipsWidth() >= 0.02f) {
                float ext = s.hipsWidth() * 2f;            // up to 2 px per side
                float yTop = 6f, yBot = 12f, zf = -2.01f, zb = 2.01f;
                SkinBox.draw(ps, vc, light, overlay, alpha, 4f, yTop, zf, 4f + ext, yBot, zb,
                        BACK_POS, FRONT_POS, null, SIDE_POS, HIP_TOP_POS, null);
                SkinBox.draw(ps, vc, light, overlay, alpha, -4f - ext, yTop, zf, -4f, yBot, zb,
                        BACK_NEG, FRONT_NEG, SIDE_NEG, null, HIP_TOP_NEG, null);
            }

            if (s.glutesSize() >= 0.02f) {
                float size = s.glutesSize();
                float depth = size * 3f;                   // up to 3 px behind the torso
                float w = 4f + size * 0.5f;
                float top = 8f - s.glutesHeight() * 1.5f;
                float bottom = top + 4f + size * 1.5f;
                float z0 = 2.0f, z1 = 2.0f + depth;
                SkinBox.draw(ps, vc, light, overlay, alpha, -w, top, z0, 0f, bottom, z1,
                        BACK_L, null, EDGE_L, null, TOP_L, BOT_L);
                SkinBox.draw(ps, vc, light, overlay, alpha, 0f, top, z0, w, bottom, z1,
                        BACK_R, null, null, EDGE_R, TOP_R, BOT_R);
            }
            ps.popPose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Vanilla torso is 8 x 12 x 4 px, origin at top centre, Y down, front = -Z. */
    private static void drawTorso(PoseStack ps, VertexConsumer vc, int light, int overlay, float alpha,
                                  float waist, boolean jacket) {
        for (int i = 0; i < SLICES.length; i++) {
            int y0 = (int) SLICES[i][0], y1 = (int) SLICES[i][1];
            float k = SLICES[i][2];
            float ix = waist * 1.5f * k;   // up to 1.5 px per side
            float iz = waist * 0.6f * k;   // up to 0.6 px front and back
            boolean first = i == 0, last = i == SLICES.length - 1;

            SkinBox.draw(ps, vc, light, overlay, alpha,
                    -4f + ix, y0, -2f + iz, 4f - ix, y1, 2f - iz,
                    new int[]{32, 20 + y0, 40, 20 + y1},   // back
                    new int[]{20, 20 + y0, 28, 20 + y1},   // front
                    new int[]{16, 20 + y0, 20, 20 + y1},   // -x
                    new int[]{28, 20 + y0, 32, 20 + y1},   // +x
                    first ? new int[]{20, 16, 28, 20} : new int[]{20, 20 + y0, 28, 21 + y0},
                    last  ? new int[]{28, 16, 36, 20} : new int[]{20, 19 + y1, 28, 20 + y1});

            if (jacket) {
                float g = 0.25f;
                SkinBox.draw(ps, vc, light, overlay, alpha,
                        -4f + ix - g, first ? y0 - g : y0, -2f + iz - g,
                        4f - ix + g, last ? y1 + g : y1, 2f - iz + g,
                        new int[]{32, 36 + y0, 40, 36 + y1},
                        new int[]{20, 36 + y0, 28, 36 + y1},
                        new int[]{16, 36 + y0, 20, 36 + y1},
                        new int[]{28, 36 + y0, 32, 36 + y1},
                        first ? new int[]{20, 32, 28, 36} : new int[]{20, 36 + y0, 28, 37 + y0},
                        last  ? new int[]{28, 32, 36, 36} : new int[]{20, 35 + y1, 28, 36 + y1});
            }
        }
    }
}
