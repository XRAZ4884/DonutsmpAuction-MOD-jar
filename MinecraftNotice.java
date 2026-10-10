package com.xraz.donutauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

final class MinecraftNotice {
    private MinecraftNotice() {}
    static void completed(String itemName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal("§d[XRAZ AUCTION] §fAuction timer completed: " + itemName), false);
    }
}
