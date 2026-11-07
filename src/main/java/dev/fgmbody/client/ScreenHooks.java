package dev.fgmbody.client;

import dev.fgmbody.FgmBodyMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FgmBodyMod.MODID, value = Dist.CLIENT)
public final class ScreenHooks {
    @SubscribeEvent
    public static void init(ScreenEvent.Init.Post e) {
        // Compare by name so this class never touches FGM classes unless the wardrobe is really open
        if (!"com.wildfire.gui.screen.WardrobeBrowserScreen".equals(e.getScreen().getClass().getName())) return;
        if (!ModList.get().isLoaded("wildfire_gender")) return;
        FgmCompat.addButton(e);
    }
}
