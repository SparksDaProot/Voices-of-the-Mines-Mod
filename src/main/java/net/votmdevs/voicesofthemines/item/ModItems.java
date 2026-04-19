package net.votmdevs.voicesofthemines.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VoicesOfTheMines.MOD_ID);

    public static final RegistryObject<Item> BANANA = ITEMS.register("banana",
            ()-> new Item(new Item.Properties().food(ModFoods.BANANA)));
    public static final RegistryObject<Item> CHEESE = ITEMS.register("cheese",
            ()-> new Item((new Item.Properties().food(ModFoods.CHEESE))));
    public static final RegistryObject<Item> TACO = ITEMS.register("taco",
            ()-> new Item(new Item.Properties().food(ModFoods.TACO)));
    public static final RegistryObject<Item> TOBLERONE = ITEMS.register("toblerone",
            ()-> new Item(new Item.Properties().food(ModFoods.TOBLERONE)));
    public static final RegistryObject<Item> BURGER = ITEMS.register("burger",
            ()-> new Item(new Item.Properties().food(ModFoods.BURGER)));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
