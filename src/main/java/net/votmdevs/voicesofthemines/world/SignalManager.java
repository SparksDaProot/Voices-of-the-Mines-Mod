package net.votmdevs.voicesofthemines.world;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class SignalManager extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String[] SATELLITES = {
            "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo", "Tango",
            "Victor", "Echo", "Xray", "Yankee", "Uniform", "Sierra", "Whiskey", "Golf",
            "Delta", "Charlie", "Bravo", "Hotel", "India", "Juliett", "Foxtrot"
    };

    public static class VotvSignal {
        public final String id; public final float x, y; public final String type;
        public boolean isDownloaded = false; public boolean isCalibrated = false; public boolean isChecked = false;
        public final float targetLine; public final float targetWave;

        public VotvSignal(String id, float x, float y, String type, boolean isDownloaded, boolean isCalibrated, boolean isChecked, float targetLine, float targetWave) {
            this.id = id; this.x = x; this.y = y; this.type = type;
            this.isDownloaded = isDownloaded; this.isCalibrated = isCalibrated; this.isChecked = isChecked;
            this.targetLine = targetLine; this.targetWave = targetWave;
        }
    }

    private final List<VotvSignal> activeSignals = new ArrayList<>();
    private int tickCounter = 0;
    private final PlayerData globalPlayerData = new PlayerData();

    public int currentDay = 1;
    public final Map<String, String> dailyHashes = new HashMap<>();
    public final Map<String, Float> calibrations = new HashMap<>();
    public final Map<String, BlockPos> placedServers = new HashMap<>();

    public SignalManager() {
        for (String sat : SATELLITES) calibrations.put(sat, 100.0f);
        generateDailyHashes();
    }

    public void generateDailyHashes() {
        Random rand = new Random();
        dailyHashes.clear();
        for (String sat : SATELLITES) {
            String letters = "";
            for(int i=0; i<4; i++) letters += (char)('A' + rand.nextInt(26));
            String hash = String.format("%03d%s%02d", rand.nextInt(1000), letters, rand.nextInt(100));
            dailyHashes.put(sat, hash);
        }
        setDirty();
    }

    public void advanceDay() {
        currentDay++;
        generateDailyHashes();
        setDirty();
    }

    public void degradeRandomCalibration() {
        Random rand = new Random();
        String target = SATELLITES[rand.nextInt(SATELLITES.length)];
        float current = calibrations.getOrDefault(target, 100.0f);
        current -= (1.0f + rand.nextFloat() * 2.0f); // Падение от 1 до 3%
        if (current < 0) current = 0;
        calibrations.put(target, current);
        setDirty();
    }

    public PlayerData getGlobalPlayerData() { return globalPlayerData; }

    public static SignalManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(SignalManager::load, SignalManager::new, "votv_signals");
    }

    public List<VotvSignal> getUncaughtSignals() {
        List<VotvSignal> list = new ArrayList<>();
        for (VotvSignal s : activeSignals) if (!s.isDownloaded) list.add(s);
        return list;
    }
    public VotvSignal getProcessingSignal() {
        for (VotvSignal s : activeSignals) if (s.isDownloaded && !s.isCalibrated) return s;
        return null;
    }
    public VotvSignal getCalibratedSignal() {
        for (VotvSignal s : activeSignals) if (s.isCalibrated && !s.isChecked) return s;
        return null;
    }

    public boolean hasProcessingSignal() {
        return getProcessingSignal() != null;
    }

    public VotvSignal getSignalById(String id) {
        for (VotvSignal s : activeSignals) if (s.id.equals(id)) return s;
        return null;
    }


    public void catchSignal(String id) { for (VotvSignal s : activeSignals) if (s.id.equals(id)) { s.isDownloaded = true; setDirty(); break; } }
    public void finishCalibration(String id) { for (VotvSignal s : activeSignals) if (s.id.equals(id)) { s.isCalibrated = true; setDirty(); break; } }
    public void finishCheck(String id) { for (VotvSignal s : activeSignals) if (s.id.equals(id)) { s.isChecked = true; setDirty(); break; } }

    public void tick() {
        tickCounter++;
        if (tickCounter >= 6000) {
            tickCounter = 0;
            if (getUncaughtSignals().size() < 15) {
                float randX = (float) ((Math.random() - 0.5) * 10000); float randY = (float) ((Math.random() - 0.5) * 10000);
                float tLine = (float) (Math.random() * 300); float tWave = (float) (Math.random() * 300);

                String[] commons = {"mars", "venus", "enceladus", "ceres", "dione", "moon", "jupiter", "uranus", "neptune", "saturn"};
                String[] rares = {"siggen1", "earth", "exogen1"};
                String[] rarers = {"faces", "retroplanet", "votv_earth"};
                String[] veryRares = {"fard", "ironlung"};

                int roll = (int) (Math.random() * 100);
                String type = roll < 60 ? commons[(int)(Math.random()*commons.length)] : roll < 85 ? rares[(int)(Math.random()*rares.length)] : roll < 95 ? rarers[(int)(Math.random()*rarers.length)] : veryRares[(int)(Math.random()*veryRares.length)];

                activeSignals.add(new VotvSignal(UUID.randomUUID().toString(), randX, randY, type, false, false, false, tLine, tWave));
                setDirty();
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (VotvSignal s : activeSignals) {
            CompoundTag st = new CompoundTag();
            st.putString("Id", s.id); st.putFloat("X", s.x); st.putFloat("Y", s.y); st.putString("Type", s.type);
            st.putBoolean("Downloaded", s.isDownloaded); st.putBoolean("Calibrated", s.isCalibrated); st.putBoolean("Checked", s.isChecked);
            st.putFloat("TLine", s.targetLine); st.putFloat("TWave", s.targetWave);
            list.add(st);
        }
        tag.put("Signals", list);
        tag.put("BaseData", globalPlayerData.serializeNBT());
        tag.putInt("TickCounter", tickCounter);

        tag.putInt("CurrentDay", currentDay);
        CompoundTag hashTag = new CompoundTag();
        for (Map.Entry<String, String> e : dailyHashes.entrySet()) hashTag.putString(e.getKey(), e.getValue());
        tag.put("DailyHashes", hashTag);

        CompoundTag calTag = new CompoundTag();
        for (Map.Entry<String, Float> e : calibrations.entrySet()) calTag.putFloat(e.getKey(), e.getValue());
        tag.put("Calibrations", calTag);

        CompoundTag srvTag = new CompoundTag();
        for (Map.Entry<String, BlockPos> e : placedServers.entrySet()) srvTag.putLong(e.getKey(), e.getValue().asLong());
        tag.put("PlacedServers", srvTag);

        return tag;
    }

    public static SignalManager load(CompoundTag tag) {
        SignalManager manager = new SignalManager();
        if (tag.contains("BaseData")) manager.globalPlayerData.deserializeNBT(tag.getCompound("BaseData"));
        if (tag.contains("Signals")) {
            ListTag list = tag.getList("Signals", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag st = list.getCompound(i);
                manager.activeSignals.add(new VotvSignal(st.getString("Id"), st.getFloat("X"), st.getFloat("Y"), st.getString("Type"),
                        st.getBoolean("Downloaded"), st.getBoolean("Calibrated"), st.getBoolean("Checked"), st.getFloat("TLine"), st.getFloat("TWave")));
            }
        }
        if (tag.contains("TickCounter")) manager.tickCounter = tag.getInt("TickCounter");

        if (tag.contains("CurrentDay")) manager.currentDay = tag.getInt("CurrentDay");
        if (tag.contains("DailyHashes")) {
            CompoundTag ht = tag.getCompound("DailyHashes");
            for (String k : ht.getAllKeys()) manager.dailyHashes.put(k, ht.getString(k));
        }
        if (tag.contains("Calibrations")) {
            CompoundTag ct = tag.getCompound("Calibrations");
            for (String k : ct.getAllKeys()) manager.calibrations.put(k, ct.getFloat(k));
        }
        if (tag.contains("PlacedServers")) {
            CompoundTag st = tag.getCompound("PlacedServers");
            for (String k : st.getAllKeys()) manager.placedServers.put(k, BlockPos.of(st.getLong(k)));
        }
        return manager;
    }
}