package net.votmdevs.voicesofthemines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.sounds.SoundSource;
import net.votmdevs.voicesofthemines.entity.*;
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

import java.util.List;

@Mod.EventBusSubscriber(modid = VoicesOfTheMines.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VotmEventHandler {

    @SubscribeEvent
    public static void onSoundPlayPos(net.minecraftforge.event.PlayLevelSoundEvent.AtPosition event) {
        if (event.getLevel() instanceof ServerLevel level) {
            net.minecraft.sounds.SoundEvent sound = event.getSound() != null ? event.getSound().value() : null;
            if (sound != null) {
                net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity.recordAmbientSound(level, net.minecraft.core.BlockPos.containing(event.getPosition()), sound, event.getNewVolume(), event.getNewPitch());
            }
        }
    }

    @SubscribeEvent
    public static void onSoundPlayEntity(net.minecraftforge.event.PlayLevelSoundEvent.AtEntity event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() != null) {
            net.minecraft.sounds.SoundEvent sound = event.getSound() != null ? event.getSound().value() : null;
            if (sound != null) {
                net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity.recordAmbientSound(level, event.getEntity().blockPosition(), sound, event.getNewVolume(), event.getNewPitch());
            }
        }
    }

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
                entity instanceof RozitalShipEntity ||
                entity instanceof RozitalPyramidEntity ||
                entity instanceof SoltomiaEntity ||
                entity instanceof RozitalScoutEntity ||
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

    @SubscribeEvent
    public static void onCommandsRegister(net.minecraftforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("votmevent")
                .requires(s -> s.hasPermission(2))
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
                .then(net.minecraft.commands.Commands.literal("censorguy")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            Player player = context.getSource().getPlayerOrException();

                            CensorGuyEntity censor = VoicesOfTheMines.CENSOR_GUY.get().create(level);
                            if (censor != null) {
                                float yaw = player.getYRot() + (level.random.nextFloat() - 0.5f) * 60f;
                                float pitch = player.getXRot();

                                float f = (float)Math.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
                                float f1 = (float)Math.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
                                float f2 = (float)-Math.cos(-pitch * ((float)Math.PI / 180F));
                                float f3 = (float)Math.sin(-pitch * ((float)Math.PI / 180F));
                                net.minecraft.world.phys.Vec3 lookVec = new net.minecraft.world.phys.Vec3(f1 * f2, f3, f * f2).normalize();

                                // ИСПРАВЛЕНИЕ: Увеличили дистанцию до 35-60 блоков
                                double dist = 35 + level.random.nextDouble() * 25;
                                net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition();
                                net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookVec.scale(dist));

                                net.minecraft.world.phys.BlockHitResult hit = level.clip(new net.minecraft.world.level.ClipContext(eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, player));

                                net.minecraft.world.phys.Vec3 spawnPos;
                                if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                                    spawnPos = endPos;
                                } else {
                                    spawnPos = hit.getLocation().subtract(lookVec.scale(1.5));
                                }

                                int ty = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)spawnPos.x, (int)spawnPos.z);
                                double finalY = Math.max(spawnPos.y, ty + 0.2);

                                censor.moveTo(spawnPos.x, finalY, spawnPos.z, 0, 0);
                                level.addFreshEntity(censor);
                            }

                            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§8Censor Guy spawned in field of view..."), true);
                            return 1;
                        })
                )
                .then(net.minecraft.commands.Commands.literal("rozitals")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            Player player = context.getSource().getPlayerOrException();

                            RozitalShipEntity ship = VoicesOfTheMines.ROZITAL_SHIP.get().create(level);
                            if (ship != null) {
                                ship.moveTo(player.getX(), player.getY() + 60.0D, player.getZ(), player.getYRot(), 0.0F);
                                level.addFreshEntity(ship);
                            }

                            for (int i = 0; i < 3; i++) {
                                RozitalPyramidEntity pyramid = VoicesOfTheMines.ROZITAL_PYRAMID.get().create(level);
                                if (pyramid != null) {
                                    double ox = (level.random.nextDouble() - 0.5) * 100;
                                    double oz = (level.random.nextDouble() - 0.5) * 100;
                                    pyramid.moveTo(player.getX() + ox, player.getY() + 100, player.getZ() + oz, level.random.nextFloat() * 1080, 0);
                                    level.addFreshEntity(pyramid);
                                }
                            }

                            SoltomiaEntity soltomia = VoicesOfTheMines.SOLTOMIA.get().create(level);
                            if (soltomia != null) {
                                double ox = (level.random.nextDouble() - 0.5) * 20;
                                double oz = (level.random.nextDouble() - 0.5) * 20;
                                soltomia.moveTo(player.getX() + ox, player.getY() + 5, player.getZ() + oz, 0, 0);
                                level.addFreshEntity(soltomia);
                            }

                            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§dThe Rozitals have arrived!"), true);
                            return 1;
                        })
                )
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

    @SubscribeEvent
    public static void onPlayerTickDetector(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide() && event.player.tickCount % 40 == 0) {
            Player player = event.player;
            ServerLevel level = (ServerLevel) player.level();

            if (player.getMainHandItem().getItem() == VoicesOfTheMines.METAL_DETECTOR_ITEM.get() ||
                    player.getOffhandItem().getItem() == VoicesOfTheMines.METAL_DETECTOR_ITEM.get()) {

                java.util.List<TreasureSpotEntity> spots = level.getEntitiesOfClass(
                        TreasureSpotEntity.class,
                        player.getBoundingBox().inflate(50.0D)
                );

                if (spots.size() < 2) {
                    if (level.random.nextFloat() < 0.10f) {
                        double angle = level.random.nextDouble() * Math.PI * 2;
                        double dist = 15 + level.random.nextDouble() * 25;
                        int targetX = (int) (player.getX() + Math.cos(angle) * dist);
                        int targetZ = (int) (player.getZ() + Math.sin(angle) * dist);
                        int targetY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ) - 1;

                        BlockPos spawnPos = new BlockPos(targetX, targetY, targetZ);

                        if (level.getBlockState(spawnPos).getBlock() == Blocks.GRASS_BLOCK) {
                            TreasureSpotEntity spot = VoicesOfTheMines.TREASURE_SPOT.get().create(level);
                            if (spot != null) {
                                spot.moveTo(targetX + 0.5, targetY + 1.0, targetZ + 0.5, 0, 0);
                                float roll = level.random.nextFloat();
                                String lootId = "minecraft:dirt";

                                if (roll < 0.60f) {
                                    String[] common = {"minecraft:dirt", "voicesofthemines:trash_bag", "voicesofthemines:burger", "voicesofthemines:taco", "voicesofthemines:cheese", "voicesofthemines:toblerone", "minecraft:coal", "minecraft:cobblestone", "minecraft:raw_iron", "minecraft:raw_gold", "voicesofthemines:paper_sheet"};
                                    lootId = common[level.random.nextInt(common.length)];
                                } else if (roll < 0.90f) {
                                    String[] rare = {"voicesofthemines:disk_blue", "voicesofthemines:drive_box", "voicesofthemines:sign", "voicesofthemines:poster", "voicesofthemines:jacket", "voicesofthemines:glasses", "voicesofthemines:ribbon", "minecraft:diamond"};
                                    lootId = rare[level.random.nextInt(rare.length)];
                                } else {
                                    String[] veryRare = {"voicesofthemines:candle_handle", "voicesofthemines:maracas", "voicesofthemines:radioactive_capsule", "voicesofthemines:kerfur_part"};
                                    lootId = veryRare[level.random.nextInt(veryRare.length)];
                                }

                                spot.getEntityData().set(TreasureSpotEntity.LOOT_ID, lootId);
                                spot.getEntityData().set(TreasureSpotEntity.LOOT_COUNT, 1);
                                level.addFreshEntity(spot);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDigTreasure(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            Player player = event.getEntity();
            net.minecraft.world.item.ItemStack handItem = event.getItemStack();

            if (handItem.getItem() == net.minecraft.world.item.Items.IRON_SHOVEL) {
                BlockPos clickedPos = event.getPos();
                net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(clickedPos).inflate(1.0D);
                java.util.List<TreasureSpotEntity> spots = event.getLevel().getEntitiesOfClass(TreasureSpotEntity.class, searchBox);

                if (!spots.isEmpty()) {
                    spots.get(0).digUp();
                    handItem.hurtAndBreak(5, player, (p) -> p.broadcastBreakEvent(event.getHand()));
                    event.setCanceled(true);
                }
            }
        }
    }

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
                    event.getLevel().playSound(null, pos, selectedSplat, SoundSource.BLOCKS, 1.0F, 0.8F + event.getLevel().random.nextFloat() * 0.4F);
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

                if (manager.isCensorEventActive) {
                    manager.censorEventTimer--;
                    if (manager.censorEventTimer <= 0) {
                        manager.isCensorEventActive = false;
                        manager.setDirty();
                        net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SyncCensorStatePacket(false));
                    } else {
                        // CHAOS
                        if (level.getGameTime() % 15 == 0) {
                            for (Player p : level.players()) {
                                if (level.random.nextFloat() < 0.25f) {
                                    BlockPos center = p.blockPosition();
                                    java.util.List<BlockPos> interactableBlocks = new java.util.ArrayList<>();

                                    for (BlockPos rp : BlockPos.betweenClosed(center.offset(-10, -5, -10), center.offset(10, 5, 10))) {
                                        BlockState rs = level.getBlockState(rp);
                                        Block b = rs.getBlock();
                                        if (rs.hasProperty(BlockStateProperties.OPEN) ||
                                                rs.hasProperty(BlockStateProperties.POWERED) ||
                                                b instanceof net.minecraft.world.level.block.ChestBlock ||
                                                b instanceof net.minecraft.world.level.block.ShulkerBoxBlock ||
                                                b instanceof net.minecraft.world.level.block.EnderChestBlock ||
                                                b instanceof net.minecraft.world.level.block.BellBlock ||
                                                b instanceof net.minecraft.world.level.block.NoteBlock) {
                                            interactableBlocks.add(rp.immutable());
                                        }
                                    }

                                    if (!interactableBlocks.isEmpty()) {
                                        BlockPos targetPos = interactableBlocks.get(level.random.nextInt(interactableBlocks.size()));
                                        BlockState targetState = level.getBlockState(targetPos);
                                        Block targetBlock = targetState.getBlock();

                                        if (targetBlock instanceof net.minecraft.world.level.block.BellBlock bell) {
                                            // BELL
                                            bell.attemptToRing(level, targetPos, null);
                                        } else if (targetBlock instanceof net.minecraft.world.level.block.NoteBlock) {
                                            // NOTE BLOCK
                                            level.blockEvent(targetPos, targetBlock, 0, 0);
                                            level.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.BLOCKS, 3.0f, level.random.nextFloat() * 1.5f + 0.5f);
                                        } else if (targetState.hasProperty(BlockStateProperties.OPEN)) {
                                            // DOORS & TRAPDOORS
                                            boolean isOpening = !targetState.getValue(BlockStateProperties.OPEN);
                                            level.setBlock(targetPos, targetState.setValue(BlockStateProperties.OPEN, isOpening), 3);

                                            // SOUNDS
                                            if (targetBlock instanceof net.minecraft.world.level.block.DoorBlock) {
                                                level.playSound(null, targetPos, isOpening ? net.minecraft.sounds.SoundEvents.WOODEN_DOOR_OPEN : net.minecraft.sounds.SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1f);
                                            } else {
                                                level.playSound(null, targetPos, isOpening ? net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_OPEN : net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1f, 1f);
                                            }

                                            if (targetState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && targetState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                                                BlockState upper = level.getBlockState(targetPos.above());
                                                if (upper.getBlock() == targetBlock && upper.hasProperty(BlockStateProperties.OPEN)) {
                                                    level.setBlock(targetPos.above(), upper.setValue(BlockStateProperties.OPEN, isOpening), 3);
                                                }
                                            }
                                        } else if (targetState.hasProperty(BlockStateProperties.POWERED) && targetBlock instanceof net.minecraft.world.level.block.LeverBlock) {
                                            // LEVER
                                            level.setBlock(targetPos, targetState.setValue(BlockStateProperties.POWERED, !targetState.getValue(BlockStateProperties.POWERED)), 3);
                                            level.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1f, 1f);
                                        } else if (targetBlock instanceof net.minecraft.world.level.block.ChestBlock || targetBlock instanceof net.minecraft.world.level.block.ShulkerBoxBlock || targetBlock instanceof net.minecraft.world.level.block.EnderChestBlock) {
                                            // CHEST,SHULKERS
                                            level.blockEvent(targetPos, targetBlock, 1, 1);

                                            if (targetBlock instanceof net.minecraft.world.level.block.ShulkerBoxBlock) {
                                                level.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 1f, 1f);
                                            } else {
                                                level.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 1f);
                                            }

                                            // AUTO-CLOSE
                                            new java.util.Timer().schedule(new java.util.TimerTask() {
                                                @Override
                                                public void run() {
                                                    level.getServer().execute(() -> {
                                                        level.blockEvent(targetPos, targetBlock, 1, 0); // 0 = закрыть
                                                        if (targetBlock instanceof net.minecraft.world.level.block.ShulkerBoxBlock) {
                                                            level.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 1f, 1f);
                                                        } else {
                                                            level.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, 1f);
                                                        }
                                                    });
                                                }
                                            }, 750);
                                        }
                                    }
                                }
                            }
                        }

                        if (level.getGameTime() % 150 == 0 && !net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity.LOADED_RECORDERS.isEmpty()) {
                            if (level.random.nextFloat() < 0.5f) {
                                net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity be = net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity.LOADED_RECORDERS.get(level.random.nextInt(net.votmdevs.voicesofthemines.block.TapeRecorderBlockEntity.LOADED_RECORDERS.size()));
                                String[] msgs = {"HELP", "HELLO", "OK", "Y Y Y","I C U", "LOOK AT ME", "I M U", "O I C", "U N I", "C U SOON", "I NO U", "U R MI FREND?", "DONT BE AFRAID"};
                                be.playMessage(msgs[level.random.nextInt(msgs.length)]);
                            }
                        }
                    }
                    if (level.getGameTime() % 60 == 0) {
                        for (Player p : level.players()) {
                            if (level.random.nextFloat() < 0.15f) { // 15% шанс сыграть звук рядом с игроком
                                net.minecraft.sounds.SoundEvent[] scarySounds = {
                                        VotmSounds.CENSOR1.get(),
                                        VotmSounds.CENSOR2.get(),
                                        VotmSounds.CENSOR3.get()
                                };
                                net.minecraft.sounds.SoundEvent randomSound = scarySounds[level.random.nextInt(scarySounds.length)];

                                level.playSound(null, p.blockPosition(), randomSound, SoundSource.AMBIENT, 1.0f, level.random.nextFloat() * 0.2f + 0.9f);
                            }
                        }
                    }
                } else {
                    if (level.random.nextInt(200000) == 0) {
                        Player target = level.getRandomPlayer();
                        if (target != null) {
                            CensorGuyEntity censor = VoicesOfTheMines.CENSOR_GUY.get().create(level);
                            if (censor != null) {
                                float yaw = target.getYRot() + (level.random.nextFloat() - 0.5f) * 60f;
                                float pitch = target.getXRot();

                                float f = (float)Math.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
                                float f1 = (float)Math.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
                                float f2 = (float)-Math.cos(-pitch * ((float)Math.PI / 180F));
                                float f3 = (float)Math.sin(-pitch * ((float)Math.PI / 180F));
                                net.minecraft.world.phys.Vec3 lookVec = new net.minecraft.world.phys.Vec3(f1 * f2, f3, f * f2).normalize();

                                double dist = 35 + level.random.nextDouble() * 25;
                                net.minecraft.world.phys.Vec3 eyePos = target.getEyePosition();
                                net.minecraft.world.phys.Vec3 endPos = eyePos.add(lookVec.scale(dist));

                                net.minecraft.world.phys.BlockHitResult hit = level.clip(new net.minecraft.world.level.ClipContext(eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, target));

                                net.minecraft.world.phys.Vec3 spawnPos;
                                if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                                    spawnPos = endPos;
                                } else {
                                    spawnPos = hit.getLocation().subtract(lookVec.scale(1.5));
                                }

                                int ty = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)spawnPos.x, (int)spawnPos.z);
                                double finalY = Math.max(spawnPos.y, ty + 0.2);

                                censor.moveTo(spawnPos.x, finalY, spawnPos.z, 0, 0);
                                level.addFreshEntity(censor);
                            }
                        }
                    }
                }

                if (manager.isRozitalEventPending && manager.currentDay >= manager.rozitalEventTargetDay) {
                    long currentTime = level.getDayTime() % 24000;
                    if (currentTime >= manager.rozitalEventTargetTime) {
                        manager.isRozitalEventPending = false;
                        manager.setDirty();

                        Player target = level.getRandomPlayer();
                        if (target != null) {
                            RozitalShipEntity ship = VoicesOfTheMines.ROZITAL_SHIP.get().create(level);
                            if (ship != null) {
                                ship.moveTo(target.getX(), target.getY() + 30.0D, target.getZ(), target.getYRot(), 0.0F);
                                level.addFreshEntity(ship);
                            }
                            for (int i = 0; i < 3; i++) {
                                double ox = (level.random.nextDouble() - 0.5) * 100;
                                double oz = (level.random.nextDouble() - 0.5) * 100;
                                RozitalPyramidEntity pyramid = VoicesOfTheMines.ROZITAL_PYRAMID.get().create(level);
                                if (pyramid != null) {
                                    pyramid.moveTo(target.getX() + ox, target.getY() + 100, target.getZ() + oz, level.random.nextFloat() * 1080, 0);
                                    level.addFreshEntity(pyramid);
                                }
                            }
                            SoltomiaEntity soltomia = VoicesOfTheMines.SOLTOMIA.get().create(level);
                            if (soltomia != null) {
                                double ox = (level.random.nextDouble() - 0.5) * 20;
                                double oz = (level.random.nextDouble() - 0.5) * 20;
                                int targetX = (int) (target.getX() + ox);
                                int targetZ = (int) (target.getZ() + oz);
                                int groundY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
                                soltomia.moveTo(targetX + 0.5, groundY, targetZ + 0.5, 0, 0);
                                level.addFreshEntity(soltomia);
                            }
                        }
                    }
                }

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

    @SubscribeEvent
    public static void onPlayerWispTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (!event.player.level().isClientSide() && event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            net.minecraft.nbt.CompoundTag data = event.player.getPersistentData();
            if (data.contains("WispDoomTimer")) {
                int timer = data.getInt("WispDoomTimer");
                if (timer > 0) {
                    data.putInt("WispDoomTimer", timer - 1);
                    if (timer - 1 <= 0) {
                        event.player.kill();
                        data.remove("WispDoomTimer");
                    }
                }
            }
        }
    }

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