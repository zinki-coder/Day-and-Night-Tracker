package dayandnighttracker.modid.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private int grabX;
    private int grabY;

    public EditScreen(Screen parent) {
        super(Component.translatableWithFallback("dnt.edit.title", "Day and Night Tracker"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        this.addRenderableWidget(Button.builder(
                Component.translatableWithFallback("dnt.edit.reset", "Reset position"),
                b -> Settings.reset()).bounds(cx - 100, this.height - 52, 200, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                b -> this.onClose()).bounds(cx - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void onClose() {
        Settings.save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);

        String hint = Component.translatableWithFallback("dnt.edit.hint",
                "Drag the overlay with the mouse to move it").getString();
        g.text(this.font, hint, (this.width - this.font.width(hint)) / 2, 14, 0xFFFFFFFF, true);

        OverlayRenderer.Info info = OverlayRenderer.current();
        int w = OverlayRenderer.width(this.font, info);
        int x = OverlayRenderer.posX(w);
        int y = OverlayRenderer.posY();
        OverlayRenderer.draw(g, this.font, info, x, y);
        g.outline(x - 1, y - 1, w + 2, OverlayRenderer.HEIGHT + 2, dragging ? 0xFFFFFFFF : 0x80FFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() == 0) {
            OverlayRenderer.Info info = OverlayRenderer.current();
            int w = OverlayRenderer.width(this.font, info);
            int x = OverlayRenderer.posX(w);
            int y = OverlayRenderer.posY();
            if (event.x() >= x && event.x() < x + w && event.y() >= y && event.y() < y + OverlayRenderer.HEIGHT) {
                dragging = true;
                grabX = (int) event.x() - x;
                grabY = (int) event.y() - y;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            OverlayRenderer.Info info = OverlayRenderer.current();
            int w = OverlayRenderer.width(this.font, info);
            int nx = (int) event.x() - grabX;
            int ny = (int) event.y() - grabY;
            double fx = this.width - w <= 0 ? 0 : nx / (double) (this.width - w);
            double fy = this.height - OverlayRenderer.HEIGHT <= 0 ? 0 : ny / (double) (this.height - OverlayRenderer.HEIGHT);
            Settings.setPos(fx, fy);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging) {
            dragging = false;
            Settings.save();
            return true;
        }
        return super.mouseReleased(event);
    }
}
