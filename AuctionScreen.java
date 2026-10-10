package com.xraz.donutauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class AuctionScreen extends Screen {
    private TextFieldWidget searchField, priceField, durationField;
    private final List<net.minecraft.item.Item> filtered = new ArrayList<>();
    private int selectedItemIndex = 0;
    private int listOffset = 0;
    private String message = "Select an item, set a price, and start.";
    private int messageColor = 0xFFBFA7D9;
    private boolean compact = false;
    private boolean overlayOn = true;

    public AuctionScreen() { super(Text.literal("XRAZ AUCTION")); }

    @Override
    protected void init() {
        int left = Math.max(12, width / 2 - 205);
        int top = Math.max(12, height / 2 - 125);
        searchField = new TextFieldWidget(textRenderer, left + 12, top + 43, 180, 18, Text.literal("Search items"));
        searchField.setPlaceholder(Text.literal("Search all Minecraft items..."));
        searchField.setChangedListener(s -> refreshItems());
        addDrawableChild(searchField);

        priceField = new TextFieldWidget(textRenderer, left + 220, top + 72, 150, 18, Text.literal("Price"));
        AuctionRecord current = AuctionStore.selected();
        priceField.setText(current == null ? "0" : current.price);
        addDrawableChild(priceField);
        durationField = new TextFieldWidget(textRenderer, left + 220, top + 113, 150, 18, Text.literal("Duration (seconds)"));
        durationField.setText(current == null ? "60" : String.valueOf(current.durationSeconds));
        addDrawableChild(durationField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Create / Save"), b -> saveRecord())
                .dimensions(left + 220, top + 142, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Start"), b -> withSelected(AuctionRecord::start))
                .dimensions(left + 220, top + 169, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Pause"), b -> withSelected(AuctionRecord::pause))
                .dimensions(left + 298, top + 169, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Resume"), b -> withSelected(AuctionRecord::resume))
                .dimensions(left + 220, top + 194, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> withSelected(AuctionRecord::reset))
                .dimensions(left + 298, top + 194, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("End"), b -> withSelected(AuctionRecord::end))
                .dimensions(left + 220, top + 219, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> { AuctionStore.deleteSelected(); syncFields(); })
                .dimensions(left + 298, top + 219, 72, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Overlay: ON/OFF"), b -> {
            overlayOn = !overlayOn; DonutAuctionClient.setOverlayVisible(overlayOn);
        }).dimensions(left + 12, top + 218, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Compact: ON/OFF"), b -> compact = !compact)
                .dimensions(left + 12, top + 243, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(left + 270, top + 243, 100, 20).build());
        refreshItems();
    }

    private void refreshItems() {
        String query = searchField == null ? "" : searchField.getText().toLowerCase(Locale.ROOT).trim();
        filtered.clear();
        Registries.ITEM.stream().filter(item -> query.isEmpty()
                || item.getName().getString().toLowerCase(Locale.ROOT).contains(query)
                || Registries.ITEM.getId(item).toString().toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(item -> item.getName().getString(), String.CASE_INSENSITIVE_ORDER))
                .limit(1000).forEach(filtered::add);
        selectedItemIndex = Math.max(0, Math.min(selectedItemIndex, filtered.size() - 1));
    }

    private void saveRecord() {
        try {
            String price = priceField.getText().trim();
            if (price.isEmpty() || price.length() > 24) throw new IllegalArgumentException();
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration < 1 || duration > 86400) throw new IllegalArgumentException();
            net.minecraft.item.Item item = filtered.isEmpty() ? Registries.ITEM.get(Identifier.of("minecraft:diamond")) : filtered.get(selectedItemIndex);
            AuctionRecord current = AuctionStore.selected();
            if (current == null) {
                AuctionStore.add(new AuctionRecord(Registries.ITEM.getId(item).toString(), price, duration));
            } else {
                current.itemId = Registries.ITEM.getId(item).toString();
                current.price = price;
                current.durationSeconds = duration;
                current.label = item.getName().getString();
                current.reset();
                AuctionStore.save();
            }
            syncFields();
            setMessage("Auction saved.", 0xFF8FE3B0);
        } catch (Exception ex) { setMessage("Invalid price or duration (1–86400 seconds).", 0xFFFF8585); }
    }

    private void syncFields() {
        AuctionRecord r = AuctionStore.selected();
        if (r != null && priceField != null) {
            priceField.setText(r.price);
            durationField.setText(String.valueOf(r.durationSeconds));
        }
    }
    private void withSelected(java.util.function.Consumer<AuctionRecord> action) {
        AuctionRecord record = AuctionStore.selected();
        if (record == null) { setMessage("Create an auction first.", 0xFFFF8585); return; }
        action.accept(record); AuctionStore.save();
        setMessage("Auction updated.", 0xFF8FE3B0);
    }
    private void setMessage(String value, int color) { message = value; messageColor = color; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = Math.max(12, width / 2 - 205);
        int top = Math.max(12, height / 2 - 125);
        int panelWidth = Math.min(410, width - 24);
        int panelHeight = Math.min(compact ? 270 : 300, height - 24);
        context.fill(left, top, left + panelWidth, top + panelHeight, 0xF21A1425);
        context.fill(left, top, left + panelWidth, top + 3, 0xFFB66CFF);
        context.drawText(textRenderer, Text.literal("XRAZ AUCTION"), left + 12, top + 10, 0xFFD6A8FF, false);
        context.drawText(textRenderer, Text.literal("Made by XRAZ"), left + panelWidth - 83, top + 12, 0xFFB7A8C8, false);
        context.drawText(textRenderer, Text.literal("ITEM REGISTRY"), left + 12, top + 32, 0xFFE8DDF5, false);
        context.drawText(textRenderer, Text.literal("AUCTION CONTROLS"), left + 220, top + 48, 0xFFE8DDF5, false);
        super.render(context, mouseX, mouseY, delta);
        int listTop = top + 66;
        int listBottom = top + 202;
        int row = 0;
        for (int i = listOffset; i < Math.min(filtered.size(), listOffset + 7); i++) {
            int y = listTop + row * 19;
            if (i == selectedItemIndex) context.fill(left + 10, y - 2, left + 205, y + 16, 0xFF49305F);
            context.drawItem(new ItemStack(filtered.get(i)), left + 12, y - 1);
            context.drawText(textRenderer, Text.literal(filtered.get(i).getName().getString()), left + 33, y + 3, 0xFFFFFFFF, false);
            row++;
        }
        AuctionRecord current = AuctionStore.selected();
        if (current != null) {
            context.drawItem(new ItemStack(current.item()), left + 220, top + 48);
            context.drawText(textRenderer, Text.literal(current.itemName()), left + 243, top + 53, 0xFFFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Timer: " + current.formattedTime() + "  " + current.status), left + 220, top + 263, 0xFFD6A8FF, false);
        }
        context.drawText(textRenderer, Text.literal(message), left + 12, top + panelHeight - 18, messageColor, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
        int left = Math.max(12, width / 2 - 205);
        int top = Math.max(12, height / 2 - 125);
        if (click.x() >= left + 10 && click.x() <= left + 205 && click.y() >= top + 64 && click.y() <= top + 202) {
            int index = listOffset + (int)((click.y() - (top + 64)) / 19);
            if (index >= 0 && index < filtered.size()) {
                selectedItemIndex = index;
                if (!filtered.isEmpty()) {
                    String id = Registries.ITEM.getId(filtered.get(index)).toString();
                    AuctionRecord r = AuctionStore.selected();
                    if (r != null) { r.itemId = id; r.label = filtered.get(index).getName().getString(); syncFields(); AuctionStore.save(); }
                }
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (input.key() == GLFW.GLFW_KEY_DOWN && selectedItemIndex < filtered.size()-1) { selectedItemIndex++; return true; }
        if (input.key() == GLFW.GLFW_KEY_UP && selectedItemIndex > 0) { selectedItemIndex--; return true; }
        return super.keyPressed(input);
    }
    @Override public void close() { MinecraftClient.getInstance().setScreen(null); }
    @Override public boolean shouldPause() { return false; }
}
