package dev.fgmbody.client;

import dev.fgmbody.BodyShape;
import dev.fgmbody.BodyShapeStore;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Standalone settings screen (key J, or the button in FGM's wardrobe). */
public class BodyShapeScreen extends Screen {
    private final Screen parent;
    private float glutesSize, glutesHeight, hipsWidth, waist;

    public BodyShapeScreen() {
        this(null);
    }

    public BodyShapeScreen(Screen parent) {
        super(Component.literal("Body shape"));
        this.parent = parent;
        BodyShape s = BodyShapeStore.local();
        glutesSize = s.glutesSize();
        glutesHeight = s.glutesHeight();
        hipsWidth = s.hipsWidth();
        waist = s.waist();
    }

    @Override
    protected void init() {
        int w = 200, x = this.width / 2 - w / 2, y = this.height / 2 - 52;
        addRenderableWidget(new Slider(x, y, w, "Waist (slimmer)", 0f, 1f, waist, v -> waist = v));
        addRenderableWidget(new Slider(x, y + 24, w, "Hips width", 0f, 1f, hipsWidth, v -> hipsWidth = v));
        addRenderableWidget(new Slider(x, y + 48, w, "Glutes size", 0f, 1f, glutesSize, v -> glutesSize = v));
        addRenderableWidget(new Slider(x, y + 72, w, "Glutes height", -1f, 1f, glutesHeight, v -> glutesHeight = v));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(x, y + 106, w, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 74, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public void onClose() {
        BodyShape s = new BodyShape(glutesSize, glutesHeight, hipsWidth, waist).clamped();
        BodyShapeStore.setLocal(s);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.getConnection() != null) {
            ClientNet.push(mc.player.getUUID(), s, mc.getConnection().getConnection());
        }
        mc.setScreen(parent);
    }

    private static class Slider extends AbstractSliderButton {
        private final String label;
        private final float min, max;
        private final Consumer<Float> setter;

        Slider(int x, int y, int w, String label, float min, float max, float cur, Consumer<Float> setter) {
            super(x, y, w, 20, Component.empty(), (cur - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        private float get() {
            return min + (float) this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + Math.round(get() * 100) + "%"));
        }

        @Override
        protected void applyValue() {
            setter.accept(get());
        }
    }
}
