package net.votmdevs.voicesofthemines.world;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignalManager extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static class VotvSignal {
        public final String id;
        public final float x, y;
        public final String type;
        public boolean isDownloaded = false;
        public boolean isCalibrated = false;
        public boolean isChecked = false;
        public final float targetLine;
        public final float targetWave;

        public VotvSignal(String id, float x, float y, String type, boolean isDownloaded, boolean isCalibrated, boolean isChecked, float targetLine, float targetWave) {
            this.id = id; this.x = x; this.y = y; this.type = type;
            this.isDownloaded = isDownloaded; this.isCalibrated = isCalibrated; this.isChecked = isChecked;
            this.targetLine = targetLine; this.targetWave = targetWave;
        }
    }

    private final List<VotvSignal> activeSignals = new ArrayList<>();
    private int tickCounter = 0;

    private final PlayerData globalPlayerData = new PlayerData();
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

    public VotvSignal getSignalById(String id) {
        for (VotvSignal s : activeSignals) if (s.id.equals(id)) return s;
        return null;
    }

    public boolean hasProcessingSignal() { return getProcessingSignal() != null; }

    public void catchSignal(String id) {
        for (VotvSignal s : activeSignals) {
            if (s.id.equals(id)) { s.isDownloaded = true; setDirty(); break; }
        }
    }
    public void finishCalibration(String id) {
        for (VotvSignal s : activeSignals) {
            if (s.id.equals(id)) { s.isCalibrated = true; setDirty(); break; }
        }
    }
    public void finishCheck(String id) {
        for (VotvSignal s : activeSignals) {
            if (s.id.equals(id)) { s.isChecked = true; setDirty(); break; }
        }
    }

    public void tick() {
        tickCounter++;
        if (tickCounter >= 2000) {
            tickCounter = 0;
            if (getUncaughtSignals().size() < 15) {
                float randX = (float) ((Math.random() - 0.5) * 10000);
                float randY = (float) ((Math.random() - 0.5) * 10000);
                float tLine = (float) (Math.random() * 300);
                float tWave = (float) (Math.random() * 300);

                String[] commons = {"mars", "venus", "enceladus", "ceres", "dione", "bennu", "mercury", "makemake", "rhea", "iris", "amazur", "vion", "subplanet", "europa", "moon", "jupiter", "uranus", "neptune", "saturn", "hilero"};
                String[] rares = {"siggen1", "siggen2", "siggen3", "siggen4", "siggen5", "siggen6", "earth", "exogen1", "exogen2"};
                String[] rarers = {"faces", "retroplanet", "votv_earth", "hairy"};
                String[] veryRares = {"siggenus1", "siggenus2", "siggenus3", "siggenus4", "siggenus5", "siggenus6", "siggenus7", "siggenus8", "fard", "ironlung"};

                int roll = (int) (Math.random() * 100);
                String generatedType;

                if (roll < 60) { generatedType = commons[(int)(Math.random() * commons.length)]; }
                else if (roll < 85) { generatedType = rares[(int)(Math.random() * rares.length)]; }
                else if (roll < 95) { generatedType = rarers[(int)(Math.random() * rarers.length)]; }
                else { generatedType = veryRares[(int)(Math.random() * veryRares.length)]; }

                activeSignals.add(new VotvSignal(UUID.randomUUID().toString(), randX, randY, generatedType, false, false, false, tLine, tWave));
                setDirty();
                LOGGER.info("=== SIGNAL GENERATED (" + generatedType + ") ==="); // just for IDEA logs
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (VotvSignal s : activeSignals) {
            CompoundTag st = new CompoundTag();
            st.putString("Id", s.id);
            st.putFloat("X", s.x); st.putFloat("Y", s.y);
            st.putString("Type", s.type);
            st.putBoolean("Downloaded", s.isDownloaded);
            st.putBoolean("Calibrated", s.isCalibrated);
            st.putBoolean("Checked", s.isChecked);
            tag.put("BaseData", globalPlayerData.serializeNBT()); // Сохраняем деньги!
            st.putFloat("TLine", s.targetLine); st.putFloat("TWave", s.targetWave);
            list.add(st);
        }
        tag.put("Signals", list);
        tag.putInt("TickCounter", tickCounter);
        return tag;
    }

    public static SignalManager load(CompoundTag tag) {
        SignalManager manager = new SignalManager();
        if (tag.contains("BaseData")) {
            manager.globalPlayerData.deserializeNBT(tag.getCompound("BaseData"));
        }
        if (tag.contains("Signals")) {
            ListTag list = tag.getList("Signals", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag st = list.getCompound(i);
                manager.activeSignals.add(new VotvSignal(st.getString("Id"), st.getFloat("X"), st.getFloat("Y"), st.getString("Type"),
                        st.getBoolean("Downloaded"), st.getBoolean("Calibrated"), st.getBoolean("Checked"), st.getFloat("TLine"), st.getFloat("TWave")));
            }
        }
        if (tag.contains("TickCounter")) manager.tickCounter = tag.getInt("TickCounter");
        return manager;
    }
}