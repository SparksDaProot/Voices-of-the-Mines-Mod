package net.votmdevs.voicesofthemines;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.votmdevs.voicesofthemines.block.*;
import net.votmdevs.voicesofthemines.client.*;
import net.votmdevs.voicesofthemines.effect.RadiationEffect;
import net.votmdevs.voicesofthemines.entity.*;
import net.votmdevs.voicesofthemines.inventory.KerfurMenu;
import net.votmdevs.voicesofthemines.item.HazardArmorMaterial;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.votmdevs.voicesofthemines.config.VotmConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

@Mod(VoicesOfTheMines.MODID)
public class VoicesOfTheMines {
    public static final String MODID = "voicesofthemines";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", RadiationEffect::new);

    public static final RegistryObject<EntityType<CockroachEntity>> COCKROACH = ENTITY_TYPES.register("cockroach",
            () -> EntityType.Builder.of(CockroachEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.1f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "cockroach").toString()));

    public static final RegistryObject<Item> COCKROACH_SPAWN_EGG = ITEMS.register("cockroach_spawn_egg",
            () -> new ForgeSpawnEggItem(COCKROACH, 0x4B3A2A, 0x2A1F16, new Item.Properties()));

    public static final RegistryObject<EntityType<MaxwellEntity>> MAXWELL = ENTITY_TYPES.register("maxwell",
            () -> EntityType.Builder.of(MaxwellEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.5f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "maxwell").toString()));

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
    public static final RegistryObject<Item> TABLE_ITEM = ITEMS.register("table",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    TABLE.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/table.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/table.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<Item> WALL_BEHIND_ITEM = ITEMS.register("wall_behind",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    WALL_BEHIND.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/wall_behind.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/wall_behind.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<Item> TERMINAL_FIND_ITEM = ITEMS.register("terminal_find",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    TERMINAL_FIND.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/terminal_find.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/terminal_find.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<Item> TERMINAL_PROCESSING_ITEM = ITEMS.register("terminal_processing",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    TERMINAL_PROCESSING.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/terminal_processing.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/terminal_processing.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<Item> TERMINAL_CHECK_ITEM = ITEMS.register("terminal_check",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    TERMINAL_CHECK.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/terminal_check.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/terminal_check.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<Item> TERMINAL_CALIBRATE_ITEM = ITEMS.register("terminal_calibrate",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    TERMINAL_CALIBRATE.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/terminal_calibrate.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/terminal_calibrate.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<Item> ACCESSORY_MAID = ITEMS.register("maid", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACCESSORY_RIBBON = ITEMS.register("ribbon", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACCESSORY_GLASSES = ITEMS.register("glasses", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ACCESSORY_JACKET = ITEMS.register("jacket", () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TRASH_BAG = ITEMS.register("trash_bag", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRASH_ROLL = ITEMS.register("trash_roll", () -> new Item(new Item.Properties().defaultDurability(10)));


    public static final RegistryObject<Item> FUEL_CAN_ITEM = ITEMS.register("fuel_can", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DRIVE_ITEM = ITEMS.register("drive", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WASH_SPONGE_ITEM = ITEMS.register("wash_sponge", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> METAL_DETECTOR_ITEM = ITEMS.register("metal_detector",
            () -> new net.votmdevs.voicesofthemines.item.MetalDetectorItem(new Item.Properties().stacksTo(1)));

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
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    VOTV_DOOR.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/votv_door.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/votv_door.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<EntityType<OmegaKerfurEntity>> OMEGA_KERFUR = ENTITY_TYPES.register("omega_kerfur",
            () -> EntityType.Builder.of(OmegaKerfurEntity::new, MobCategory.CREATURE)
                    .sized(0.8f, 2.2f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "omega_kerfur").toString()));

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


    public static final RegistryObject<EntityType<MannequinEntity>> MANNEQUIN = ENTITY_TYPES.register("mannequin",
            () -> EntityType.Builder.of(MannequinEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "mannequin").toString()));

    public static final RegistryObject<Item> MANNEQUIN_SPAWN_EGG = ITEMS.register("mannequin_spawn_egg",
            () -> new ForgeSpawnEggItem(MANNEQUIN, 0x8B7355, 0x5C4033, new Item.Properties()));

    public static final RegistryObject<EntityType<HostileMannequinEntity>> HOSTILE_MANNEQUIN = ENTITY_TYPES.register("hostile_mannequin",
            () -> EntityType.Builder.of(HostileMannequinEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "hostile_mannequin").toString()));

    public static final RegistryObject<Item> HOSTILE_MANNEQUIN_SPAWN_EGG = ITEMS.register("hostile_mannequin_spawn_egg",
            () -> new ForgeSpawnEggItem(HOSTILE_MANNEQUIN, 0x8B7355, 0xFF0000, new Item.Properties()));

    public static final RegistryObject<EntityType<MannequinStandEntity>> MANNEQUIN_STAND = ENTITY_TYPES.register("mannequin_stand",
            () -> EntityType.Builder.of(MannequinStandEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.1f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "mannequin_stand").toString()));


    // DECORATIVE BLOCKS

    public static final RegistryObject<Block> KITCHEN_TILE = BLOCKS.register("kitchen_tile", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> CARPET_BROWN_FLOOR = BLOCKS.register("carpet_brown_floor", () -> new Block(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> CARPET_FLOOR = BLOCKS.register("carpet_floor", () -> new Block(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> WALL_LINES = BLOCKS.register("wall_lines", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f)));
    public static final RegistryObject<Block> WALL = BLOCKS.register("wall", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f)));
    public static final RegistryObject<Block> WALL_DOWN = BLOCKS.register("wall_down", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f)));
    public static final RegistryObject<Block> GREYWALL_UP = BLOCKS.register("greywall_up", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> GREYWALL_DOWN = BLOCKS.register("greywall_down", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> GARAGE_YELLOW = BLOCKS.register("garage_yellow", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.DEEPSLATE_BRICKS)));
    public static final RegistryObject<Block> GARAGE_RED = BLOCKS.register("garage_red", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.DEEPSLATE_BRICKS)));
    public static final RegistryObject<Block> GARAGE_BRICKS = BLOCKS.register("garage_bricks", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.DEEPSLATE_BRICKS)));
    public static final RegistryObject<Block> OUTSIDE_FLOOR = BLOCKS.register("outside_floor", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> METAL_TILE = BLOCKS.register("metal_tile", () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> TILE = BLOCKS.register("tile", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> BATHROOM_TILE = BLOCKS.register("bathroom_tile", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> UP_TILE = BLOCKS.register("up_tile", () -> new Block(BlockBehaviour.Properties.of().strength(1.5f)));

    public static final RegistryObject<Block> SIGN = BLOCKS.register("sign",
            () -> new DecorativeHorizontalBlock.NoCollisionDecorativeBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.MOSS)));
    public static final RegistryObject<Block> SMOKE_SIGN = BLOCKS.register("smoke_sign",
            () -> new DecorativeHorizontalBlock.NoCollisionDecorativeBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.MOSS)));

    public static final RegistryObject<Block> MINI_VENT = BLOCKS.register("mini_vent", () -> new net.votmdevs.voicesofthemines.block.DecorativeHorizontalBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> EXIT = BLOCKS.register("exit",
            () -> new DecorativeHorizontalBlock.NoCollisionDecorativeBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().lightLevel(s -> 10)));

    public static final RegistryObject<Block> ROOM_SIGN_SIGNALS = BLOCKS.register("room_sign_signals",
            () -> new DecorativeHorizontalBlock.NoCollisionDecorativeBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.BAMBOO_WOOD)));

    public static final RegistryObject<Block> ROOM_SIGN_KITCHEN = BLOCKS.register("room_sign_kitchen",
                () -> new DecorativeHorizontalBlock.NoCollisionDecorativeBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.BAMBOO_WOOD)));

    public static final RegistryObject<Block> ROOM_SIGN_GARAGE = BLOCKS.register("room_sign_garage",
                () -> new DecorativeHorizontalBlock.NoCollisionDecorativeBlock(BlockBehaviour.Properties.of().strength(1.0f).noOcclusion().sound(SoundType.BAMBOO_WOOD)));


    public static final RegistryObject<Item> KITCHEN_TILE_ITEM = ITEMS.register("kitchen_tile", () -> new net.minecraft.world.item.BlockItem(KITCHEN_TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SIGN_ITEM = ITEMS.register("sign", () -> new net.minecraft.world.item.BlockItem(SIGN.get(), new Item.Properties()));
    public static final RegistryObject<Item> CARPET_BROWN_FLOOR_ITEM = ITEMS.register("carpet_brown_floor", () -> new net.minecraft.world.item.BlockItem(CARPET_BROWN_FLOOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> CARPET_FLOOR_ITEM = ITEMS.register("carpet_floor", () -> new net.minecraft.world.item.BlockItem(CARPET_FLOOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> WALL_LINES_ITEM = ITEMS.register("wall_lines", () -> new net.minecraft.world.item.BlockItem(WALL_LINES.get(), new Item.Properties()));
    public static final RegistryObject<Item> WALL_ITEM = ITEMS.register("wall", () -> new net.minecraft.world.item.BlockItem(WALL.get(), new Item.Properties()));
    public static final RegistryObject<Item> WALL_DOWN_ITEM = ITEMS.register("wall_down", () -> new net.minecraft.world.item.BlockItem(WALL_DOWN.get(), new Item.Properties()));
    public static final RegistryObject<Item> GREYWALL_UP_ITEM = ITEMS.register("greywall_up", () -> new net.minecraft.world.item.BlockItem(GREYWALL_UP.get(), new Item.Properties()));
    public static final RegistryObject<Item> GREYWALL_DOWN_ITEM = ITEMS.register("greywall_down", () -> new net.minecraft.world.item.BlockItem(GREYWALL_DOWN.get(), new Item.Properties()));
    public static final RegistryObject<Item> GARAGE_YELLOW_ITEM = ITEMS.register("garage_yellow", () -> new net.minecraft.world.item.BlockItem(GARAGE_YELLOW.get(), new Item.Properties()));
    public static final RegistryObject<Item> GARAGE_RED_ITEM = ITEMS.register("garage_red", () -> new net.minecraft.world.item.BlockItem(GARAGE_RED.get(), new Item.Properties()));
    public static final RegistryObject<Item> GARAGE_BRICKS_ITEM = ITEMS.register("garage_bricks", () -> new net.minecraft.world.item.BlockItem(GARAGE_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<Item> OUTSIDE_FLOOR_ITEM = ITEMS.register("outside_floor", () -> new net.minecraft.world.item.BlockItem(OUTSIDE_FLOOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> METAL_TILE_ITEM = ITEMS.register("metal_tile", () -> new net.minecraft.world.item.BlockItem(METAL_TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> TILE_ITEM = ITEMS.register("tile", () -> new net.minecraft.world.item.BlockItem(TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> BATHROOM_TILE_ITEM = ITEMS.register("bathroom_tile", () -> new net.minecraft.world.item.BlockItem(BATHROOM_TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> UP_TILE_ITEM = ITEMS.register("up_tile", () -> new net.minecraft.world.item.BlockItem(UP_TILE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SMOKE_SIGN_ITEM = ITEMS.register("smoke_sign", () -> new net.minecraft.world.item.BlockItem(SMOKE_SIGN.get(), new Item.Properties()));
    public static final RegistryObject<Item> MINI_VENT_ITEM = ITEMS.register("mini_vent", () -> new net.minecraft.world.item.BlockItem(MINI_VENT.get(), new Item.Properties()));
    public static final RegistryObject<Item> EXIT_ITEM = ITEMS.register("exit", () -> new net.minecraft.world.item.BlockItem(EXIT.get(), new Item.Properties()));
    public static final RegistryObject<Item> ROOM_SIGN_SIGNALS_ITEM = ITEMS.register("room_sign_signals", () -> new net.minecraft.world.item.BlockItem(ROOM_SIGN_SIGNALS.get(), new Item.Properties()));
    public static final RegistryObject<Item> ROOM_SIGN_KITCHEN_ITEM = ITEMS.register("room_sign_kitchen", () -> new net.minecraft.world.item.BlockItem(ROOM_SIGN_KITCHEN.get(), new Item.Properties()));
    public static final RegistryObject<Item> ROOM_SIGN_GARAGE_ITEM = ITEMS.register("room_sign_garage", () -> new net.minecraft.world.item.BlockItem(ROOM_SIGN_GARAGE.get(), new Item.Properties()));


    public static final RegistryObject<EntityType<AtvEntity>> ATV = ENTITY_TYPES.register("atv",
            () -> EntityType.Builder.of(AtvEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.2f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "atv").toString()));

    public static final RegistryObject<Item> ATV_SPAWN_EGG = ITEMS.register("atv_spawn_egg",
            () -> new ForgeSpawnEggItem(ATV, 0x555555, 0x111111, new Item.Properties()));

    public static final RegistryObject<EntityType<net.votmdevs.voicesofthemines.entity.HookEntity>> HOOK_ENTITY = ENTITY_TYPES.register("hook_entity",
            () -> EntityType.Builder.<net.votmdevs.voicesofthemines.entity.HookEntity>of(net.votmdevs.voicesofthemines.entity.HookEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(20)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "hook_entity").toString()));

    public static final RegistryObject<EntityType<FleshEntity>> FLESH = ENTITY_TYPES.register("flesh",
            () -> EntityType.Builder.of(FleshEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "flesh").toString()));

    public static final RegistryObject<Item> FLESH_SPAWN_EGG = ITEMS.register("flesh_spawn_egg",
            () -> new ForgeSpawnEggItem(FLESH, 0x880000, 0x440000, new Item.Properties()));

    public static final RegistryObject<EntityType<FuelCanEntity>> FUEL_CAN = ENTITY_TYPES.register("fuel_can",
            () -> EntityType.Builder.of(FuelCanEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "fuel_can").toString()));

    public static final RegistryObject<Item> FUEL_CAN_SPAWN_EGG = ITEMS.register("fuel_can_spawn_egg",
            () -> new ForgeSpawnEggItem(FUEL_CAN, 0x880000, 0x440000, new Item.Properties()));

    public static final RegistryObject<EntityType<GarbageEntity>> GARBAGE = ENTITY_TYPES.register("garbage",
            () -> EntityType.Builder.of(GarbageEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "garbage").toString()));

    public static final RegistryObject<EntityType<DriveEntity>> DRIVE = ENTITY_TYPES.register("drive",
            () -> EntityType.Builder.of(DriveEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.1f) // Плоский хитбокс
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "drive").toString()));

    public static final RegistryObject<Item> DRIVE_SPAWN_EGG = ITEMS.register("drive_spawn_egg",
            () -> new ForgeSpawnEggItem(DRIVE, 0x111111, 0x555555, new Item.Properties()));

    public static final RegistryObject<Block> DRIVE_BOX = BLOCKS.register("drive_box",
            () -> new DriveBoxBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(0.2F).noOcclusion()));

    public static final RegistryObject<Item> DRIVE_BOX_ITEM = ITEMS.register("drive_box",
            () -> new net.votmdevs.voicesofthemines.item.DriveBoxItem(DRIVE_BOX.get(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<DriveBoxBlockEntity>> DRIVE_BOX_BE = BLOCK_ENTITIES.register("drive_box_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(DriveBoxBlockEntity::new, DRIVE_BOX.get()).build(null));


    public static final RegistryObject<Item> PAPER_SHEET = ITEMS.register("paper_sheet",
            () -> new net.votmdevs.voicesofthemines.item.PaperSheetItem(new Item.Properties().stacksTo(16)));

    //candle handel

    public static final RegistryObject<Block> CANDLE_HANDLE = BLOCKS.register("candle_handle",
            () -> new net.votmdevs.voicesofthemines.block.CandleHandleBlock(BlockBehaviour.Properties.copy(Blocks.TORCH)
                    .instabreak()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(net.votmdevs.voicesofthemines.block.CandleHandleBlock.LIT) ? 14 : 0)));

    public static final RegistryObject<Item> CANDLE_HANDLE_ITEM = ITEMS.register("candle_handle",
            () -> new BlockItem(CANDLE_HANDLE.get(), new Item.Properties()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.CandleHandleBlockEntity>> CANDLE_HANDLE_BE = BLOCK_ENTITIES.register("candle_handle_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.CandleHandleBlockEntity::new, CANDLE_HANDLE.get()).build(null));


    // new redstone lamp?
    public static final RegistryObject<Block> UP_LAMP = BLOCKS.register("up_lamp",
            () -> new net.votmdevs.voicesofthemines.block.UpLampBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(1.0f)
                    .noOcclusion() // Обязательно для кастомных моделей, чтобы не было рентгена соседних блоков
                    .lightLevel(state -> state.getValue(net.votmdevs.voicesofthemines.block.UpLampBlock.LIT) ? 15 : 0))); // Лампа дает свет 15 при LIT=true

    public static final RegistryObject<Block> SWITCH_BLOCK = BLOCKS.register("switch",
            () -> new net.votmdevs.voicesofthemines.block.SwitchBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .noOcclusion()));

    // items
    public static final RegistryObject<Item> UP_LAMP_ITEM = ITEMS.register("up_lamp",
            () -> new net.minecraft.world.item.BlockItem(UP_LAMP.get(), new Item.Properties()));

    public static final RegistryObject<Item> SWITCH_ITEM = ITEMS.register("switch",
            () -> new net.minecraft.world.item.BlockItem(SWITCH_BLOCK.get(), new Item.Properties()));

    // entities
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.UpLampBlockEntity>> UP_LAMP_BE = BLOCK_ENTITIES.register("up_lamp",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.UpLampBlockEntity::new, UP_LAMP.get()).build(null));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.SwitchBlockEntity>> SWITCH_BE = BLOCK_ENTITIES.register("switch",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.SwitchBlockEntity::new, SWITCH_BLOCK.get()).build(null));

    // maracas :D
    public static final RegistryObject<Item> MARACAS = ITEMS.register("maracas",
            () -> new net.votmdevs.voicesofthemines.item.MaracasItem(new Item.Properties().stacksTo(1)));


    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity>> TERMINAL_BE = BLOCK_ENTITIES.register("terminal_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.VotvTerminalBlockEntity::new,
                    TABLE.get(), WALL_BEHIND.get(), TERMINAL_FIND.get(), TERMINAL_PROCESSING.get(), TERMINAL_CHECK.get(), TERMINAL_CALIBRATE.get()
            ).build(null));

    public static final RegistryObject<Item> GARBAGE_SPAWN_EGG = ITEMS.register("garbage_spawn_egg",
            () -> new ForgeSpawnEggItem(GARBAGE, 0x880000, 0x440000, new Item.Properties()));

    public static final RegistryObject<EntityType<BloodSplashEntity>> BLOOD_SPLASH = ENTITY_TYPES.register("blood_splash",
            () -> EntityType.Builder.of(BloodSplashEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.1f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "blood_splash").toString()));


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
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    SERVER_BLOCK.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/server.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/server.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.ServerBlockEntity>> SERVER_BE = BLOCK_ENTITIES.register("server_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.ServerBlockEntity::new, SERVER_BLOCK.get()).build(null));

    public static final RegistryObject<Block> CONSOLE_BLOCK = BLOCKS.register("console_block",
            () -> new net.votmdevs.voicesofthemines.block.ConsoleBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> CONSOLE_BLOCK_ITEM = ITEMS.register("console_block",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    CONSOLE_BLOCK.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/console.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/console.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.ConsoleBlockEntity>> CONSOLE_BE = BLOCK_ENTITIES.register("console_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.ConsoleBlockEntity::new, CONSOLE_BLOCK.get()).build(null));

    public static final RegistryObject<Block> COMPUTER_CHAIR = BLOCKS.register("computer_chair",
            () -> new ChairBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<BlockEntityType<ChairBlockEntity>> CHAIR_BE = BLOCK_ENTITIES.register("chair_be",
            () -> BlockEntityType.Builder.of(ChairBlockEntity::new, COMPUTER_CHAIR.get()).build(null));

    public static final RegistryObject<Item> COMPUTER_CHAIR_ITEM = ITEMS.register("computer_chair",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    COMPUTER_CHAIR.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/chair.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/chair.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<EntityType<SeatEntity>> SEAT_ENTITY = ENTITY_TYPES.register("seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.01f, 0.01f)
                    .build("seat"));

    // new entities

    public static final RegistryObject<EntityType<GeomOctahedronEntity>> GEOM_OCTAHEDRON = ENTITY_TYPES.register("geomoct",
            () -> EntityType.Builder.of(GeomOctahedronEntity::new, MobCategory.CREATURE)
                    .sized(1.2f, 1.2f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "geomoct").toString()));

    public static final RegistryObject<Item> GEOM_OCTAHEDRON_SPAWN_EGG = ITEMS.register("geomoct_spawn_egg",
            () -> new ForgeSpawnEggItem(GEOM_OCTAHEDRON, 0x333333, 0x00FF00, new Item.Properties()));


    //WISPS

    public static final RegistryObject<EntityType<BlackWispEntity>> BLACK_WISP = ENTITY_TYPES.register("blackwisp",
            () -> EntityType.Builder.of(BlackWispEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "blackwisp").toString()));

    public static final RegistryObject<Item> BLACK_WISP_SPAWN_EGG = ITEMS.register("blackwisp_spawn_egg",
            () -> new ForgeSpawnEggItem(BLACK_WISP, 0x000000, 0x111111, new Item.Properties()));

    public static final RegistryObject<EntityType<PinkWispEntity>> PINK_WISP = ENTITY_TYPES.register("pinkwisp",
            () -> EntityType.Builder.of(PinkWispEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "pinkwisp").toString()));

    public static final RegistryObject<Item> PINK_WISP_SPAWN_EGG = ITEMS.register("pinkwisp_spawn_egg",
            () -> new ForgeSpawnEggItem(PINK_WISP, 0x000000, 0x111111, new Item.Properties()));

    public static final RegistryObject<EntityType<YellowWispEntity>> YELLOW_WISP = ENTITY_TYPES.register("yellowwisp",
            () -> EntityType.Builder.of(YellowWispEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "yellowwisp").toString()));

    public static final RegistryObject<Item> YELLOW_WISP_SPAWN_EGG = ITEMS.register("yellowwisp_spawn_egg",
            () -> new ForgeSpawnEggItem(YELLOW_WISP, 0x000000, 0x111111, new Item.Properties()));

    public static final RegistryObject<EntityType<GreenWispEntity>> GREEN_WISP = ENTITY_TYPES.register("greenwisp",
            () -> EntityType.Builder.of(GreenWispEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "greenwisp").toString()));

    public static final RegistryObject<Item> GREEN_WISP_SPAWN_EGG = ITEMS.register("greenwisp_spawn_egg",
            () -> new ForgeSpawnEggItem(GREEN_WISP, 0x000000, 0x111111, new Item.Properties()));

    public static final RegistryObject<EntityType<BlueWispEntity>> BLUE_WISP = ENTITY_TYPES.register("bluewisp",
            () -> EntityType.Builder.of(BlueWispEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "bluewisp").toString()));

    public static final RegistryObject<Item> BLUE_WISP_SPAWN_EGG = ITEMS.register("bluewisp_spawn_egg",
            () -> new ForgeSpawnEggItem(BLUE_WISP, 0x000000, 0x111111, new Item.Properties()));


    public static final RegistryObject<EntityType<TrashSplashEntity>> TRASH_SPLASH = ENTITY_TYPES.register("trash_splash",
            () -> EntityType.Builder.of(TrashSplashEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.1f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "trash_splash").toString()));

    public static final RegistryObject<EntityType<WashSpongeEntity>> WASH_SPONGE = ENTITY_TYPES.register("wash_sponge",
            () -> EntityType.Builder.of(WashSpongeEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "wash_sponge").toString()));

    public static final RegistryObject<EntityType<TreasureSpotEntity>> TREASURE_SPOT = ENTITY_TYPES.register("treasure_spot",
            () -> EntityType.Builder.<TreasureSpotEntity>of(TreasureSpotEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .clientTrackingRange(30)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "treasure_spot").toString()));

    // drone

    public static final RegistryObject<Block> DRONE_TARGET = BLOCKS.register("target_drone_block",
            () -> new FlatBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion()));

    public static final RegistryObject<Item> DRONE_TARGET_ITEM = ITEMS.register("target_drone_block",
            () -> new BlockItem(DRONE_TARGET.get(), new Item.Properties()));

    public static final RegistryObject<Block> DRONE_PANEL = BLOCKS.register("drone_panel",
            () -> new net.votmdevs.voicesofthemines.block.DronePanelBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Item> DRONE_PANEL_ITEM = ITEMS.register("drone_panel",
            () -> new net.votmdevs.voicesofthemines.item.GeoBlockItem(
                    DRONE_PANEL.get(),
                    new Item.Properties(),
                    ResourceLocation.fromNamespaceAndPath(MODID, "geo/drone_panel.geo.json"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/drone_panel.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID, "animations/empty.animation.json")
            ));

    public static final RegistryObject<EntityType<net.votmdevs.voicesofthemines.entity.DroneEntity>> DRONE = ENTITY_TYPES.register("drone",
            () -> EntityType.Builder.of(net.votmdevs.voicesofthemines.entity.DroneEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.0f)
                    .clientTrackingRange(128)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "drone").toString()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.DronePanelBlockEntity>> DRONE_PANEL_BE = BLOCK_ENTITIES.register("drone_panel_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.DronePanelBlockEntity::new, DRONE_PANEL.get()).build(null));

    //PLUSHIE

    public static final RegistryObject<Block> PLUSHIE_BENJIKUS = BLOCKS.register("plushie_benjikus",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), net.votmdevs.voicesofthemines.block.PlushieType.BENJIKUS));

    public static final RegistryObject<Block> PLUSHIE_BENJIKUS_COMMON = BLOCKS.register("plushie_benjikuscommon",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), net.votmdevs.voicesofthemines.block.PlushieType.BENJIKUS_COMMON));

    public static final RegistryObject<Block> PLUSHIE_NIKO = BLOCKS.register("plushie_niko",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), net.votmdevs.voicesofthemines.block.PlushieType.NIKO));

    public static final RegistryObject<Block> PLUSHIE_INVINCIBLE = BLOCKS.register("plushie_invincible",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), net.votmdevs.voicesofthemines.block.PlushieType.INVINCIBLE));

    public static final RegistryObject<Block> PLUSHIE_KEL = BLOCKS.register("plushie_kel",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), net.votmdevs.voicesofthemines.block.PlushieType.KEL));

    public static final RegistryObject<Block> PLUSHIE_PECORA = BLOCKS.register("plushie_pecora",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), PlushieType.PECORA));

    public static final RegistryObject<Block> PLUSHIE_SPARKSY = BLOCKS.register("plushie_sparksy",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), PlushieType.SPARKSY));

    public static final RegistryObject<Block> PLUSHIE_LIBE = BLOCKS.register("plushie_libe",
            () -> new net.votmdevs.voicesofthemines.block.PlushieBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion(), PlushieType.LIBE));

    //items plush

    public static final RegistryObject<Item> PLUSHIE_BENJIKUS_ITEM = ITEMS.register("plushie_benjikus",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_BENJIKUS.get(), new net.minecraft.world.item.Item.Properties(), net.votmdevs.voicesofthemines.block.PlushieType.BENJIKUS));

    public static final RegistryObject<Item> PLUSHIE_BENJIKUS_COMMON_ITEM = ITEMS.register("plushie_benjikuscommon",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_BENJIKUS_COMMON.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.BENJIKUS_COMMON));

    public static final RegistryObject<Item> PLUSHIE_NIKO_ITEM = ITEMS.register("plushie_niko",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_NIKO.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.NIKO));

    public static final RegistryObject<Item> PLUSHIE_INVINCIBLE_ITEM = ITEMS.register("plushie_invincible",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_INVINCIBLE.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.INVINCIBLE));

    public static final RegistryObject<Item> PLUSHIE_KEL_ITEM = ITEMS.register("plushie_kel",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_KEL.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.KEL));

    public static final RegistryObject<Item> PLUSHIE_PECORA_ITEM = ITEMS.register("plushie_pecora",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_PECORA.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.PECORA));

    public static final RegistryObject<Item> PLUSHIE_SPARKSY_ITEM = ITEMS.register("plushie_sparksy",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_SPARKSY.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.SPARKSY));

    public static final RegistryObject<Item> PLUSHIE_LIBE_ITEM = ITEMS.register("plushie_libe",
            () -> new net.votmdevs.voicesofthemines.item.PlushieItem(PLUSHIE_LIBE.get(), new net.minecraft.world.item.Item.Properties(), PlushieType.LIBE));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<net.votmdevs.voicesofthemines.block.PlushieBlockEntity>> PLUSHIE_BE = BLOCK_ENTITIES.register("plushie_be",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(net.votmdevs.voicesofthemines.block.PlushieBlockEntity::new,
                    PLUSHIE_BENJIKUS.get(),
                    PLUSHIE_BENJIKUS_COMMON.get(),
                    PLUSHIE_NIKO.get(),
                    PLUSHIE_INVINCIBLE.get(),
                    PLUSHIE_KEL.get(),
                    PLUSHIE_PECORA.get(),
                    PLUSHIE_SPARKSY.get(),
                    PLUSHIE_LIBE.get()
            ).build(null));

    public static final RegistryObject<EntityType<KerfurEntity>> KERFUR = ENTITY_TYPES.register("kerfur",
            () -> EntityType.Builder.of(KerfurEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "kerfur").toString()));

    public static final RegistryObject<Item> KERFUR_SPAWN_EGG = ITEMS.register("kerfur_spawn_egg",
            () -> new ForgeSpawnEggItem(KERFUR, 0xFFFFFF, 0x000000, new Item.Properties()));

    // Adds all mod items to custom creative menu tab
    public static final RegistryObject<CreativeModeTab> VOICES_OF_THE_MINES_TAB =
            CREATIVE_MODE_TABS.register("voices_of_the_mines", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.voicesofthemines"))
                    .icon(() -> new ItemStack(SERVER_BLOCK_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        for (RegistryObject<Item> item : ITEMS.getEntries()) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    public VoicesOfTheMines(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, VotmConfig.SERVER_SPEC);

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
        CREATIVE_MODE_TABS.register(modEventBus);
        KerfurPacketHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SERVER_BLOCK_ITEM);
            event.accept(COMPUTER_CHAIR_ITEM);
            event.accept(CONSOLE_BLOCK_ITEM);
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
            event.accept(CANDLE_HANDLE_ITEM);
        }

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(HAZARD_HELMET);
            event.accept(HAZARD_CHESTPLATE);
            event.accept(HAZARD_LEGGINGS);
            event.accept(HAZARD_BOOTS);
        }

        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(BATHROOM_TILE_ITEM);
            event.accept(CARPET_FLOOR_ITEM);
            event.accept(CARPET_BROWN_FLOOR_ITEM);
            event.accept(EXIT_ITEM);
            event.accept(GARAGE_BRICKS_ITEM);
            event.accept(GARAGE_RED_ITEM);
            event.accept(GARAGE_YELLOW_ITEM);
            event.accept(GREYWALL_UP_ITEM);
            event.accept(GREYWALL_DOWN_ITEM);
            event.accept(KITCHEN_TILE_ITEM);
            event.accept(TILE_ITEM);
            event.accept(METAL_TILE_ITEM);
            event.accept(MINI_VENT_ITEM);
            event.accept(ROOM_SIGN_SIGNALS_ITEM);
            event.accept(ROOM_SIGN_GARAGE_ITEM);
            event.accept(ROOM_SIGN_KITCHEN_ITEM);
            event.accept(SIGN_ITEM);
            event.accept(SMOKE_SIGN_ITEM);
            event.accept(UP_TILE_ITEM);
            event.accept(WALL_ITEM);
            event.accept(WALL_DOWN_ITEM);
            event.accept(WALL_LINES_ITEM);
            event.accept(OUTSIDE_FLOOR_ITEM);
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
            event.accept(MANNEQUIN_SPAWN_EGG);
            event.accept(HOSTILE_MANNEQUIN_SPAWN_EGG);
            event.accept(KERFUR_SPAWN_EGG);
            event.accept(OMEGA_KERFUR_SPAWN_EGG);
            event.accept(FLESH_SPAWN_EGG);
            event.accept(ATV_SPAWN_EGG);
            event.accept(DRIVE_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DISK_BLUE);
            event.accept(TRASH_BAG);
            event.accept(TRASH_ROLL);
            event.accept(HOOK_ITEM);
            event.accept(MARACAS);
            event.accept(METAL_DETECTOR_ITEM);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Kerfur Mod Initialized!");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void onAttributeCreate(EntityAttributeCreationEvent event) {
            event.put(GEOM_OCTAHEDRON.get(), GeomOctahedronEntity.createAttributes().build());
            event.put(BLACK_WISP.get(), BlackWispEntity.createAttributes().build());
            event.put(BLUE_WISP.get(), BlueWispEntity.createAttributes().build());
            event.put(GREEN_WISP.get(), GreenWispEntity.createAttributes().build());
            event.put(PINK_WISP.get(), PinkWispEntity.createAttributes().build());
            event.put(YELLOW_WISP.get(), YellowWispEntity.createAttributes().build());
            event.put(MANNEQUIN.get(), MannequinEntity.createAttributes().build());
            event.put(TREASURE_SPOT.get(), net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0D).build());
            event.put(HOSTILE_MANNEQUIN.get(), HostileMannequinEntity.createAttributes().build());
            event.put(MANNEQUIN_STAND.get(), MannequinStandEntity.createAttributes().build());
            event.put(KERFUR.get(), KerfurEntity.createAttributes().build());
            event.put(FLESH.get(), FleshEntity.createAttributes().build());
            event.put(BLOOD_SPLASH.get(), BloodSplashEntity.createAttributes().build());
            event.put(TRASH_SPLASH.get(), TrashSplashEntity.createAttributes().build());
            event.put(WASH_SPONGE.get(), WashSpongeEntity.createAttributes().build());
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


                    net.minecraft.client.renderer.item.ItemProperties.register(VoicesOfTheMines.HOOK_ITEM.get(), ResourceLocation.fromNamespaceAndPath(VoicesOfTheMines.MODID, "active"),
                            (stack, level, entity, seed) -> {

                                return stack.hasTag() && stack.getTag().getBoolean("Active") ? 1.0F : 0.0F;
                            });
                });
            }


            @SubscribeEvent
            public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
                event.registerBlockEntityRenderer(VoicesOfTheMines.PLUSHIE_BE.get(), net.votmdevs.voicesofthemines.client.PlushieRenderer::new);
                event.registerEntityRenderer(VoicesOfTheMines.BLUE_WISP.get(), net.votmdevs.voicesofthemines.client.BlueWispRenderer::new);
                event.registerEntityRenderer(VoicesOfTheMines.GREEN_WISP.get(), net.votmdevs.voicesofthemines.client.GreenWispRenderer::new);
                event.registerEntityRenderer(VoicesOfTheMines.YELLOW_WISP.get(), net.votmdevs.voicesofthemines.client.YellowWispRenderer::new);
                event.registerEntityRenderer(VoicesOfTheMines.PINK_WISP.get(), net.votmdevs.voicesofthemines.client.PinkWispRenderer::new);
                event.registerEntityRenderer(VoicesOfTheMines.BLACK_WISP.get(), net.votmdevs.voicesofthemines.client.BlackWispRenderer::new);
                event.registerEntityRenderer(VoicesOfTheMines.GEOM_OCTAHEDRON.get(), net.votmdevs.voicesofthemines.client.GeomOctahedronRenderer::new);
                event.registerEntityRenderer(TREASURE_SPOT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
                event.registerBlockEntityRenderer(VoicesOfTheMines.UP_LAMP_BE.get(), net.votmdevs.voicesofthemines.client.UpLampRenderer::new);
                event.registerBlockEntityRenderer(VoicesOfTheMines.SWITCH_BE.get(), net.votmdevs.voicesofthemines.client.SwitchRenderer::new);
                event.registerBlockEntityRenderer(CANDLE_HANDLE_BE.get(), CandleHandleRenderer::new);
                event.registerBlockEntityRenderer(DRIVE_BOX_BE.get(), DriveBoxRenderer::new);
                event.registerEntityRenderer(MANNEQUIN.get(), manager -> new BaseMannequinRenderer<>(manager, new MannequinModel()));
                event.registerEntityRenderer(HOSTILE_MANNEQUIN.get(), manager -> new BaseMannequinRenderer<>(manager, new HostileMannequinModel()));
                event.registerEntityRenderer(MANNEQUIN_STAND.get(), manager -> new BaseMannequinRenderer<>(manager, new MannequinStandModel()));
                event.registerEntityRenderer(VoicesOfTheMines.SEAT_ENTITY.get(), SeatRenderer::new);
                event.registerBlockEntityRenderer(SERVER_BE.get(), ServerRenderer::new);
                event.registerBlockEntityRenderer(VoicesOfTheMines.CHAIR_BE.get(), ChairRenderer::new);
                event.registerEntityRenderer(KERFUR.get(), KerfurRenderer::new);
                event.registerEntityRenderer(FLESH.get(), FleshRenderer::new);
                event.registerBlockEntityRenderer(CONSOLE_BE.get(), ConsoleRenderer::new);
                event.registerEntityRenderer(COCKROACH.get(), CockroachRenderer::new);
                event.registerEntityRenderer(OMEGA_KERFUR.get(), OmegaKerfurRenderer::new);
                event.registerBlockEntityRenderer(POSTER_BLOCK_ENTITY.get(), PosterRenderer::new);
                event.registerEntityRenderer(GARBAGE.get(), net.votmdevs.voicesofthemines.client.GarbageRenderer::new);
                event.registerEntityRenderer(BLOOD_SPLASH.get(), BloodSplashRenderer::new);
                event.registerEntityRenderer(TRASH_SPLASH.get(), TrashSplashRenderer::new);
                event.registerEntityRenderer(WASH_SPONGE.get(), WashSpongeRenderer::new);
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
                            ResourceLocation HAZARD_OVERLAY = ResourceLocation.fromNamespaceAndPath(VoicesOfTheMines.MODID, "textures/gui/hazard_overlay.png");
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
                event.registerAbove(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HELMET.id(), "wisp_overlay", (gui, guiGraphics, partialTick, width, height) -> {
                    if (VoicesOfTheMines.ClientForgeEvents.wispBlackScreenTimer > 0) {
                        ResourceLocation BLACK_OVERLAY = ResourceLocation.fromNamespaceAndPath(VoicesOfTheMines.MODID, "textures/gui/blackoverlay.png");
                        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                        guiGraphics.blit(BLACK_OVERLAY, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
                        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                    }
                });
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        public static int wispShakeTimer = 0;
        public static int wispBlackScreenTimer = 0;

        // sounds
        private static net.minecraft.client.resources.sounds.SimpleSoundInstance breakingBadSound = null;
        private static net.minecraft.client.resources.sounds.SimpleSoundInstance breathSound = null; // НОВАЯ ПЕРЕМЕННАЯ ДЛЯ ШЛЕМА
        private static int breathTimer = 0;
        // atv
        private static net.minecraft.client.resources.sounds.SimpleSoundInstance atvSoundInstance = null;
        private static String currentAtvSoundState = "none"; // "idle", "start", "loop"
        private static int atvSoundTimer = 0;

        @SubscribeEvent
        public static void onCameraSetup(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event) {
            if (wispShakeTimer > 0) {
                float shake = 0.8f; // Интенсивность тряски (чем больше, тем сильнее)
                event.setPitch(event.getPitch() + (float)(Math.random() - 0.5) * shake);
                event.setYaw(event.getYaw() + (float)(Math.random() - 0.5) * shake);
                event.setRoll(event.getRoll() + (float)(Math.random() - 0.5) * shake);
            }
        }

        @SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            // breaking bad EASTER egg
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

            if (wispShakeTimer > 0) wispShakeTimer--;
            if (wispBlackScreenTimer > 0) wispBlackScreenTimer--;

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

                ResourceLocation SELECT_ICON = ResourceLocation.fromNamespaceAndPath(VoicesOfTheMines.MODID, "textures/gui/select.png");
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