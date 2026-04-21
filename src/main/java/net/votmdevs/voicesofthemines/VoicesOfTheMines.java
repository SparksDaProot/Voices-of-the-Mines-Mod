package net.votmdevs.voicesofthemines;

import net.votmdevs.voicesofthemines.block.KerfurWorkbenchBlock;
import net.votmdevs.voicesofthemines.block.PosterBlockEntity;
import net.votmdevs.voicesofthemines.block.TrashBinBlock;
import net.votmdevs.voicesofthemines.client.*;
import net.votmdevs.voicesofthemines.effect.RadiationEffect;
import net.votmdevs.voicesofthemines.entity.*;
import net.votmdevs.voicesofthemines.inventory.KerfurMenu;
import net.votmdevs.voicesofthemines.item.HazardArmorMaterial;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

@Mod(VoicesOfTheMines.MODID)
public class VoicesOfTheMines {
    public static final String MODID = "voicesofthemines";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public static final RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", RadiationEffect::new);

    public static final RegistryObject<EntityType<CockroachEntity>> COCKROACH = ENTITY_TYPES.register("cockroach",
            () -> EntityType.Builder.of(CockroachEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.1f)
                    .build(new ResourceLocation(MODID, "cockroach").toString()));

    public static final RegistryObject<Item> COCKROACH_SPAWN_EGG = ITEMS.register("cockroach_spawn_egg",
            () -> new ForgeSpawnEggItem(COCKROACH, 0x4B3A2A, 0x2A1F16, new Item.Properties()));

    public static final RegistryObject<EntityType<MaxwellEntity>> MAXWELL = ENTITY_TYPES.register("maxwell",
            () -> EntityType.Builder.of(MaxwellEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.5f)
                    .build(new ResourceLocation(MODID, "maxwell").toString()));

    public static final RegistryObject<Item> MAXWELL_ITEM = ITEMS.register("maxwell_item",
            () -> new Item(new Item.Properties()) {
                @Override
                public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
                    Level level = context.getLevel();
                    if (!level.isClientSide) {
                        MaxwellEntity maxwell = MAXWELL.get().create(level);
                        if (maxwell != null) {
                            maxwell.moveTo(context.getClickLocation().x, context.getClickLocation().y, context.getClickLocation().z, context.getPlayer() != null ? context.getPlayer().getYRot() : 0, 0.0F);
                            level.addFreshEntity(maxwell);
                            context.getItemInHand().shrink(1);
                            return InteractionResult.SUCCESS;
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            });

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<MenuType<KerfurMenu>> KERFUR_MENU = MENUS.register("kerfur_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                String color = data.readUtf();
                boolean isOmega = data.readBoolean();
                return new KerfurMenu(windowId, inv, new net.minecraft.world.SimpleContainer(isOmega ? 54 : 27), color, isOmega);
            }));

    public static final RegistryObject<MenuType<net.votmdevs.voicesofthemines.inventory.DroneMenu>> DRONE_MENU = MENUS.register("drone_menu",
            () -> net.minecraftforge.common.extensions.IForgeMenuType.create((windowId, inv, data) -> {
                int entityId = data.readInt();
                net.minecraft.world.entity.Entity entity = inv.player.level().getEntity(entityId);
                if (entity instanceof net.votmdevs.voicesofthemines.entity.DroneEntity drone) {
                    return new net.votmdevs.voicesofthemines.inventory.DroneMenu(windowId, inv, drone.inventory);
                }
                return new net.votmdevs.voicesofthemines.inventory.DroneMenu(windowId, inv, new net.minecraft.world.SimpleContainer(27));
            }));

    public static final RegistryObject<Block> KERFUR_WORKBENCH = BLOCKS.register("kerfur_workbench",
            () -> new KerfurWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(5.0F, 6.0F).noOcclusion()));

