package dev.fgmbody.client;

import com.wildfire.gui.WildfireButton;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.ModList;

/** The only class that references FGM classes. */
final class FgmCompat {
    private FgmCompat() {}

    static void addButton(ScreenEvent.Init.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Screen s = e.getScreen();
        GenderPlayer plr = WildfireGender.getPlayerById(mc.player.getUUID());
        boolean female = plr != null && plr.getGender().canHaveBreasts();
        int y = s.height / 2;
        // Same column/pitch FGM Plus uses: female buttons end at y+8, male at y-12.
        int yPos = female ? y + 8 : y - 12;
        // FGM Plus' Shape Studio button takes that slot; go one row lower.
        if (ModList.get().isLoaded("fgmplus")) yPos += 22;
        e.addListener(new WildfireButton(s.width / 2 - 42, yPos, 158, 20,
                Component.literal("Body shape (waist, hips, glutes)"),
                b -> mc.setScreen(new BodyShapeScreen(s))));
    }
}
