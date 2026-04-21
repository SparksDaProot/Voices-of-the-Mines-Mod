package net.votmdevs.voicesofthemines.world;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerData implements INBTSerializable<CompoundTag> {

    private int upgradeCursorSpeed = 0;
    private int upgradePingCooldown = 0;
    private int upgradeProcessingSpeed = 0;
    private int upgradeProcessingLevel = 0;

    private final Map<UUID, Integer> playerPoints = new HashMap<>();
    private final Map<UUID, NonNullList<ItemStack>> playerDeliveryQueues = new HashMap<>();
    private final Map<UUID, List<Email>> playerEmails = new HashMap<>();

    private final Map<String, UUID> usernameCache = new HashMap<>();
    public final List<CustomLot> customMarket = new ArrayList<>();

    public static class CustomLot {
        public String lotId;
        public UUID sellerId;
        public ItemStack stack;
        public int price;
        public CustomLot(String lotId, UUID sellerId, ItemStack stack, int price) {
            this.lotId = lotId; this.sellerId = sellerId; this.stack = stack; this.price = price;
        }
    }

    public static class Email {
        public String sender;
        public String title;
        public String text;
        public boolean isRead;

        public Email(String sender, String title, String text, boolean isRead) {
            this.sender = sender; this.title = title; this.text = text; this.isRead = isRead;
        }
    }

    public PlayerData() {}

    public void initPlayerIfNeeded(UUID uuid, String username) {
        usernameCache.put(username, uuid); // Запоминаем ник
        if (!playerPoints.containsKey(uuid)) playerPoints.put(uuid, 0);
        if (!playerDeliveryQueues.containsKey(uuid)) playerDeliveryQueues.put(uuid, NonNullList.create());

        if (!playerEmails.containsKey(uuid)) {
            List<Email> startingEmails = new ArrayList<>();
            startingEmails.add(new Email("Prof_Lea", "Welcome!", "Welcome to your new job...", false));
            startingEmails.add(new Email("Dr Bao", "Hi", "Hello! I am Dr. Bao. I will be responsible...", false));
            playerEmails.put(uuid, startingEmails);
        }
    }

    public UUID getUUIDByName(String name) { return usernameCache.get(name); }

    public int getPoints(UUID uuid) { return playerPoints.getOrDefault(uuid, 0); }
    public void addPoints(UUID uuid, int amount) {
        if(playerPoints.containsKey(uuid)) playerPoints.put(uuid, playerPoints.get(uuid) + amount);
    }
    public boolean spendPoints(UUID uuid, int amount) {
        if (!playerPoints.containsKey(uuid)) return false;
        int current = playerPoints.get(uuid);
        if (current >= amount) { playerPoints.put(uuid, current - amount); return true; }
        return false;
    }

    public NonNullList<ItemStack> getDeliveryQueue(UUID uuid) { return playerDeliveryQueues.getOrDefault(uuid, NonNullList.create()); }
    public void addDelivery(UUID uuid, ItemStack stack) { if (playerDeliveryQueues.containsKey(uuid)) playerDeliveryQueues.get(uuid).add(stack); }

    public List<Email> getEmails(UUID uuid) { return playerEmails.getOrDefault(uuid, new ArrayList<>()); }
    public void addEmail(UUID uuid, String sender, String title, String text) {
        if (playerEmails.containsKey(uuid)) playerEmails.get(uuid).add(0, new Email(sender, title, text, false));
    }

    public void broadcastEmail(String sender, String title, String text) {
        for (UUID uuid : playerEmails.keySet()) playerEmails.get(uuid).add(0, new Email(sender, title, text, false));
    }

    public int getCursorSpeedLvl() { return upgradeCursorSpeed; }
    public int getPingCooldownLvl() { return upgradePingCooldown; }
    public int getProcessingSpeedLvl() { return upgradeProcessingSpeed; }
    public int getProcessingLevelLvl() { return upgradeProcessingLevel; }

    public boolean buyUpgrade(UUID uuid, String type) {
        int cost = getNextCost(type);
        if (cost != -1 && spendPoints(uuid, cost)) {
            if (type.equals("cursor_speed")) upgradeCursorSpeed++;
            else if (type.equals("ping_cooldown")) upgradePingCooldown++;
            else if (type.equals("processing_speed")) upgradeProcessingSpeed++;
            else if (type.equals("processing_level")) upgradeProcessingLevel++;
            return true;
        }
        return false;
    }

    public int getNextCost(String type) {
        if (type.equals("cursor_speed")) return upgradeCursorSpeed >= 16 ? -1 : 5 + (upgradeCursorSpeed * 5);
        if (type.equals("ping_cooldown")) return upgradePingCooldown >= 16 ? -1 : 15 + (upgradePingCooldown * 5);
        if (type.equals("processing_speed")) return upgradeProcessingSpeed >= 16 ? -1 : 20 + (upgradeProcessingSpeed * 5);
        if (type.equals("processing_level")) return upgradeProcessingLevel >= 3 ? -1 : 30 + (upgradeProcessingLevel * 20);
        return -1;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("upg_cursor", upgradeCursorSpeed);
        tag.putInt("upg_ping", upgradePingCooldown);
        tag.putInt("upg_procspeed", upgradeProcessingSpeed);
        tag.putInt("upg_proclvl", upgradeProcessingLevel);

        ListTag playersList = new ListTag();
        for (UUID uuid : playerPoints.keySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("uuid", uuid);
            pTag.putInt("points", playerPoints.get(uuid));

            ListTag queueTag = new ListTag();
            for (ItemStack stack : playerDeliveryQueues.get(uuid)) queueTag.add(stack.save(new CompoundTag()));
            pTag.put("DeliveryQueue", queueTag);

            ListTag emailsTag = new ListTag();
            for (Email e : playerEmails.get(uuid)) {
                CompoundTag eTag = new CompoundTag();
                eTag.putString("sender", e.sender); eTag.putString("title", e.title);
                eTag.putString("text", e.text); eTag.putBoolean("read", e.isRead);
                emailsTag.add(eTag);
            }
            pTag.put("Emails", emailsTag);
            playersList.add(pTag);
        }
        tag.put("Players", playersList);

        CompoundTag namesTag = new CompoundTag();
        for (Map.Entry<String, UUID> entry : usernameCache.entrySet()) namesTag.putUUID(entry.getKey(), entry.getValue());
        tag.put("Usernames", namesTag);

        ListTag marketTag = new ListTag();
        for (CustomLot lot : customMarket) {
            CompoundTag lTag = new CompoundTag();
            lTag.putString("id", lot.lotId); lTag.putUUID("seller", lot.sellerId);
            lTag.put("item", lot.stack.save(new CompoundTag())); lTag.putInt("price", lot.price);
            marketTag.add(lTag);
        }
        tag.put("Market", marketTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.upgradeCursorSpeed = tag.getInt("upg_cursor"); this.upgradePingCooldown = tag.getInt("upg_ping");
        this.upgradeProcessingSpeed = tag.getInt("upg_procspeed"); this.upgradeProcessingLevel = tag.getInt("upg_proclvl");

        playerPoints.clear(); playerDeliveryQueues.clear(); playerEmails.clear(); usernameCache.clear(); customMarket.clear();

        if (tag.contains("Players")) {
            ListTag playersList = tag.getList("Players", 10);
            for (int i = 0; i < playersList.size(); i++) {
                CompoundTag pTag = playersList.getCompound(i);
                UUID uuid = pTag.getUUID("uuid");
                playerPoints.put(uuid, pTag.getInt("points"));

                NonNullList<ItemStack> queue = NonNullList.create();
                ListTag queueTag = pTag.getList("DeliveryQueue", 10);
                for (int q = 0; q < queueTag.size(); q++) queue.add(ItemStack.of(queueTag.getCompound(q)));
                playerDeliveryQueues.put(uuid, queue);

                List<Email> emails = new ArrayList<>();
                ListTag emailsTag = pTag.getList("Emails", 10);
                for (int e = 0; e < emailsTag.size(); e++) {
                    CompoundTag eTag = emailsTag.getCompound(e);
                    emails.add(new Email(eTag.getString("sender"), eTag.getString("title"), eTag.getString("text"), eTag.getBoolean("read")));
                }
                playerEmails.put(uuid, emails);
            }
        }
        if (tag.contains("Usernames")) {
            CompoundTag namesTag = tag.getCompound("Usernames");
            for (String key : namesTag.getAllKeys()) usernameCache.put(key, namesTag.getUUID(key));
        }
        if (tag.contains("Market")) {
            ListTag marketTag = tag.getList("Market", 10);
            for (int i = 0; i < marketTag.size(); i++) {
                CompoundTag lTag = marketTag.getCompound(i);
                customMarket.add(new CustomLot(lTag.getString("id"), lTag.getUUID("seller"), ItemStack.of(lTag.getCompound("item")), lTag.getInt("price")));
            }
        }
    }
}