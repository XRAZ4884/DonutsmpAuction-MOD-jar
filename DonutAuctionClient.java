package com.xraz.donutauction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class DonutAuctionClient implements ClientModInitializer {
    public static final String MOD_ID = "xrazauction";
    private static KeyBinding openDashboard;
    private static KeyBinding toggleOverlay;
    private static KeyBinding pauseResume;
    private static boolean overlayVisible = true;
    private static AuctionScreen dashboard;

    @Override
    public void onInitializeClient() {
        AuctionStore.load();
        openDashboard = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xrazauction.open_dashboard", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));
        toggleOverlay = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xrazauction.toggle_overlay", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O, KeyBinding.Category.MISC));
        pauseResume = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xrazauction.pause_resume", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openDashboard.wasPressed()) {
                if (client.currentScreen == null || client.currentScreen instanceof AuctionScreen) {
                    if (client.currentScreen instanceof AuctionScreen) {
                        client.setScreen(null);
                    } else {
                        dashboard = new AuctionScreen();
                        client.setScreen(dashboard);
                    }
                }
            }
            while (toggleOverlay.wasPressed()) overlayVisible = !overlayVisible;
            while (pauseResume.wasPressed()) {
                AuctionRecord active = AuctionStore.active();
                if (active != null) active.togglePause();
            }
            AuctionStore.tick();
        });
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (overlayVisible && client.player != null && client.world != null) {
                StreamerOverlay.render(drawContext, client);
            }
        });
    }

    public static boolean overlayVisible() { return overlayVisible; }
    public static void setOverlayVisible(boolean value) { overlayVisible = value; }
}
