package net.votmdevs.voicesofthemines.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerData implements INBTSerializable<CompoundTag> {
    private int points = 0; // Наша валюта

    // Upgrades
    private int upgradeCursorSpeed = 0; // max 16
    private int upgradePingCooldown = 0; // max 16
    private int upgradeProcessingSpeed = 0; // max 16
    private int upgradeProcessingLevel = 0; // max 3

    public int getPoints() { return points; }
    public void addPoints(int amount) { this.points += amount; }
    public boolean spendPoints(int amount) {
        if (this.points >= amount) {
            this.points -= amount;
            return true;
        }
        return false;
    }

    public int getCursorSpeedLvl() { return upgradeCursorSpeed; }
    public int getPingCooldownLvl() { return upgradePingCooldown; }
    public int getProcessingSpeedLvl() { return upgradeProcessingSpeed; }
    public int getProcessingLevelLvl() { return upgradeProcessingLevel; }

    // bug upgrades
    public boolean buyUpgrade(String type) {
        int cost = 0;
        if (type.equals("cursor_speed") && upgradeCursorSpeed < 16) {
            cost = 5 + (upgradeCursorSpeed * 5);
            if (spendPoints(cost)) { upgradeCursorSpeed++; return true; }
        }
        else if (type.equals("ping_cooldown") && upgradePingCooldown < 16) {
            cost = 15 + (upgradePingCooldown * 5);
            if (spendPoints(cost)) { upgradePingCooldown++; return true; }
        }
        else if (type.equals("processing_speed") && upgradeProcessingSpeed < 16) {
            cost = 20 + (upgradeProcessingSpeed * 5);
            if (spendPoints(cost)) { upgradeProcessingSpeed++; return true; }
        }
        else if (type.equals("processing_level") && upgradeProcessingLevel < 3) {
            cost = 30 + (upgradeProcessingLevel * 20);
            if (spendPoints(cost)) { upgradeProcessingLevel++; return true; }
        }
        return false;
    }

    // price
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
        tag.putInt("points", points);
        tag.putInt("upg_cursor", upgradeCursorSpeed);
        tag.putInt("upg_ping", upgradePingCooldown);
        tag.putInt("upg_procspeed", upgradeProcessingSpeed);
        tag.putInt("upg_proclvl", upgradeProcessingLevel);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.points = tag.getInt("points");
        this.upgradeCursorSpeed = tag.getInt("upg_cursor");
        this.upgradePingCooldown = tag.getInt("upg_ping");
        this.upgradeProcessingSpeed = tag.getInt("upg_procspeed");
        this.upgradeProcessingLevel = tag.getInt("upg_proclvl");
    }
}