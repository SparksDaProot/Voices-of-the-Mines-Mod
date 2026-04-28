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

    public boolean isBadSunActive = false;

    public int currentDay = 1;
    public final Map<String, String> dailyHashes = new HashMap<>();
    public final Map<String, Float> calibrations = new HashMap<>();
    public final Map<String, BlockPos> placedServers = new HashMap<>();
    public final Map<String, Long> lastFixedGameTimes = new HashMap<>();

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

        if (currentDay % 24 == 0) {
            isBadSunActive = true;
        } else {
            isBadSunActive = false;
        }

        setDirty();
    }

    public void degradeRandomCalibration(long gameTime, long recentlyFixedProtectionTicks) {
        Random rand = new Random();

        List<String> validTargets = new ArrayList<>();

        for (String sat : SATELLITES) {
            long lastFixed = lastFixedGameTimes.getOrDefault(sat, Long.MIN_VALUE);

            boolean isProtected = recentlyFixedProtectionTicks > 0L
                    && lastFixed != Long.MIN_VALUE
                    && gameTime - lastFixed < recentlyFixedProtectionTicks;

            if (isProtected) {
                if (net.votmdevs.voicesofthemines.config.VotmConfig.debugSignalBreaks()) {
                    LOGGER.info(
                            "[VOTM Signal Debug] Skipping {} because it was recently fixed. gameTime={} | lastFixed={} | remainingProtectionTicks={}",
                            sat,
                            gameTime,
                            lastFixed,
                            recentlyFixedProtectionTicks - (gameTime - lastFixed)
                    );
                }
            } else {
                validTargets.add(sat);
            }
        }

        if (validTargets.isEmpty()) {
            if (net.votmdevs.voicesofthemines.config.VotmConfig.debugSignalBreaks()) {
                LOGGER.info(
                        "[VOTM Signal Debug] No calibration degraded. All satellites are protected. gameTime={} | protectionTicks={}",
                        gameTime,
                        recentlyFixedProtectionTicks
                );
            }

            return;
        }

        String target = validTargets.get(rand.nextInt(validTargets.size()));

        float oldValue = calibrations.getOrDefault(target, 100.0F);
        float loss = 1.0F + rand.nextFloat() * 2.0F;
        float newValue = oldValue - loss;

        if (newValue < 0.0F) {
            newValue = 0.0F;
        }

        calibrations.put(target, newValue);
        setDirty();

        if (net.votmdevs.voicesofthemines.config.VotmConfig.debugSignalBreaks()) {
            LOGGER.info(
                    "[VOTM Signal Debug] Degraded {} calibration. old={} | loss={} | new={} | gameTime={}",
                    target,
                    oldValue,
                    loss,
                    newValue,
                    gameTime
            );
        }
    }

    public void markCalibrationFixed(String satellite, long gameTime) {
        if (satellite == null || satellite.isEmpty()) {
            return;
        }

        lastFixedGameTimes.put(satellite, gameTime);
        setDirty();
    }

    public void setCalibrationFixed(String satellite, float value, long gameTime) {
        if (satellite == null || satellite.isEmpty()) {
            return;
        }

        float clamped = Math.max(0.0F, Math.min(100.0F, value));

        calibrations.put(satellite, clamped);
        lastFixedGameTimes.put(satellite, gameTime);
        setDirty();

        if (net.votmdevs.voicesofthemines.config.VotmConfig.debugSignalBreaks()) {
            LOGGER.info(
                    "[VOTM Signal Debug] Marked {} as fixed. calibration={} | gameTime={}",
                    satellite,
                    clamped,
                    gameTime
            );
        }
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
        if (tickCounter >= 1000) { // Change the signal update frequency here! 1000 FOR TESTS (EVERY 1 MINUTE)
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
        tag.putBoolean("BadSun", isBadSunActive);
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
        if (tag.contains("BadSun")) manager.isBadSunActive = tag.getBoolean("BadSun");
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