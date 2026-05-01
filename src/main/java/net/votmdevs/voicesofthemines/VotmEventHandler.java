package net.votmdevs.voicesofthemines;

import net.minecraft.core.BlockPos;
import net.votmdevs.voicesofthemines.entity.CockroachEntity;
import net.votmdevs.voicesofthemines.entity.FleshEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.votmdevs.voicesofthemines.entity.TrashSplashEntity;

import java.util.List;

@Mod.EventBusSubscriber(modid = VoicesOfTheMines.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VotmEventHandler {

    @SubscribeEvent
    public static void onZombieDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Zombie zombie) {
            Level level = zombie.level();
            if (!level.isClientSide()) {
                if (level.random.nextFloat() < 0.5F) {
                    FleshEntity flesh = VoicesOfTheMines.FLESH.get().create(level);
                    if (flesh != null) {
                        flesh.moveTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), 0.0F);
                        flesh.setDeltaMovement((level.random.nextFloat() - 0.5) * 0.2, 0.2, (level.random.nextFloat() - 0.5) * 0.2);
                        level.addFreshEntity(flesh);
                    }
                }
            }
        }
    }

    // badsun
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (entity instanceof net.votmdevs.voicesofthemines.entity.FleshEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.GarbageEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.FuelCanEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.OmegaKerfurEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.KerfurEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.DroneEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.DriveEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.BloodSplashEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.MaxwellEntity ||
                entity instanceof net.votmdevs.voicesofthemines.entity.AbstractMannequinEntity) {
            return;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == Level.OVERWORLD) {
                net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(serverLevel);

                if (manager.isBadSunActive && serverLevel.isDay()) {
                    if (entity.tickCount % 20 == 0) {

                        BlockPos eyePos = BlockPos.containing(entity.getEyePosition());
                        if (serverLevel.canSeeSky(eyePos)) {

                            entity.hurt(serverLevel.damageSources().onFire(), 3.0F);

                            if (serverLevel.random.nextFloat() < 0.15F) {
                                FleshEntity flesh = VoicesOfTheMines.FLESH.get().create(serverLevel);
                                if (flesh != null) {
                                    flesh.moveTo(entity.getX(), entity.getY() + 1.0, entity.getZ(), 0, 0);
                                    flesh.setDeltaMovement((serverLevel.random.nextFloat() - 0.5) * 0.3, 0.2, (serverLevel.random.nextFloat() - 0.5) * 0.3);
                                    serverLevel.addFreshEntity(flesh);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // /votmevent
    @SubscribeEvent
    public static void onCommandsRegister(net.minecraftforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("votmevent")
                .requires(s -> s.hasPermission(2))

                // 1. Команда badsun
                .then(net.minecraft.commands.Commands.literal("badsun")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(level);

                            manager.isBadSunActive = !manager.isBadSunActive;
                            manager.setDirty();

                            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Bad Sun event is now: " + manager.isBadSunActive), true);
                            return 1;
                        })
                )

                // /votmevent spsig <signal> - for tests
                .then(net.minecraft.commands.Commands.literal("spsig")
                        .then(net.minecraft.commands.Commands.argument("signal_name", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    Player player = context.getSource().getPlayerOrException();
                                    String signalType = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "signal_name");

                                    net.votmdevs.voicesofthemines.entity.DriveEntity drive = VoicesOfTheMines.DRIVE.get().create(level);
                                    if (drive != null) {
                                        String fakeId = java.util.UUID.randomUUID().toString();

                                        drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID, fakeId);
                                        drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE, signalType);
                                        drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL, 0);

                                        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                                        drive.moveTo(player.getX() + look.x, player.getY() + 1.0, player.getZ() + look.z, 0, 0);
                                        level.addFreshEntity(drive);

                                        context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§aSpawned Level 0 Drive with signal: " + signalType), true);
                                    }
                                    return 1;
                                })
                                .then(net.minecraft.commands.Commands.argument("level", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 3))
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            Player player = context.getSource().getPlayerOrException();
                                            String signalType = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "signal_name");
                                            int sigLvl = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "level");

                                            net.votmdevs.voicesofthemines.entity.DriveEntity drive = VoicesOfTheMines.DRIVE.get().create(level);
                                            if (drive != null) {
                                                String fakeId = java.util.UUID.randomUUID().toString();

                                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID, fakeId);
                                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_TYPE, signalType);
                                                drive.getEntityData().set(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_LEVEL, sigLvl);

                                                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                                                drive.moveTo(player.getX() + look.x, player.getY() + 1.0, player.getZ() + look.z, 0, 0);
                                                level.addFreshEntity(drive);

                                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§aSpawned Level " + sigLvl + " Drive with signal: " + signalType), true);
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
                // /votmevent addpoints
                .then(net.minecraft.commands.Commands.literal("addpoints")
                        .then(net.minecraft.commands.Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    net.minecraft.server.level.ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount");

                                    net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(level);
                                    net.votmdevs.voicesofthemines.world.PlayerData pd = manager.getGlobalPlayerData();

                                    pd.addPoints(player.getUUID(), amount);
                                    manager.setDirty();

                                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncComputerDataPacket(
                                                    pd.getPoints(player.getUUID()),
                                                    pd.getCursorSpeedLvl(),
                                                    pd.getPingCooldownLvl(),
                                                    pd.getProcessingSpeedLvl(),
                                                    pd.getProcessingLevelLvl(),
                                                    pd.getEmails(player.getUUID()),
                                                    pd.customMarket
                                            ),
                                            player.connection.connection,
                                            net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                                    );

                                    context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§aAdded " + amount + " points to your account!"), true);
                                    return 1;
                                })
                        )
                )
        );
        // DEBUG COMMAND FOR TESTS DAILY TASKS - CHOOSE DIFFICULTY /repordif
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("reportdif")
                .requires(s -> s.hasPermission(2))
                .then(net.minecraft.commands.Commands.argument("day", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(level);

                            int newDay = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "day");
                            manager.currentDay = newDay;
                            manager.generateDailyTask();

                            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Set current day to " + newDay + ". Check your email for new tasks!"), true);
                            return 1;
                        })
                )
        );
    }
