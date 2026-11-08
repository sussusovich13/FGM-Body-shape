package dev.fgmbody;

import dev.fgmbody.net.BodyShapePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(FgmBodyMod.MODID)
public class FgmBodyMod {
    public static final String MODID = "fgmbody";
    private static final String PROTOCOL = "3";

    // Own channel: no conflicts with FGM / FGM Plus packets.
    // "v -> true" lets players join servers that don't have this addon.
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"), () -> PROTOCOL, v -> true, v -> true);

    public FgmBodyMod() {
        CHANNEL.registerMessage(0, BodyShapePacket.class,
                BodyShapePacket::encode, BodyShapePacket::decode, BodyShapePacket::handle);
    }
}
