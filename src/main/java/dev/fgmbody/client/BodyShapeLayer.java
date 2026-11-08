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
 *  - glutes (optionally round, optionally with physics) and hips: extra geometry on top of the vanilla torso;
 *  - waist: the vanilla torso/jacket is hidden by TorsoEvents and redrawn here by TorsoMesh.
 * Independent from FGM's GenderLayer (separate layer, separate data).
 */
public class BodyShapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // Skin rects {u0, v0, u1, v1}. Back texture u 32..40: x=+4 -> u32, x=-4 -> u40. Rows 28..32 = lower back.
    // Left cheek is x<0 (player's right), right cheek is x>0.
    private static final int[] BACK_L = {36, 28, 40, 32};
    private static final int[] BACK_R = {32, 28, 36, 32};
    private static final int[] EDGE_L = {39, 28, 40, 32};
    private static final int[] EDGE_R = {32, 28, 33, 32};
    private static final int[] TOP_L  = {36, 28, 40, 29};
    private static final int[] TOP_R  = {32, 28, 36, 29};
    private static final int[] BOT_L  = {36, 31, 40, 32};
    private static final int[] BOT_R  = {32, 31, 36, 32};
    // hips (side flare)
    private static final int[] SIDE_POS  = {28, 26, 32, 32};
    private static final int[] SIDE_NEG  = {16, 26, 20, 32};
    private static final int[] FRONT_POS = {27, 26, 28, 32};
    private static final int[] FRONT_NEG = {20, 26, 21, 32};
    private static final int[] BACK_POS  = {32, 26, 33, 32};
    private static final int[] BACK_NEG  = {39, 26, 40, 32};
    private static final int[] HIP_TOP_POS = {28, 26, 32, 27};
    private static final int[] HIP_TOP_NEG = {16, 26, 20, 27};

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

            float waist = s.waist();
            if (waist >= 0.02f) {
                TorsoMesh.draw(ps, vc, light, overlay, alpha, waist, s.waistRound(),
                        player.isModelPartShown(PlayerModelPart.JACKET));
            }

            if (s.hipsWidth() >= 0.02f) {
                float ext = s.hipsWidth() * 2f;            // up to 2 px per side
                // with a narrow waist the flare starts lower and reaches slightly into the torso
                float yTop = 6f + 2.5f * waist, inner = 4f - 0.75f * waist;
                float zf = -2.01f, zb = 2.01f;
                SkinBox.draw(ps, vc, light, overlay, alpha, inner, yTop, zf, 4f + ext, 12f, zb,
                        BACK_POS, FRONT_POS, null, SIDE_POS, HIP_TOP_POS, null);
                SkinBox.draw(ps, vc, light, overlay, alpha, -4f - ext, yTop, zf, -inner, 12f, zb,
                        BACK_NEG, FRONT_NEG, SIDE_NEG, null, HIP_TOP_NEG, null);
            }

            if (s.glutesSize() >= 0.02f) {
                float size = s.glutesSize();
                float phys = s.glutesPhysics() >= 0.02f
                        ? GlutePhysics.get(player.getUUID()).update(player, partialTick, ageInTicks,
                                limbSwing, limbSwingAmount, s.glutesPhysics())
                        : 0f;
                float depth = Math.max(0.5f, size * 3f * (1f + 0.05f * phys)); // protrusion behind the torso
                float w = 4f + size * 0.5f;
                float top = 8f - s.glutesHeight() * 1.5f + phys;
                float bottom = top + 4f + size * 1.5f;
                float round = s.glutesRound() >= 0.02f ? s.glutesRound() : 0f;
                int seg = round > 0f ? 6 : 1;
                // centred on the torso back plane (z = 2): the inner half hides inside the torso
                float z0 = 2f - depth, z1 = 2f + depth;
                SkinBox.drawRounded(ps, vc, light, overlay, alpha, -w, top, z0, 0f, bottom, z1,
                        new int[][]{BACK_L, null, EDGE_L, null, TOP_L, BOT_L}, round, seg);
                SkinBox.drawRounded(ps, vc, light, overlay, alpha, 0f, top, z0, w, bottom, z1,
                        new int[][]{BACK_R, null, null, EDGE_R, TOP_R, BOT_R}, round, seg);
            }
            ps.popPose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
