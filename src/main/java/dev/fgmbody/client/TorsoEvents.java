package dev.fgmbody.client;

import dev.fgmbody.FgmBodyMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * PlayerRenderer#render calls setModelProperties (which makes every part visible) before
 * RenderLivingEvent.Pre fires, so hiding the torso here sticks for this render only.
 */
@Mod.EventBusSubscriber(modid = FgmBodyMod.MODID, value = Dist.CLIENT)
public final class TorsoEvents {
    private static boolean hidden;

    @SubscribeEvent
    public static void pre(RenderLivingEvent.Pre<?, ?> e) {
        if (!(e.getEntity() instanceof AbstractClientPlayer p) || !(e.getRenderer() instanceof PlayerRenderer pr)) return;
        if (!BodyShapeLayer.replacesTorso(p)) return;
        PlayerModel<AbstractClientPlayer> m = pr.getModel();
        m.body.visible = false;
        m.jacket.visible = false;
        hidden = true;
    }

    @SubscribeEvent
    public static void post(RenderLivingEvent.Post<?, ?> e) {
        if (!hidden) return;
        hidden = false;
        if (e.getEntity() instanceof AbstractClientPlayer p && e.getRenderer() instanceof PlayerRenderer pr) {
            PlayerModel<AbstractClientPlayer> m = pr.getModel();
            m.body.visible = true;
            m.jacket.visible = p.isModelPartShown(PlayerModelPart.JACKET);
        }
    }
}