//TERMINALS

    // WALL
    public static final RegistryObject<Block> WALL_BEHIND = BLOCKS.register("wall_behind",
            () -> new net.votmdevs.voicesofthemines.block.VotvTerminalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(),
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 14.0D, 16.0D), // North (Теперь вытянут по оси X)
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 14.0D, 32.0D), // East (Теперь вытянут по оси Z)
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 14.0D, 16.0D), // South
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 14.0D, 32.0D)  // West
            ));

    // TERMINAL FIND
    public static final RegistryObject<Block> TERMINAL_FIND = BLOCKS.register("terminal_find",
            () -> new net.votmdevs.voicesofthemines.block.VotvTerminalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(),
                    Block.box(0.0D, 0.0D, -4.0D, 16.0D, 37.0D, 32.0D), // North
                    Block.box(-4.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D), // East
                    Block.box(0.0D, 0.0D, -4.0D, 16.0D, 37.0D, 32.0D), // South
                    Block.box(-4.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D)  // West
            ));

    // TERMINAL CHECK
    public static final RegistryObject<Block> TERMINAL_CHECK = BLOCKS.register("terminal_check",
            () -> new net.votmdevs.voicesofthemines.block.VotvTerminalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(),
                    Block.box(0.0D, 0.0D, -4.0D, 16.0D, 37.0D, 32.0D), // North
                    Block.box(-4.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D), // East
                    Block.box(0.0D, 0.0D, -4.0D, 16.0D, 37.0D, 32.0D), // South
                    Block.box(-4.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D)  // West
            ));

    // Table?
    public static final RegistryObject<Block> TABLE = BLOCKS.register("table",
            () -> new net.votmdevs.voicesofthemines.block.VotvTerminalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(),
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 14.0D, 16.0D), // North (Теперь вытянут по оси X)
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 14.0D, 32.0D), // East (Теперь вытянут по оси Z)
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 14.0D, 16.0D), // South
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 14.0D, 32.0D)  // West
            ));

    // hitboxes
    public static final RegistryObject<Block> TERMINAL_PROCESSING = BLOCKS.register("terminal_processing",
            () -> new net.votmdevs.voicesofthemines.block.VotvTerminalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(),
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 37.0D, 32.0D), // North
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D), // East
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 37.0D, 32.0D), // South
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D)  // West
            ));

    public static final RegistryObject<Block> TERMINAL_CALIBRATE = BLOCKS.register("terminal_calibrate",
            () -> new net.votmdevs.voicesofthemines.block.VotvTerminalBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(),
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 37.0D, 32.0D), // North
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D), // East
                    Block.box(0.0D, 0.0D, -16.0D, 16.0D, 37.0D, 32.0D), // South
                    Block.box(-16.0D, 0.0D, 0.0D, 32.0D, 37.0D, 16.0D)  // West
            ));

    public static final RegistryObject<Block> PHANTOM_BLOCK = BLOCKS.register("phantom_block",
            () -> new net.votmdevs.voicesofthemines.block.VotvPhantomBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noOcclusion()));



