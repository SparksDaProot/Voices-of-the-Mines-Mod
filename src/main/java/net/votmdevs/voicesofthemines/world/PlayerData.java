package net.votmdevs.voicesofthemines.world;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerData implements INBTSerializable<CompoundTag> {
    private int points = 0; // Наша валюта

    // Upgrades
    private int upgradeCursorSpeed = 0; // max 16
    private int upgradePingCooldown = 0; // max 16
    private int upgradeProcessingSpeed = 0; // max 16
    private int upgradeProcessingLevel = 0; // max 3
    // delvery
    private final NonNullList<ItemStack> deliveryQueue = NonNullList.create();

    // === НОВОЕ: ПОЧТА ===
    public static class Email {
        public String sender;
        public String title;
        public String text;
        public boolean isRead;

        public Email(String sender, String title, String text, boolean isRead) {
            this.sender = sender;
            this.title = title;
            this.text = text;
            this.isRead = isRead;
        }
    }

    private final List<Email> emails = new ArrayList<>();
    private boolean hasInitializedEmails = false;

    public PlayerData() {
        checkInitEmails();
    }

    private void checkInitEmails() {
        if (!hasInitializedEmails) {
            hasInitializedEmails = true;
            emails.add(new Email("Prof_Lea", "Welcome!", "Welcome to your new job, Dr.Steve. My name is Lea, your main supervisor. You've probably got through the learning period, but I'll remind you what your job is and what to do. In short, your job is basically scanning the sky for anomalous signals. That is your main task. Another task is to process the data of these signals and send us the data stored on analogue drives. You will get a reward for each drive, and if you process the signal on further levels, you will get more points. Next task is to look after these big satellite dishes, its servers, and calibration. You can re-calibrate satelites remotely through the console panel, but if the satellites server shuts down, you have to manually fix it. The server is inside the satellites. Alright, I think that is it - the nuclear reactor is not implemented, yet so you don't need to worry about that. Gather the signals, process them, sell results to us, look after the satellites, that's it. Good luck. Prof.Lea", false));
            emails.add(new Email("Dr Bao", "Hi", "Hello! I am Dr. Bao. I will be responsible for your daily tasks. In short, I will ask you to do a quick task at the start of every day. First, I will request a specific amount of drives. These can range from level 0 drives to level 3. I understand your \"situation\" however, and will not request a drive you can't obtain. The second task I will ask of you is a simple satelite checkup. Go to the satellite, type in \"sv.hash\" into the server, and write down the server and number in a note. For example, \"Bravo 02A421A095\". Once you've done that, attatch it to the lid of a drive box, and send it off with the drone. I've left a little notebook on the table you can tear some pages out of for the notes. As usual, you will get a nice point bonus if you complete these for me. Have a nice day/night wherever you are! - Dr. Bao", false));
        }
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void addEmail(String sender, String title, String text) {
        emails.add(0, new Email(sender, title, text, false)); // Добавляем в начало списка
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }

    public boolean spendPoints(int amount) {
        if (this.points >= amount) {
            this.points -= amount;
            return true;
        }
        return false;
    }

    public int getCursorSpeedLvl() {
        return upgradeCursorSpeed;
    }

    public int getPingCooldownLvl() {
        return upgradePingCooldown;
    }

    public int getProcessingSpeedLvl() {
        return upgradeProcessingSpeed;
    }

    public int getProcessingLevelLvl() {
        return upgradeProcessingLevel;
    }

    public NonNullList<ItemStack> getDeliveryQueue() {
        return deliveryQueue;
    }

    public void addDelivery(ItemStack stack) {
        deliveryQueue.add(stack);
    }

    public boolean buyUpgrade(String type) {
        int cost = 0;
        if (type.equals("cursor_speed") && upgradeCursorSpeed < 16) {
            cost = 5 + (upgradeCursorSpeed * 5);
            if (spendPoints(cost)) {
                upgradeCursorSpeed++;
                return true;
            }
        } else if (type.equals("ping_cooldown") && upgradePingCooldown < 16) {
            cost = 15 + (upgradePingCooldown * 5);
            if (spendPoints(cost)) {
                upgradePingCooldown++;
                return true;
            }
        } else if (type.equals("processing_speed") && upgradeProcessingSpeed < 16) {
            cost = 20 + (upgradeProcessingSpeed * 5);
            if (spendPoints(cost)) {
                upgradeProcessingSpeed++;
                return true;
            }
        } else if (type.equals("processing_level") && upgradeProcessingLevel < 3) {
            cost = 30 + (upgradeProcessingLevel * 20);
            if (spendPoints(cost)) {
                upgradeProcessingLevel++;
                return true;
            }
        }
        return false;
    }

    public int getNextCost(String type) {
        if (type.equals("cursor_speed")) return upgradeCursorSpeed >= 16 ? -1 : 5 + (upgradeCursorSpeed * 5);
        if (type.equals("ping_cooldown")) return upgradePingCooldown >= 16 ? -1 : 15 + (upgradePingCooldown * 5);
        if (type.equals("processing_speed"))
            return upgradeProcessingSpeed >= 16 ? -1 : 20 + (upgradeProcessingSpeed * 5);
        if (type.equals("processing_level"))
            return upgradeProcessingLevel >= 3 ? -1 : 30 + (upgradeProcessingLevel * 20);
        return -1;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("points", points);
        tag.putInt("upg_cursor", upgradeCursorSpeed);
        tag.putInt("upg_ping", upgradePingCooldown);
        tag.putInt("upg_procspeed", upgradeProcessingSpeed);
        tag.putInt("upg_proclvl", upgradeProcessingLevel);

        ListTag queueTag = new ListTag();
        for (ItemStack stack : deliveryQueue) {
            queueTag.add(stack.save(new CompoundTag()));
        }
        tag.put("DeliveryQueue", queueTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.points = tag.getInt("points");
        this.upgradeCursorSpeed = tag.getInt("upg_cursor");
        this.upgradePingCooldown = tag.getInt("upg_ping");
        this.upgradeProcessingSpeed = tag.getInt("upg_procspeed");
        this.upgradeProcessingLevel = tag.getInt("upg_proclvl");

        if (tag.contains("DeliveryQueue")) {
            ListTag queueTag = tag.getList("DeliveryQueue", 10);
            deliveryQueue.clear();
            for (int i = 0; i < queueTag.size(); i++) {
                deliveryQueue.add(ItemStack.of(queueTag.getCompound(i)));
            }
            if (tag.contains("Emails")) {
                ListTag emailsTag = tag.getList("Emails", 10);
                emails.clear();
                for (int i = 0; i < emailsTag.size(); i++) {
                    CompoundTag eTag = emailsTag.getCompound(i);
                    emails.add(new Email(eTag.getString("sender"), eTag.getString("title"), eTag.getString("text"), eTag.getBoolean("read")));
                }
            }
            checkInitEmails();
        }
    }
}