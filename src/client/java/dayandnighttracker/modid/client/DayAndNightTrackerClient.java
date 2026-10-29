package dayandnighttracker.modid.client;

import dayandnighttracker.modid.DayAndNightTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DayAndNightTrackerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Settings.load();
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            DayAndNightTracker.id("overlay"),
            DayAndNightTrackerClient::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof EditScreen) return;
        OverlayRenderer.Info info = OverlayRenderer.current();
        int w = OverlayRenderer.width(mc.font, info);
        OverlayRenderer.draw(graphics, mc.font, info, OverlayRenderer.posX(w), OverlayRenderer.posY());
    }
}
