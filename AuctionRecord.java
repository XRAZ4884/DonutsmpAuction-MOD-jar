package com.xraz.donutauction;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class AuctionRecord {
    public enum Status { READY, RUNNING, PAUSED, COMPLETED, STOPPED }

    public String itemId = "minecraft:diamond";
    public String price = "0";
    public int durationSeconds = 60;
    public long remainingMillis = 60_000;
    public long lastStartedMillis;
    public Status status = Status.READY;
    public String label = "Diamond auction";

    public AuctionRecord() {}
    public AuctionRecord(String itemId, String price, int durationSeconds) {
        this.itemId = itemId;
        this.price = price;
        this.durationSeconds = Math.max(1, Math.min(durationSeconds, 86400));
        this.remainingMillis = this.durationSeconds * 1000L;
        this.label = item().getName().getString();
    }

    public Item item() {
        try {
            Item item = Registries.ITEM.get(Identifier.of(itemId));
            return item == null ? Items.DIAMOND : item;
        } catch (Exception ignored) {
            return Items.DIAMOND;
        }
    }

    public String itemName() { return item().getName().getString(); }
    public long remainingMillis() {
        if (status == Status.RUNNING) return Math.max(0, remainingMillis - (System.currentTimeMillis() - lastStartedMillis));
        return Math.max(0, remainingMillis);
    }
    public void start() {
        if (status == Status.COMPLETED || status == Status.STOPPED) reset();
        if (status != Status.RUNNING) {
            lastStartedMillis = System.currentTimeMillis();
            status = Status.RUNNING;
        }
    }
    public void pause() {
        if (status == Status.RUNNING) {
            remainingMillis = remainingMillis();
            status = Status.PAUSED;
        }
    }
    public void resume() { if (status == Status.PAUSED) start(); }
    public void togglePause() {
        if (status == Status.RUNNING) pause();
        else if (status == Status.PAUSED || status == Status.READY) start();
    }
    public void reset() {
        durationSeconds = Math.max(1, Math.min(durationSeconds, 86400));
        remainingMillis = durationSeconds * 1000L;
        status = Status.READY;
        lastStartedMillis = 0;
    }
    public void end() { remainingMillis = 0; status = Status.STOPPED; }
    public void tick() {
        if (status == Status.RUNNING && remainingMillis() <= 0) {
            remainingMillis = 0;
            status = Status.COMPLETED;
            MinecraftNotice.completed(itemName());
        }
    }
    public String formattedTime() {
        long total = (remainingMillis() + 999) / 1000;
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