// Food

    public static final RegistryObject<Item> BANANA = ITEMS.register("banana",
            ()-> new Item(new Item.Properties().food(net.votmdevs.voicesofthemines.item.ModFoods.BANANA)));
    public static final RegistryObject<Item> CHEESE = ITEMS.register("cheese",
            ()-> new Item((new Item.Properties().food(net.votmdevs.voicesofthemines.item.ModFoods.CHEESE))));
    public static final RegistryObject<Item> TACO = ITEMS.register("taco",
            ()-> new Item(new Item.Properties().food(net.votmdevs.voicesofthemines.item.ModFoods.TACO)));
    public static final RegistryObject<Item> TOBLERONE = ITEMS.register("toblerone",
            ()-> new Item(new Item.Properties().food(net.votmdevs.voicesofthemines.item.ModFoods.TOBLERONE)));
    public static final RegistryObject<Item> BURGER = ITEMS.register("burger",
            ()-> new Item(new Item.Properties().food(net.votmdevs.voicesofthemines.item.ModFoods.BURGER)));

    // term items
    public static final RegistryObject<Item> TABLE_ITEM = ITEMS.register("table", () -> new BlockItem(TABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> WALL_BEHIND_ITEM = ITEMS.register("wall_behind", () -> new BlockItem(WALL_BEHIND.get(), new Item.Properties()));
    public static final RegistryObject<Item> TERMINAL_FIND_ITEM = ITEMS.register("terminal_find", () -> new BlockItem(TERMINAL_FIND.get(), new Item.Properties()));
    public static final RegistryObject<Item> TERMINAL_PROCESSING_ITEM = ITEMS.register("terminal_processing", () -> new BlockItem(TERMINAL_PROCESSING.get(), new Item.Properties()));
    public static final RegistryObject<Item> TERMINAL_CHECK_ITEM = ITEMS.register("terminal_check", () -> new BlockItem(TERMINAL_CHECK.get(), new Item.Properties()));
    public static final RegistryObject<Item> TERMINAL_CALIBRATE_ITEM = ITEMS.register("terminal_calibrate", () -> new BlockItem(TERMINAL_CALIBRATE.get(), new Item.Properties()));

    public static final RegistryObject<Item> ACCESSORY_MAID = ITEMS.register("maid", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACCESSORY_RIBBON = ITEMS.register("ribbon", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACCESSORY_GLASSES = ITEMS.register("glasses", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACCESSORY_JACKET = ITEMS.register("jacket", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TRASH_BAG = ITEMS.register("trash_bag", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRASH_ROLL = ITEMS.register("trash_roll", () -> new Item(new Item.Properties().defaultDurability(10)));

    public static final RegistryObject<Block> POSTER = BLOCKS.register("poster",
            () -> new net.votmdevs.voicesofthemines.block.PosterBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).instabreak().noCollission().noOcclusion()));

    public static final RegistryObject<Block> KEYPAD = BLOCKS.register("keypad",
            () -> new net.votmdevs.voicesofthemines.block.KeypadBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> KEYPAD_ITEM = ITEMS.register("keypad_item",
            () -> new BlockItem(KEYPAD.get(), new Item.Properties()));

    public static final RegistryObject<Item> POSTER_ITEM = ITEMS.register("poster",
            () -> new BlockItem(POSTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> KERFUR_WORKBENCH_ITEM = ITEMS.register("kerfur_workbench",
            () -> new BlockItem(KERFUR_WORKBENCH.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOTV_DOOR = BLOCKS.register("votv_door",
            () -> new net.votmdevs.voicesofthemines.block.VotvDoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> VOTV_DOOR_ITEM = ITEMS.register("votv_door",
            () -> new BlockItem(VOTV_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<EntityType<OmegaKerfurEntity>> OMEGA_KERFUR = ENTITY_TYPES.register("omega_kerfur",
            () -> EntityType.Builder.of(OmegaKerfurEntity::new, MobCategory.CREATURE)
                    .sized(0.8f, 2.2f)
                    .build(new ResourceLocation(MODID, "omega_kerfur").toString()));

    public static final RegistryObject<Item> OMEGA_KERFUR_SPAWN_EGG = ITEMS.register("omega_kerfur_spawn_egg",
            () -> new ForgeSpawnEggItem(OMEGA_KERFUR, 0x3F3F3F, 0x00FF00, new Item.Properties()));

    public static final RegistryObject<Block> TRASH_BIN = BLOCKS.register("trash_bin",
            () -> new TrashBinBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(3.0F, 3.0F).noOcclusion()));

    public static final RegistryObject<Item> TRASH_BIN_ITEM = ITEMS.register("trash_bin",
            () -> new BlockItem(TRASH_BIN.get(), new Item.Properties()));

    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<PosterBlockEntity>> POSTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("poster_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.PosterBlockEntity::new, POSTER.get()).build(null));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.KeypadBlockEntity>> KEYPAD_BLOCK_ENTITY = BLOCK_ENTITIES.register("keypad_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.KeypadBlockEntity::new, KEYPAD.get()).build(null));

    public static final RegistryObject<Item> HOOK_ITEM = ITEMS.register("hook",
            () -> new net.votmdevs.voicesofthemines.item.HookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HOOK_PART = ITEMS.register("hook_last_part", () -> new Item(new Item.Properties()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.VotvDoorBlockEntity>> VOTV_DOOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("votv_door_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.VotvDoorBlockEntity::new, VOTV_DOOR.get()).build(null));


    public static final RegistryObject<EntityType<AtvEntity>> ATV = ENTITY_TYPES.register("atv",
            () -> EntityType.Builder.of(AtvEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.2f)
                    .build(new ResourceLocation(MODID, "atv").toString()));

    public static final RegistryObject<Item> ATV_SPAWN_EGG = ITEMS.register("atv_spawn_egg",
            () -> new ForgeSpawnEggItem(ATV, 0x555555, 0x111111, new Item.Properties()));

    public static final RegistryObject<EntityType<net.votmdevs.voicesofthemines.entity.HookEntity>> HOOK_ENTITY = ENTITY_TYPES.register("hook_entity",
            () -> EntityType.Builder.<net.votmdevs.voicesofthemines.entity.HookEntity>of(net.votmdevs.voicesofthemines.entity.HookEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(20)
                    .build(new ResourceLocation(MODID, "hook_entity").toString()));

    public static final RegistryObject<EntityType<FleshEntity>> FLESH = ENTITY_TYPES.register("flesh",
            () -> EntityType.Builder.of(FleshEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(new ResourceLocation(MODID, "flesh").toString()));

    public static final RegistryObject<Item> FLESH_SPAWN_EGG = ITEMS.register("flesh_spawn_egg",
            () -> new ForgeSpawnEggItem(FLESH, 0x880000, 0x440000, new Item.Properties()));

    public static final RegistryObject<EntityType<FuelCanEntity>> FUEL_CAN = ENTITY_TYPES.register("fuel_can",
            () -> EntityType.Builder.of(FuelCanEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(new ResourceLocation(MODID, "fuel_can").toString()));

    public static final RegistryObject<Item> FUEL_CAN_SPAWN_EGG = ITEMS.register("fuel_can_spawn_egg",
            () -> new ForgeSpawnEggItem(FUEL_CAN, 0x880000, 0x440000, new Item.Properties()));

    public static final RegistryObject<EntityType<GarbageEntity>> GARBAGE = ENTITY_TYPES.register("garbage",
            () -> EntityType.Builder.of(GarbageEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(new ResourceLocation(MODID, "garbage").toString()));

    public static final RegistryObject<EntityType<DriveEntity>> DRIVE = ENTITY_TYPES.register("drive",
            () -> EntityType.Builder.of(DriveEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.1f) // Плоский хитбокс
                    .build(new ResourceLocation(MODID, "drive").toString()));

    public static final RegistryObject<Item> DRIVE_SPAWN_EGG = ITEMS.register("drive_spawn_egg",
            () -> new ForgeSpawnEggItem(DRIVE, 0x111111, 0x555555, new Item.Properties()));


    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity>> TERMINAL_BE = BLOCK_ENTITIES.register("terminal_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity::new,
                    TABLE.get(), WALL_BEHIND.get(), TERMINAL_FIND.get(), TERMINAL_PROCESSING.get(), TERMINAL_CHECK.get(), TERMINAL_CALIBRATE.get()
            ).build(null));

    public static final RegistryObject<Item> GARBAGE_SPAWN_EGG = ITEMS.register("garbage_spawn_egg",
            () -> new ForgeSpawnEggItem(GARBAGE, 0x880000, 0x440000, new Item.Properties()));

    public static final RegistryObject<EntityType<BloodSplashEntity>> BLOOD_SPLASH = ENTITY_TYPES.register("blood_splash",
            () -> EntityType.Builder.of(BloodSplashEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.1f)
                    .build(new ResourceLocation(MODID, "blood_splash").toString()));

    public static final RegistryObject<Block> BOOK_RECIPE = BLOCKS.register("book_kerfur_recipe",
            () -> new net.votmdevs.voicesofthemines.block.BookRecipeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).instabreak().noOcclusion()));

    public static final RegistryObject<Item> BOOK_RECIPE_ITEM = ITEMS.register("book_kerfur_recipe",
            () -> new BlockItem(BOOK_RECIPE.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    super.appendHoverText(stack, level, tooltip, flag);
                    tooltip.add(Component.literal("Kerfur-O recipe:").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Kerfur part x4").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Recycled Plastic x2").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Recycled Rubber").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Metal Scrap").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Electronic Waste x2").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Radioactive Capsule").withStyle(net.minecraft.ChatFormatting.GOLD));
                    tooltip.add(Component.literal("- Paint").withStyle(net.minecraft.ChatFormatting.GOLD));
                }
            });

    public static final RegistryObject<Item> RADIOACTIVE_CAPSULE = ITEMS.register("radioactive_capsule", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELECTRONIC_WASTE = ITEMS.register("electronic_waste", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RECYCLED_RUBBER = ITEMS.register("recycled_rubber", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> METAL_SCRAP = ITEMS.register("metal_scrap", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RECYCLED_PLASTIC = ITEMS.register("recycled_plastic", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> KERFUR_PART = ITEMS.register("kerfur_part", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_BLACK = ITEMS.register("painter_black", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_BLUE = ITEMS.register("painter_blue", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_GREEN = ITEMS.register("painter_green", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_PINK = ITEMS.register("painter_pink", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_RED = ITEMS.register("painter_red", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_WHITE = ITEMS.register("painter_white", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAINTER_YELLOW = ITEMS.register("painter_yellow", () -> new Item(new Item.Properties()));


    public static final RegistryObject<Item> HAZARD_HELMET = ITEMS.register("hazard_helmet", () -> new ArmorItem(HazardArmorMaterial.HAZARD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> HAZARD_CHESTPLATE = ITEMS.register("hazard_chestplate", () -> new ArmorItem(HazardArmorMaterial.HAZARD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> HAZARD_LEGGINGS = ITEMS.register("hazard_leggings", () -> new ArmorItem(HazardArmorMaterial.HAZARD, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> HAZARD_BOOTS = ITEMS.register("hazard_boots", () -> new ArmorItem(HazardArmorMaterial.HAZARD, ArmorItem.Type.BOOTS, new Item.Properties()));


    public static final RegistryObject<Item> DISK_BLUE = ITEMS.register("disk_blue",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Block> SERVER_BLOCK = BLOCKS.register("server_block",
            () -> new net.votmdevs.voicesofthemines.block.ServerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> SERVER_BLOCK_ITEM = ITEMS.register("server_block",
            () -> new BlockItem(SERVER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.ServerBlockEntity>> SERVER_BE = BLOCK_ENTITIES.register("server_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.ServerBlockEntity::new, SERVER_BLOCK.get()).build(null));

    public static final RegistryObject<Block> CONSOLE_BLOCK = BLOCKS.register("console_block",
            () -> new net.votmdevs.voicesofthemines.block.ConsoleBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> CONSOLE_BLOCK_ITEM = ITEMS.register("console_block",
            () -> new BlockItem(CONSOLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.ConsoleBlockEntity>> CONSOLE_BE = BLOCK_ENTITIES.register("console_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.ConsoleBlockEntity::new, CONSOLE_BLOCK.get()).build(null));

    // drone


    public static final RegistryObject<Block> DRONE_TARGET = BLOCKS.register("target_drone_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion().noCollission()));

    public static final RegistryObject<Item> DRONE_TARGET_ITEM = ITEMS.register("target_drone_block",
            () -> new BlockItem(DRONE_TARGET.get(), new Item.Properties()));

    public static final RegistryObject<Block> DRONE_PANEL = BLOCKS.register("drone_panel",
            () -> new net.votmdevs.voicesofthemines.block.DronePanelBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> DRONE_PANEL_ITEM = ITEMS.register("drone_panel",
            () -> new BlockItem(DRONE_PANEL.get(), new Item.Properties()));

    public static final RegistryObject<EntityType<net.votmdevs.voicesofthemines.entity.DroneEntity>> DRONE = ENTITY_TYPES.register("drone",
            () -> EntityType.Builder.of(net.votmdevs.voicesofthemines.entity.DroneEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.0f)
                    .clientTrackingRange(128)
                    .build(new ResourceLocation(MODID, "drone").toString()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.DronePanelBlockEntity>> DRONE_PANEL_BE = BLOCK_ENTITIES.register("drone_panel_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.DronePanelBlockEntity::new, DRONE_PANEL.get()).build(null));

    public static final RegistryObject<EntityType<KerfurEntity>> KERFUR = ENTITY_TYPES.register("kerfur",
            () -> EntityType.Builder.of(KerfurEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .build(new ResourceLocation(MODID, "kerfur").toString()));

    public static final RegistryObject<Item> KERFUR_SPAWN_EGG = ITEMS.register("kerfur_spawn_egg",
            () -> new ForgeSpawnEggItem(KERFUR, 0xFFFFFF, 0x000000, new Item.Properties()));

    public VoicesOfTheMines(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        VotmSounds.SOUNDS.register(modEventBus);
        MENUS.register(modEventBus);
        EFFECTS.register(modEventBus);
        KerfurPacketHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(KERFUR_WORKBENCH_ITEM);
            event.accept(TRASH_BIN_ITEM);
            event.accept(POSTER_ITEM);
            event.accept(KEYPAD_ITEM);
            event.accept(VOTV_DOOR_ITEM);
            event.accept(TABLE_ITEM);
            event.accept(WALL_BEHIND_ITEM);
            event.accept(TERMINAL_FIND_ITEM);
            event.accept(TERMINAL_PROCESSING_ITEM);
            event.accept(TERMINAL_CHECK_ITEM);
            event.accept(TERMINAL_CALIBRATE_ITEM);
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(HAZARD_HELMET);
            event.accept(HAZARD_CHESTPLATE);
            event.accept(HAZARD_LEGGINGS);
            event.accept(HAZARD_BOOTS);
        }

        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(BANANA);
            event.accept(BURGER);
            event.accept(TOBLERONE);
            event.accept(TACO);
            event.accept(CHEESE);
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(BOOK_RECIPE_ITEM);
            event.accept(RECYCLED_PLASTIC);
            event.accept(RECYCLED_RUBBER);
            event.accept(METAL_SCRAP);
            event.accept(ELECTRONIC_WASTE);
            event.accept(RADIOACTIVE_CAPSULE);
            event.accept(ACCESSORY_MAID);
            event.accept(ACCESSORY_RIBBON);
            event.accept(ACCESSORY_GLASSES);
            event.accept(ACCESSORY_JACKET);

            event.accept(KERFUR_PART);
            event.accept(PAINTER_BLACK);
            event.accept(PAINTER_BLUE);
            event.accept(PAINTER_GREEN);
            event.accept(PAINTER_PINK);
            event.accept(PAINTER_RED);
            event.accept(PAINTER_WHITE);
            event.accept(PAINTER_YELLOW);
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(KERFUR_SPAWN_EGG);
            event.accept(OMEGA_KERFUR_SPAWN_EGG);
            event.accept(FLESH_SPAWN_EGG);
            event.accept(ATV_SPAWN_EGG);
            event.accept(DRIVE_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TRASH_BAG);
            event.accept(TRASH_ROLL);
            event.accept(HOOK_ITEM);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Kerfur Mod Initialized!");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void onAttributeCreate(EntityAttributeCreationEvent event) {
            event.put(KERFUR.get(), KerfurEntity.createAttributes().build());
            event.put(FLESH.get(), FleshEntity.createAttributes().build());
            event.put(BLOOD_SPLASH.get(), BloodSplashEntity.createAttributes().build());
            event.put(OMEGA_KERFUR.get(), OmegaKerfurEntity.createAttributes().build());
            event.put(COCKROACH.get(), CockroachEntity.createAttributes().build());
            event.put(GARBAGE.get(), GarbageEntity.createAttributes().build());
            event.put(ATV.get(), AtvEntity.createAttributes().build());
            event.put(MAXWELL.get(), MaxwellEntity.createAttributes().build());
            event.put(FUEL_CAN.get(), FuelCanEntity.createAttributes().build());
            event.put(DRIVE.get(), DriveEntity.createAttributes().build());
            event.put(DRONE.get(), DroneEntity.createAttributes().build());
        }

        @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
        public static class ClientModEvents {
            @SubscribeEvent
            public static void onClientSetup(FMLClientSetupEvent event) {
                LOGGER.info("Kerfur Client Setup Done!");
                event.enqueueWork(() -> {
                    MenuScreens.register(KERFUR_MENU.get(), KerfurScreen::new);
                    MenuScreens.register(DRONE_MENU.get(), net.votmdevs.voicesofthemines.client.gui.DroneScreen::new);


                    net.minecraft.client.renderer.item.ItemProperties.register(VoicesOfTheMines.HOOK_ITEM.get(), new ResourceLocation(VoicesOfTheMines.MODID, "active"),
                            (stack, level, entity, seed) -> {

                                return stack.hasTag() && stack.getTag().getBoolean("Active") ? 1.0F : 0.0F;
                            });
                });
            }


            @SubscribeEvent
            public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
                event.registerBlockEntityRenderer(SERVER_BE.get(), ServerRenderer::new);
                event.registerEntityRenderer(KERFUR.get(), KerfurRenderer::new);
                event.registerEntityRenderer(FLESH.get(), FleshRenderer::new);
                event.registerBlockEntityRenderer(CONSOLE_BE.get(), ConsoleRenderer::new);
                event.registerEntityRenderer(COCKROACH.get(), CockroachRenderer::new);
                event.registerEntityRenderer(OMEGA_KERFUR.get(), OmegaKerfurRenderer::new);
                event.registerBlockEntityRenderer(POSTER_BLOCK_ENTITY.get(), PosterRenderer::new);
                event.registerEntityRenderer(GARBAGE.get(), net.votmdevs.voicesofthemines.client.GarbageRenderer::new);
                event.registerEntityRenderer(BLOOD_SPLASH.get(), BloodSplashRenderer::new);
                event.registerEntityRenderer(MAXWELL.get(), MaxwellRenderer::new);
                event.registerBlockEntityRenderer(KEYPAD_BLOCK_ENTITY.get(), KeypadRenderer::new);
                event.registerEntityRenderer(HOOK_ENTITY.get(), HookRenderer::new);
                event.registerEntityRenderer(ATV.get(), AtvRenderer::new);
                event.registerEntityRenderer(FUEL_CAN.get(), FuelCanRenderer::new);
                event.registerEntityRenderer(DRIVE.get(), DriveRenderer::new);
                event.registerBlockEntityRenderer(TERMINAL_BE.get(), VotvTerminalRenderer::new);
                event.registerEntityRenderer(DRONE.get(), DroneRenderer::new);
                event.registerBlockEntityRenderer(DRONE_PANEL_BE.get(), DronePanelRenderer::new);
                event.registerBlockEntityRenderer(VOTV_DOOR_BLOCK_ENTITY.get(), net.votmdevs.voicesofthemines.client.VotvDoorRenderer::new);
            }

            @SubscribeEvent
            public static void registerOverlays(RegisterGuiOverlaysEvent event) {
                event.registerAbove(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HELMET.id(), "hazard_overlay", (gui, guiGraphics, partialTick, width, height) -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null && mc.options.getCameraType().isFirstPerson()) {
                        if (mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() == VoicesOfTheMines.HAZARD_HELMET.get()) {
                            ResourceLocation HAZARD_OVERLAY = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/hazard_overlay.png");
                            com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                            com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                            guiGraphics.blit(HAZARD_OVERLAY, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
                            com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                            com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                        }
                    }
                });
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        // sounds
        private static net.minecraft.client.resources.sounds.SimpleSoundInstance breakingBadSound = null;
        private static net.minecraft.client.resources.sounds.SimpleSoundInstance breathSound = null; // НОВАЯ ПЕРЕМЕННАЯ ДЛЯ ШЛЕМА
        private static int breathTimer = 0;
        // atv
        private static net.minecraft.client.resources.sounds.SimpleSoundInstance atvSoundInstance = null;
        private static String currentAtvSoundState = "none"; // "idle", "start", "loop"
        private static int atvSoundTimer = 0;

        @SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            // breaking bad easter egg
            boolean holdingShard = mc.player.getMainHandItem().getItem() == net.minecraft.world.item.Items.AMETHYST_SHARD;

            if (holdingShard) {
                if (breakingBadSound == null || !mc.getSoundManager().isActive(breakingBadSound)) {
                    breakingBadSound = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.BREAKING_BAD.get(), 1.0F, 1.0F);
                    mc.getSoundManager().play(breakingBadSound);
                }
            } else {
                if (breakingBadSound != null) {
                    mc.getSoundManager().stop(breakingBadSound);
                    breakingBadSound = null;
                }
            }
            // atv drive
            if (mc.player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
                if (atv.isEngineOn()) {
                    float speed = atv.getEntityData().get(net.votmdevs.voicesofthemines.entity.AtvEntity.CURRENT_SPEED);

                    String desiredState;
                    if (Math.abs(speed) < 0.05f) {
                        desiredState = "idle";
                    } else {
                        desiredState = "drive";
                    }

                    // soundmanager
                    if (!desiredState.equals(currentAtvSoundState)) {

                        // idle/loop
                        if (atvSoundInstance != null) {
                            mc.getSoundManager().stop(atvSoundInstance);
                            atvSoundInstance = null;
                        }

                        currentAtvSoundState = desiredState;

                        if (desiredState.equals("idle")) {
                            atvSoundInstance = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.IDLE.get(), 1.0F, 1.0F);
                            mc.getSoundManager().play(atvSoundInstance);
                        } else if (desiredState.equals("drive")) {
                            atvSoundInstance = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.ATV_DRIVE_START.get(), 1.0F, 1.0F);
                            mc.getSoundManager().play(atvSoundInstance);
                            atvSoundTimer = 60; // 3 sec
                        }
                    }


                    if (currentAtvSoundState.equals("drive")) {
                        if (atvSoundTimer > 0) {
                            atvSoundTimer--;
                        } else if (atvSoundInstance == null || !mc.getSoundManager().isActive(atvSoundInstance)) {
                            atvSoundInstance = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.ATV_DRIVE_LOOP.get(), 1.0F, 1.0F);
                            mc.getSoundManager().play(atvSoundInstance);
                        }
                    }
                } else {
                    if (atvSoundInstance != null) {
                        mc.getSoundManager().stop(atvSoundInstance);
                        atvSoundInstance = null;
                    }
                    currentAtvSoundState = "none";
                }
            } else {
                if (atvSoundInstance != null) {
                    mc.getSoundManager().stop(atvSoundInstance);
                    atvSoundInstance = null;
                }
                currentAtvSoundState = "none";
            }

            // breath
            boolean wearingHelmet = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() == VoicesOfTheMines.HAZARD_HELMET.get();

            if (wearingHelmet) {
                if (breathSound == null || !mc.getSoundManager().isActive(breathSound)) {
                    if (breathTimer <= 0) {
                        breathSound = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.BREATH.get(), 1.0F, 0.5F);
                        mc.getSoundManager().play(breathSound);

                        breathTimer = 15;
                    } else {
                        breathTimer--;
                    }
                }
            } else {
                if (breathSound != null) {
                    mc.getSoundManager().stop(breathSound);
                    breathSound = null;
                }
                breathTimer = 0;
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(net.minecraftforge.client.event.RenderHighlightEvent.Block event) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            BlockPos pos = event.getTarget().getBlockPos();

            if (mc.level == null || mc.player == null) return;
            if (mc.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 25.0D) return;

            Block block = mc.level.getBlockState(pos).getBlock();

            if (block == POSTER.get() || block == BOOK_RECIPE.get()) {
                com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
                net.minecraft.client.renderer.MultiBufferSource bufferSource = event.getMultiBufferSource();

                poseStack.pushPose();
                net.minecraft.world.phys.Vec3 cameraPos = event.getCamera().getPosition();

                if (block == POSTER.get()) {
                    poseStack.translate(pos.getX() + 0.5 - cameraPos.x, pos.getY() + 0.5 - cameraPos.y, pos.getZ() + 0.5 - cameraPos.z);
                } else {
                    poseStack.translate(pos.getX() + 0.5 - cameraPos.x, pos.getY() + 1.1 - cameraPos.y, pos.getZ() + 0.5 - cameraPos.z);
                }

                poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));
                poseStack.scale(0.8f, 0.8f, 0.8f);

                ResourceLocation SELECT_ICON = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/select.png");
                com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(SELECT_ICON));
                org.joml.Matrix4f matrix4f = poseStack.last().pose();
                org.joml.Matrix3f normalMatrix = poseStack.last().normal();
                int packedLight = 15728880;

                vertexConsumer.vertex(matrix4f, -0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, 0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, 0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, -0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();

                poseStack.popPose();
            }
        }
    }
}