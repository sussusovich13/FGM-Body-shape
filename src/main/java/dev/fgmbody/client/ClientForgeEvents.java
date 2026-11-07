package dev.fgmbody.client;

import dev.fgmbody.BodyShapeStore;
import dev.fgmbody.FgmBodyMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FgmBodyMod.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (ClientModEvents.OPEN_MENU.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new BodyShapeScreen());
        }
    }

    @SubscribeEvent
    public static void login(ClientPlayerNetworkEvent.LoggingIn e) {
        ClientNet.push(e.getPlayer().getUUID(), BodyShapeStore.local(), e.getConnection());
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        BodyShapeStore.CLIENT.clear();
    }
}
