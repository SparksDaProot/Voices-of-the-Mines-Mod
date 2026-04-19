package net.votmdevs.voicesofthemines.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoicesOfTheMines.MOD_ID);


    public static final RegistryObject<CreativeModeTab> VOTM_FOOD =
            CREATIVE_MODE_TABS.register("votm_food", ()-> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BANANA.get()))
                    .title(Component.translatable("creativetab.votm_food"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BANANA.get());
                        output.accept(ModItems.BURGER.get());
                        output.accept(ModItems.CHEESE.get());
                        output.accept(ModItems.TACO.get());
                        output.accept(ModItems.TOBLERONE.get());


                    } ).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
