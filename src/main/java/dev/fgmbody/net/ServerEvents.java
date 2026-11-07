package dev.fgmbody.net;

import dev.fgmbody.BodyShape;
import dev.fgmbody.BodyShapeStore;
import dev.fgmbody.FgmBodyMod;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = FgmBodyMod.MODID)
public final class ServerEvents {
    /** A joining player gets the shapes of everyone already online. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        for (Map.Entry<UUID, BodyShape> entry : BodyShapeStore.SERVER.entrySet()) {
            FgmBodyMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new BodyShapePacket(entry.getKey(), entry.getValue()));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        BodyShapeStore.SERVER.remove(e.getEntity().getUUID());
    }
}
