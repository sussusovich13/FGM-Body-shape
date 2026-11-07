package dev.fgmbody.client;

import dev.fgmbody.BodyShape;
import dev.fgmbody.BodyShapeStore;
import dev.fgmbody.FgmBodyMod;
import dev.fgmbody.net.BodyShapePacket;
import java.util.UUID;
import net.minecraft.network.Connection;

final class ClientNet {
    static void push(UUID id, BodyShape shape, Connection connection) {
        BodyShapeStore.CLIENT.put(id, shape);
        if (connection != null && FgmBodyMod.CHANNEL.isRemotePresent(connection)) {
            FgmBodyMod.CHANNEL.sendToServer(new BodyShapePacket(id, shape));
        }
    }
}
