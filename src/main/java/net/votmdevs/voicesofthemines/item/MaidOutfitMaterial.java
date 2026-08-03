package net.votmdevs.voicesofthemines.item;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;

import java.util.function.Supplier;

public enum MaidOutfitMaterial implements ArmorMaterial {
    MAID("maid", 5, new int[]{1, 7, 3, 1}, 25,
            SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F,
            () -> Ingredient.of(VoicesOfTheMines.MAXWELL_ITEM.get()));

    ;

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int encchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] BASE_DURABILITY = {11, 16, 15, 13};

    MaidOutfitMaterial(String name, int durabilityMultiplier, int[] protectionAmounts, int encchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.encchantmentValue = encchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        int index = type == ArmorItem.Type.HELMET ? 0 : type == ArmorItem.Type.CHESTPLATE ? 1 : type == ArmorItem.Type.LEGGINGS ? 2 : 3;
        return BASE_DURABILITY[index] * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        int index = type == ArmorItem.Type.HELMET ? 0 : type == ArmorItem.Type.CHESTPLATE ? 1 : type == ArmorItem.Type.LEGGINGS ? 2 : 3;
        return this.protectionAmounts[index];
    }

    @Override
    public int getEnchantmentValue() {
        return this.encchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return VoicesOfTheMines.MODID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
