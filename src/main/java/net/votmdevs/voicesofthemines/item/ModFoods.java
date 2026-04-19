package net.votmdevs.voicesofthemines.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {

    public static final FoodProperties BANANA = new FoodProperties.Builder()
            .nutrition(4).saturationMod(2.4f).build();
    public static final FoodProperties CHEESE = new FoodProperties.Builder()
            .nutrition(5).saturationMod(6f).build();
    public static final FoodProperties TACO = new FoodProperties.Builder()
            .nutrition(8).saturationMod(12.8f).build();
    public static final FoodProperties TOBLERONE = new FoodProperties.Builder()
            .nutrition(3).saturationMod(3f)
            .effect(()-> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1), 1).build();
    public static final FoodProperties BURGER = new FoodProperties.Builder()
            .nutrition(8).saturationMod(12.8f).build();
}
