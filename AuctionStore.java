package com.xraz.donutauction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AuctionStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xrazauction.json");
    private static final Type LIST_TYPE = new TypeToken<ArrayList<AuctionRecord>>(){}.getType();
    private static final List<AuctionRecord> RECORDS = new ArrayList<>();
    private static int selectedIndex = 0;

    private AuctionStore() {}
    public static List<AuctionRecord> records() { return RECORDS; }
    public static int selectedIndex() { return selectedIndex; }
    public static void select(int index) {
        if (index >= 0 && index < RECORDS.size()) selectedIndex = index;
    }
    public static AuctionRecord selected() {
        return RECORDS.isEmpty() ? null : RECORDS.get(Math.max(0, Math.min(selectedIndex, RECORDS.size()-1)));
    }
    public static AuctionRecord active() {
        for (AuctionRecord record : RECORDS) if (record.status == AuctionRecord.Status.RUNNING || record.status == AuctionRecord.Status.PAUSED) return record;
        return selected();
    }
    public static void add(AuctionRecord record) {
        RECORDS.add(record); selectedIndex = RECORDS.size()-1; save();
    }
    public static void deleteSelected() {
        if (!RECORDS.isEmpty()) {
            RECORDS.remove(Math.max(0, Math.min(selectedIndex, RECORDS.size()-1)));
            selectedIndex = Math.max(0, Math.min(selectedIndex, RECORDS.size()-1));
            save();
        }
    }
    public static void tick() {
        boolean changed = false;
        for (AuctionRecord record : RECORDS) {
            AuctionRecord.Status before = record.status;
            record.tick();
            if (before != record.status) changed = true;
        }
        if (changed) save();
    }
    public static void load() {
        RECORDS.clear();
        try {
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    List<AuctionRecord> loaded = GSON.fromJson(reader, LIST_TYPE);
                    if (loaded != null) for (AuctionRecord record : loaded) if (record != null) {
                        if (record.durationSeconds < 1 || record.durationSeconds > 86400) record.durationSeconds = 60;
                        if (record.remainingMillis < 0) record.remainingMillis = record.durationSeconds * 1000L;
                        if (record.status == AuctionRecord.Status.RUNNING) {
                            record.remainingMillis = record.remainingMillis();
                            record.status = AuctionRecord.Status.PAUSED;
                        }
                        RECORDS.add(record);
                    }
                }
            }
        } catch (Exception ignored) { RECORDS.clear(); }
        if (RECORDS.isEmpty()) RECORDS.add(new AuctionRecord("minecraft:diamond", "0", 60));
        save();
    }
    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) { GSON.toJson(RECORDS, LIST_TYPE, writer); }
        } catch (Exception ignored) {}
    }
}
