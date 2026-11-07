package dev.fgmbody.client;

import dev.fgmbody.FgmBodyMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = FgmBodyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final KeyMapping OPEN_MENU =
            new KeyMapping("key.fgmbody.open", GLFW.GLFW_KEY_J, "key.categories.misc");

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(OPEN_MENU);
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.AddLayers e) {
        for (String skin : e.getSkins()) {
            if (e.getSkin(skin) instanceof PlayerRenderer pr) {
                pr.addLayer(new BodyShapeLayer(pr));
            }
        }
    }
}
