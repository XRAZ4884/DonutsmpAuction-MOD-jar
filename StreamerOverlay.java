package com.xraz.donutauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class StreamerOverlay {
    private StreamerOverlay() {}
    public static void render(DrawContext context, MinecraftClient client) {
        AuctionRecord record = AuctionStore.active();
        if (record == null) return;
        int x = 12, y = 12, width = 180, height = 52;
        context.fill(x, y, x + width, y + height, 0xD91A1425);
        context.fill(x, y, x + 3, y + height, 0xFFB66CFF);
        context.drawItem(new ItemStack(record.item()), x + 9, y + 9);
        context.drawText(client.textRenderer, Text.literal("XRAZ AUCTION"), x + 34, y + 7, 0xFFD6A8FF, false);
        context.drawText(client.textRenderer, Text.literal(record.itemName()), x + 34, y + 19, 0xFFFFFFFF, false);
        context.drawText(client.textRenderer, Text.literal("$" + record.price + "  •  " + record.formattedTime()), x + 34, y + 32, 0xFFE4D9F0, false);
    }
}
