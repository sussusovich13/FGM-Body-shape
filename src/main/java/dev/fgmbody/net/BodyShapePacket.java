package dev.fgmbody.net;

import dev.fgmbody.BodyShape;
import dev.fgmbody.BodyShapeStore;
import dev.fgmbody.FgmBodyMod;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BodyShapePacket {
    private final UUID uuid;
    private final BodyShape shape;

    public BodyShapePacket(UUID uuid, BodyShape shape) {
        this.uuid = uuid;
        this.shape = shape;
    }

    public static void encode(BodyShapePacket p, FriendlyByteBuf buf) {
        buf.writeUUID(p.uuid);
        p.shape.write(buf);
    }

    public static BodyShapePacket decode(FriendlyByteBuf buf) {
        return new BodyShapePacket(buf.readUUID(), BodyShape.read(buf));
    }

    public static void handle(BodyShapePacket p, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            if (c.getDirection().getReceptionSide().isServer()) {
                ServerPlayer sender = c.getSender();
                // Same validation idea as FGM: the uuid must match the sender.
                if (sender == null || !sender.getUUID().equals(p.uuid)) return;
                BodyShape clean = p.shape.clamped();
                BodyShapeStore.SERVER.put(p.uuid, clean);
                FgmBodyMod.CHANNEL.send(PacketDistributor.ALL.noArg(), new BodyShapePacket(p.uuid, clean));
            } else {
                BodyShapeStore.CLIENT.put(p.uuid, p.shape);
            }
        });
        c.setPacketHandled(true);
    }
}