// TRASH BAG - trash splash
    @SubscribeEvent
    public static void onRightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getItemStack().getItem() == VoicesOfTheMines.TRASH_BAG.get()) {
            BlockPos pos = event.getPos();
            net.minecraft.core.Direction face = event.getFace();
            if (face != null) {
                TrashSplashEntity splash = VoicesOfTheMines.TRASH_SPLASH.get().create(event.getLevel());
                if (splash != null) {
                    splash.moveTo(
                            pos.getX() + 0.5 + face.getStepX() * 0.51,
                            pos.getY() + 0.5 + face.getStepY() * 0.51,
                            pos.getZ() + 0.5 + face.getStepZ() * 0.51,
                            0, 0
                    );

                    float yaw = 0, pitch = 0;
                    switch (face) {
                        case UP: pitch = 0; break;
                        case DOWN: pitch = 180; break;
                        case NORTH: pitch = -90; yaw = 180; break;
                        case SOUTH: pitch = -90; yaw = 0; break;
                        case WEST: pitch = -90; yaw = 90; break;
                        case EAST: pitch = -90; yaw = -90; break;
                    }

                    splash.setLockedRotationAndFace(yaw, pitch, face);
                    event.getLevel().addFreshEntity(splash);

                    net.minecraft.sounds.SoundEvent[] splatSounds = {
                            net.votmdevs.voicesofthemines.VotmSounds.SPLAT1.get(),
                            net.votmdevs.voicesofthemines.VotmSounds.SPLAT2.get(),
                            net.votmdevs.voicesofthemines.VotmSounds.SPLAT3.get()
                    };
                    net.minecraft.sounds.SoundEvent selectedSplat = splatSounds[event.getLevel().random.nextInt(splatSounds.length)];
                    event.getLevel().playSound(null, pos, selectedSplat, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F + event.getLevel().random.nextFloat() * 0.4F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            ServerLevel level = (ServerLevel) event.level;
            if (level.dimension() == Level.OVERWORLD) {
                net.votmdevs.voicesofthemines.world.SignalManager manager = net.votmdevs.voicesofthemines.world.SignalManager.get(level);
                manager.tick();

                if (level.getDayTime() > 0 && level.getDayTime() % 24000 == 0) {
                    manager.advanceDay();
                }

                if (level.getGameTime() % 60 == 0) {
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.send(
                            net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncEventStatePacket(manager.isBadSunActive && level.isDay())
                    );
                }

                long breakIntervalTicks = net.votmdevs.voicesofthemines.config.VotmConfig.getServerBreakIntervalTicks();
                long protectionTicks = net.votmdevs.voicesofthemines.config.VotmConfig.getRecentlyFixedProtectionTicks();

                if (breakIntervalTicks > 0L && level.getGameTime() % breakIntervalTicks == 0L) {
                    if (net.votmdevs.voicesofthemines.config.VotmConfig.debugSignalBreaks()) {
                        VoicesOfTheMines.LOGGER.info(
                                "[VOTM Signal Debug] Break timer fired at gameTime={} | intervalTicks={} | protectionTicks={}",
                                level.getGameTime(),
                                breakIntervalTicks,
                                protectionTicks
                        );
                    }

                    manager.degradeRandomCalibration(level.getGameTime(), protectionTicks);
                }
            }
            if (level.getGameTime() % 20 == 0) {
                for (Player player : level.players()) {
                    List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(24.0D));
                    for (ItemEntity item : items) {
                        if (item.getItem().getItem().isEdible() && item.getAge() >= 200 && !item.getPersistentData().getBoolean("RoachesSpawned")) {
                            item.getPersistentData().putBoolean("RoachesSpawned", true);
                            int count = 3 + level.random.nextInt(3);
                            for (int i = 0; i < count; i++) {
                                CockroachEntity roach = VoicesOfTheMines.COCKROACH.get().create(level);
                                if (roach != null) {
                                    roach.moveTo(item.getX() + (level.random.nextDouble() - 0.5), item.getY(), item.getZ() + (level.random.nextDouble() - 0.5), level.random.nextFloat() * 360F, 0);
                                    level.addFreshEntity(roach);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // Knockdown
    @SubscribeEvent
    public static void onPlayerDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {

            if (event.getAmount() >= 6.0F) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendTo(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.KnockdownPacket(),
                        player.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
            }
        }
    }


    // Radiation/Suit
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            if (player.tickCount % 20 == 0) {

                boolean hasCapsule = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).getItem() == VoicesOfTheMines.RADIOACTIVE_CAPSULE.get()) {
                        hasCapsule = true;
                        break;
                    }
                }

                if (hasCapsule) {
                    boolean hasHelmet = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == VoicesOfTheMines.HAZARD_HELMET.get();
                    boolean hasChest = player.getItemBySlot(EquipmentSlot.CHEST).getItem() == VoicesOfTheMines.HAZARD_CHESTPLATE.get();
                    boolean hasLegs = player.getItemBySlot(EquipmentSlot.LEGS).getItem() == VoicesOfTheMines.HAZARD_LEGGINGS.get();
                    boolean hasBoots = player.getItemBySlot(EquipmentSlot.FEET).getItem() == VoicesOfTheMines.HAZARD_BOOTS.get();

                    if (!(hasHelmet && hasChest && hasLegs && hasBoots)) {
                        player.addEffect(new MobEffectInstance(VoicesOfTheMines.RADIATION.get(), 100, 0, false, true, true));
                    }
                }
            }
        }
    }
}