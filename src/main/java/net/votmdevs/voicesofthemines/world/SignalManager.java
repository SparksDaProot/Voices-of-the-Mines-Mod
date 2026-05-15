package net.votmdevs.voicesofthemines.world;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.items.ItemStackHandler;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import org.slf4j.Logger;

import java.util.*;

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

    public static class DailyTask {
        public int requiredSignalLevel;
        public int requiredSignalAmount;
        public List<String> requiredHashes = new ArrayList<>();
    }

    public DailyTask currentTask = new DailyTask();
    public boolean isTaskCompletedToday = false;

    private final List<VotvSignal> activeSignals = new ArrayList<>();
    private int tickCounter = 0;
    private final PlayerData globalPlayerData = new PlayerData();

    public boolean isCensorEventActive = false;
    public int censorEventTimer = 0;

    public boolean isBadSunActive = false;
    private boolean isFirstDayInitialized = false;

    public boolean isRozitalEventPending = false;
    public int rozitalEventTargetDay = -1;
    public long rozitalEventTargetTime = -1;

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

    // Task Generation
    public void generateDailyTask() {
        Random rand = new Random();
        currentTask.requiredHashes.clear();
        isTaskCompletedToday = false;

        int hashCount = 3;

        if (currentDay <= 5) {
            currentTask.requiredSignalLevel = 0; currentTask.requiredSignalAmount = 1; hashCount = 1;
        } else if (currentDay <= 10) {
            currentTask.requiredSignalLevel = 0; currentTask.requiredSignalAmount = 3; hashCount = 2;
        } else if (currentDay <= 15) {
            currentTask.requiredSignalLevel = 1; currentTask.requiredSignalAmount = 1; // +2lvl0
        } else if (currentDay <= 20) {
            currentTask.requiredSignalLevel = 1; currentTask.requiredSignalAmount = 2;
        } else if (currentDay <= 25) {
            currentTask.requiredSignalLevel = 1; currentTask.requiredSignalAmount = 3;
        } else if (currentDay <= 30) {
            currentTask.requiredSignalLevel = 2; currentTask.requiredSignalAmount = 1;
        } else if (currentDay <= 35) {
            currentTask.requiredSignalLevel = 2; currentTask.requiredSignalAmount = 2;
        } else if (currentDay <= 40) {
            currentTask.requiredSignalLevel = 2; currentTask.requiredSignalAmount = 3;
        } else if (currentDay <= 45) {
            currentTask.requiredSignalLevel = 3; currentTask.requiredSignalAmount = 1;
        } else if (currentDay <= 50) {
            currentTask.requiredSignalLevel = 3; currentTask.requiredSignalAmount = 2;
        } else {
            currentTask.requiredSignalLevel = 3; currentTask.requiredSignalAmount = 3;
        }

        List<String> availableSatellites = new ArrayList<>(Arrays.asList(SATELLITES));
        for (int i = 0; i < hashCount; i++) {
            if (availableSatellites.isEmpty()) break;
            int idx = rand.nextInt(availableSatellites.size());
            currentTask.requiredHashes.add(availableSatellites.get(idx));
            availableSatellites.remove(idx);
        }

        sendDailyTaskEmail();
        setDirty();
    }

    private void sendDailyTaskEmail() {
        StringBuilder text = new StringBuilder("There is a task for today:\nYou need to bring us these signals:\n");
        text.append(currentTask.requiredSignalAmount).append(" signals of ").append(currentTask.requiredSignalLevel).append(" level\n\n");
        text.append("And check this satellite and make report:\n");

        for (String sat : currentTask.requiredHashes) {
            text.append(sat).append("\n");
        }

        globalPlayerData.broadcastEmail("Dr. Bao", "Daily Task", text.toString());
        // sound
        net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.EmailNotificationPacket()
        );
    }

    public void scheduleRozitalEvent() {
        if (!isRozitalEventPending) {
            isRozitalEventPending = true;
            rozitalEventTargetDay = currentDay + 1; // Запланировано на следующий день
            rozitalEventTargetTime = new Random().nextInt(24000); // В рандомный тик (от 0 до 24000)
            setDirty();
        }
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

    // counting for task
    public int checkDailyTask(ItemStack driveBox, ItemStack paperSheet) {
        if (isTaskCompletedToday) return 0;

        int foundValidSignals = 0;
        int totalDisks = 0; // count drives in the box

        if (driveBox != null && driveBox.getItem() == VoicesOfTheMines.DRIVE_BOX_ITEM.get() && driveBox.hasTag() && driveBox.getTag().contains("Inventory")) {
            ItemStackHandler handler = new ItemStackHandler(6);
            handler.deserializeNBT(driveBox.getTag().getCompound("Inventory"));

            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack disk = handler.getStackInSlot(i);
                if (!disk.isEmpty() && disk.hasTag()) {
                    totalDisks++;
                    if (disk.getTag().getInt("SignalLevel") == currentTask.requiredSignalLevel) {
                        foundValidSignals++;
                    }
                }
            }
        }
        if (foundValidSignals < currentTask.requiredSignalAmount) return 0;

        boolean hashesValid = true;
        if (currentTask.requiredHashes.size() > 0) {
            if (paperSheet == null || paperSheet.getItem() != VoicesOfTheMines.PAPER_SHEET.get() || !paperSheet.hasTag()) {
                return 0;
            }

            CompoundTag tag = paperSheet.getTag();
            if (!tag.getBoolean("Written") || !tag.contains("Lines")) return 0;

            ListTag lines = tag.getList("Lines", 8);
            String fullPaperText = "";
            for (int i = 0; i < lines.size(); i++) {
                fullPaperText += lines.getString(i).toUpperCase() + " ";
            }

            for (String sat : currentTask.requiredHashes) {
                String requiredHash = dailyHashes.get(sat).toUpperCase();
                if (!fullPaperText.contains(requiredHash)) {
                    hashesValid = false;
                    break;
                }
            }
        }

        if (!hashesValid) return 0;

        isTaskCompletedToday = true;
        setDirty();

        if (foundValidSignals == currentTask.requiredSignalAmount && totalDisks == currentTask.requiredSignalAmount) return 1;
        else return 2;
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
        // send email of first task
        if (!isFirstDayInitialized && globalPlayerData.hasAnyPlayers()) {
            generateDailyTask();
            isFirstDayInitialized = true;
            setDirty();
        }

        tickCounter++;
        if (tickCounter >= 1000) { // Change the signal update frequency here! 1000 FOR TESTS (EVERY 1 MINUTE) < -- - -  - -- - - - - - :P
            tickCounter = 0;
            if (getUncaughtSignals().size() < 15) {
                float randX = (float) ((Math.random() - 0.5) * 10000); float randY = (float) ((Math.random() - 0.5) * 10000);
                float tLine = (float) (Math.random() * 300); float tWave = (float) (Math.random() * 300);

                String[] commons = {"mars", "venus", "enceladus", "ceres", "dione", "bennu", "mercury", "makemake", "rhea", "iris", "amazur", "vion", "subplanet", "europa", "moon", "jupiter", "uranus", "neptune", "saturn", "hilero", "asteroid", "mettus", "white_dwarf", "io", "tamalan"};
                String[] rares = {"siggen1", "siggen2", "siggen3", "siggen4", "siggen5", "siggen6", "earth", "exogen1", "exogen2", "neutron0", "blackhole0", "monty", "sat1", "hatefulstar"};
                String[] rarers = {"faces", "retroplanet", "votv_earth", "hairy", "roz0", "tamalanflag", "nev", "niko", "tulpar"};
                String[] veryRares = {"siggenus1", "siggenus2", "siggenus3", "siggenus4", "siggenus5", "siggenus6", "siggenus7", "siggenus8", "fard", "ironlung", "funeral", "evil", "pizzabreather", "piramid"};

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

        tag.putBoolean("RozitalPending", isRozitalEventPending);
        tag.putInt("RozitalDay", rozitalEventTargetDay);
        tag.putLong("RozitalTime", rozitalEventTargetTime);

        tag.putInt("CurrentDay", currentDay);
        tag.putBoolean("BadSun", isBadSunActive);
        tag.putBoolean("CensorEventActive", isCensorEventActive);
        tag.putInt("CensorEventTimer", censorEventTimer);
        tag.putBoolean("FirstDayInit", isFirstDayInitialized);
        CompoundTag hashTag = new CompoundTag();
        for (Map.Entry<String, String> e : dailyHashes.entrySet()) hashTag.putString(e.getKey(), e.getValue());
        tag.put("DailyHashes", hashTag);

        CompoundTag calTag = new CompoundTag();
        for (Map.Entry<String, Float> e : calibrations.entrySet()) calTag.putFloat(e.getKey(), e.getValue());
        tag.put("Calibrations", calTag);

        CompoundTag srvTag = new CompoundTag();
        for (Map.Entry<String, BlockPos> e : placedServers.entrySet()) srvTag.putLong(e.getKey(), e.getValue().asLong());
        tag.put("PlacedServers", srvTag);

        tag.putBoolean("TaskCompleted", isTaskCompletedToday);
        tag.putInt("ReqLevel", currentTask.requiredSignalLevel);
        tag.putInt("ReqAmount", currentTask.requiredSignalAmount);
        ListTag hashesList = new ListTag();
        for (String h : currentTask.requiredHashes) hashesList.add(StringTag.valueOf(h));
        tag.put("ReqHashes", hashesList);

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

        if (tag.contains("RozitalPending")) manager.isRozitalEventPending = tag.getBoolean("RozitalPending");
        if (tag.contains("RozitalDay")) manager.rozitalEventTargetDay = tag.getInt("RozitalDay");
        if (tag.contains("RozitalTime")) manager.rozitalEventTargetTime = tag.getLong("RozitalTime");
        if (tag.contains("CurrentDay")) manager.currentDay = tag.getInt("CurrentDay");
        if (tag.contains("BadSun")) manager.isBadSunActive = tag.getBoolean("BadSun");
        if (tag.contains("CensorEventActive")) {
            manager.isCensorEventActive = tag.getBoolean("CensorEventActive");
        }

        if (tag.contains("CensorEventTimer")) {
            manager.censorEventTimer = tag.getInt("CensorEventTimer");
        }
        if (tag.contains("FirstDayInit")) manager.isFirstDayInitialized = tag.getBoolean("FirstDayInit");
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

        if (tag.contains("TaskCompleted")) manager.isTaskCompletedToday = tag.getBoolean("TaskCompleted");
        if (tag.contains("ReqLevel")) manager.currentTask.requiredSignalLevel = tag.getInt("ReqLevel");
        if (tag.contains("ReqAmount")) manager.currentTask.requiredSignalAmount = tag.getInt("ReqAmount");
        if (tag.contains("ReqHashes")) {
            manager.currentTask.requiredHashes.clear();
            ListTag hashesList = tag.getList("ReqHashes", 8);
            for (int i = 0; i < hashesList.size(); i++) manager.currentTask.requiredHashes.add(hashesList.getString(i));
        }

        return manager;
    }
}