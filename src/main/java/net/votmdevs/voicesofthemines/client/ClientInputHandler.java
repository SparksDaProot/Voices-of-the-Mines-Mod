package net.votmdevs.voicesofthemines.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.votmdevs.voicesofthemines.client.gui.GmodNotificationManager;
import net.votmdevs.voicesofthemines.entity.FleshEntity;
import net.votmdevs.voicesofthemines.entity.GarbageEntity;
import net.votmdevs.voicesofthemines.entity.MaxwellEntity;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = VoicesOfTheMines.MODID, value = Dist.CLIENT)
public class ClientInputHandler {
    private static boolean wasUseKeyDown = false;
    private static Entity currentlyHeldEntity = null;

    private static boolean wasJumpKeyDown = false;
    private static boolean wasPickKeyDown = false;
    public static boolean IS_BAD_SUN = false;

    private static net.minecraft.client.resources.sounds.SoundInstance atvSoundInstance = null;
    private static String currentAtvSoundState = "none"; // none, idle, drive_start, drive_loop
    private static int atvSoundTimer = 0;

    public static BlockPos activeHackSafe = null;
    public static int hackDigitsUnlocked = 0;
    public static int tickUntilClick = 0;
    public static boolean clickWindowActive = false;
    public static CensorChaseSound censorChaseSound;

    public static int scoutStunTicks = 0;
    public static int scoutFlashTicks = 0;

    public static boolean IS_CENSOR_EVENT_ACTIVE = false;
    public static boolean isCensorShaking = false;
    public static float censorBlackoutAlpha = 0.0f;

    public static int evilEventTimer = -1;
    public static int funeralEventTimer = -1;
    public static int evilFlashTicks = 0;
    public static int evilDeathTimer = -1;
    public static int evilChatStage = 0;
    public static int evilChatTimer = -1;

    public static int sleepStage = 0;
    public static float sleepOverlayAlpha = 0.0f;
    public static int sleepTimer = 0;


    public static void startSleepAnimation() {
        sleepStage = 1;
        sleepTimer = 0;
        sleepOverlayAlpha = 0.0f;

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(VotmSounds.YAWN.get(), 1.0F, 1.0F);
        }
    }

    private static DroneLoopSound droneSoundInstance = null;
    private static TransformerLoopSound transformerSoundInstance = null;

    public static int knockdownTicks = 0;
    public static float vignetteAlpha = 0.0f;
    private static int detectorBeepTimer = 0;

    private static float lockedYaw = 0f;
    private static float lockedPitch = 0f;
    private static float targetRoll = 0f;
    private static float targetPitch = 0f;
    private static float targetYawOffset = 0f;

    public static void triggerKnockdown() {
        knockdownTicks = 80;
        vignetteAlpha = 1.0f;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            lockedYaw = mc.player.getYRot();
            lockedPitch = mc.player.getXRot();
            Random rand = new Random();
            targetRoll = (50f + rand.nextFloat() * 35f) * (rand.nextBoolean() ? 1 : -1);
            targetPitch = -40f + rand.nextFloat() * 120f;
            targetYawOffset = (rand.nextFloat() - 0.5f) * 60f;
            mc.player.playSound(VotmSounds.FALLDEATH.get(), 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(net.minecraftforge.client.event.InputEvent.MouseScrollingEvent event) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        if (activeHackSafe != null) {
            if (event.getScrollDelta() != 0) {
                if (!clickWindowActive) {
                    tickUntilClick--;
                    if (tickUntilClick <= 0) {
                        clickWindowActive = true;
                        mc.player.playSound(net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN, 1.0F, 2.0F);
                        tickUntilClick = 30;
                    }
                }
            }
            event.setCanceled(true);
            return;
        }

        // HOOK
        if (mc.player.getMainHandItem().getItem() == VoicesOfTheMines.HOOK_ITEM.get()) {
            double scroll = -event.getScrollDelta();
            if (scroll != 0) {
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.HookPullPacket(scroll));
                event.setCanceled(true);
            }
            return;
        }

        // RADIO
        if (mc.player.isShiftKeyDown() && mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
            net.minecraft.core.BlockPos pos = blockHit.getBlockPos();
            if (mc.level != null && mc.level.getBlockState(pos).getBlock() == VoicesOfTheMines.RADIO_BLOCK.get()) {
                double delta = event.getScrollDelta();
                int action = delta > 0 ? 2 : 3; //volume

                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.RadioActionPacket(pos, action, ""));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClickHook(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.player != null && mc.player.getMainHandItem().getItem() == VoicesOfTheMines.HOOK_ITEM.get()) {
            KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.HookDetachPacket());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (scoutStunTicks > 0) scoutStunTicks--;
        if (scoutFlashTicks > 0) scoutFlashTicks--;

// STETO
        boolean isUsingStethoscope = mc.options.keyUse.isDown() && mc.player.isShiftKeyDown() && mc.player.getMainHandItem().getItem() == VoicesOfTheMines.STETOSCOPE.get();
        BlockPos lookingAtSafe = null;

        if (isUsingStethoscope && mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
            if (mc.level.getBlockState(blockHit.getBlockPos()).getBlock() == VoicesOfTheMines.SAFE_BLOCK.get()) {
                net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(blockHit.getBlockPos());
                if (be instanceof net.votmdevs.voicesofthemines.block.SafeBlockEntity safe && safe.doorState == 0 && !safe.passcode.isEmpty()) {
                    lookingAtSafe = blockHit.getBlockPos();
                }
            }
        }

        if (lookingAtSafe != null) {
            if (activeHackSafe == null || !activeHackSafe.equals(lookingAtSafe)) {
                // hack
                activeHackSafe = lookingAtSafe;
                hackDigitsUnlocked = 0;
                clickWindowActive = false;
                tickUntilClick = 60 + mc.player.getRandom().nextInt(80);
                GmodNotificationManager.addNotification("Hack started. Keep holding Shift+RightClick, scroll mouse wheel and listen...");
            }

            if (mc.player.getRandom().nextInt(5) == 0) {
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        activeHackSafe.getX() + 0.5, activeHackSafe.getY() + 0.5, activeHackSafe.getZ() + 0.5,
                        (mc.player.getRandom().nextDouble() - 0.5) * 0.05, 0.05, (mc.player.getRandom().nextDouble() - 0.5) * 0.05);
            }

            if (clickWindowActive) {
                tickUntilClick--;
                if (tickUntilClick <= 0) {
                    // ПРОПУСТИЛ!
                    activeHackSafe = null;
                    GmodNotificationManager.addNotification("Hack failed! Too slow.");
                    mc.player.playSound(VotmSounds.DENY.get(), 1.0F, 1.0F);
                }
            }
        } else {
            if (activeHackSafe != null) {
                activeHackSafe = null;
                GmodNotificationManager.addNotification("Hack aborted.");
            }
        }
        //TRANSFORMERS

        BlockPos closestActiveTransformer = null;
        double closestTransformerDist = 15.0 * 15.0;
        BlockPos playerPos = mc.player.blockPosition();

        for (BlockPos checkPos : BlockPos.betweenClosed(playerPos.offset(-10, -10, -10), playerPos.offset(10, 10, 10))) {
            if (mc.level.getBlockState(checkPos).getBlock() == VoicesOfTheMines.TRANSFORMER_BLOCK.get()) {
                net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(checkPos);
                if (be instanceof net.votmdevs.voicesofthemines.block.TransformerBlockEntity tr) {
                    boolean isOn = tr.isMain ? tr.isActive : (tr.mainTransformerPos != null && !tr.needsReboot && tr.isNetworkActive);
                    if (isOn) {
                        double dist = checkPos.distToCenterSqr(mc.player.position());
                        if (dist < closestTransformerDist) {
                            closestTransformerDist = dist;
                            closestActiveTransformer = checkPos.immutable();
                        }
                    }
                }
            }
        }

        if (closestActiveTransformer != null) {
            if (transformerSoundInstance == null || !mc.getSoundManager().isActive(transformerSoundInstance)) {
                transformerSoundInstance = new TransformerLoopSound(closestActiveTransformer);
                mc.getSoundManager().play(transformerSoundInstance);
            } else {
                transformerSoundInstance.updatePos(closestActiveTransformer);
            }
        } else {
            if (transformerSoundInstance != null) {
                mc.getSoundManager().stop(transformerSoundInstance);
                transformerSoundInstance = null;
            }
        }


        if (sleepStage == 1) {
            sleepTimer++;
            sleepOverlayAlpha = (float) Math.max(0, Math.sin(sleepTimer * 0.2f));

            if (sleepTimer > 48) {
                sleepStage = 2;
                sleepTimer = 0;
            }
        }
        else if (sleepStage == 2) {
            sleepOverlayAlpha += 0.02f;
            if (sleepOverlayAlpha >= 1.0f) {
                sleepOverlayAlpha = 1.0f;
                sleepStage = 3;
                sleepTimer = 0;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.ForceSleepTimeSkipPacket());
            }
        }
        else if (sleepStage == 3) {
            sleepTimer++;
            if (sleepTimer > 60) {
                sleepStage = 4;
            }
        }
        else if (sleepStage == 4) {
            sleepOverlayAlpha -= 0.01f;
            if (sleepOverlayAlpha <= 0.0f) {
                sleepOverlayAlpha = 0.0f;
                sleepStage = 0;
            }
        }


        if (evilEventTimer > 0) {
            evilEventTimer--;
            if (evilEventTimer == 0) {
                evilFlashTicks = 15;
                evilDeathTimer = 10;
                mc.player.playSound(VotmSounds.EVIL_SCREAM.get(), 1.0F, 1.0F);
            }
        }
        if (evilDeathTimer > 0) {
            evilDeathTimer--;
            if (evilDeathTimer == 0) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.TriggerEvilDeathPacket());

                evilChatStage = 1;
                evilChatTimer = 15;
            }
        }

        if (evilChatTimer > 0) {
            evilChatTimer--;
            if (evilChatTimer == 0) {
                if (evilChatStage == 1) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(">START").withStyle(ChatFormatting.AQUA), false);
                    evilChatStage++;
                    evilChatTimer = 20; // 1 сек
                } else if (evilChatStage == 2) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(">OBJECT: ELIMINATED").withStyle(ChatFormatting.AQUA), false);
                    evilChatStage++;
                    evilChatTimer = 20;
                } else if (evilChatStage == 3) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(">PLANET: EARTH").withStyle(ChatFormatting.AQUA), false);
                    evilChatStage++;
                    evilChatTimer = 20;
                } else if (evilChatStage == 4) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(">STATUS: DETECTED").withStyle(ChatFormatting.AQUA), false);
                    evilChatStage = 0;
                }
            }
        }

        if (evilFlashTicks > 0) evilFlashTicks--;

        if (funeralEventTimer > 0) {
            funeralEventTimer--;
            if (funeralEventTimer == 0) {
                GmodNotificationManager.addNotification("don't turn around...");
            }
        }
        // beep beep beep
        boolean holdingDetector = mc.player.getMainHandItem().getItem() == VoicesOfTheMines.METAL_DETECTOR_ITEM.get() ||
                mc.player.getOffhandItem().getItem() == VoicesOfTheMines.METAL_DETECTOR_ITEM.get();

        if (holdingDetector) {
            if (detectorBeepTimer > 0) {
                detectorBeepTimer--;
            } else {
                double closestDist = Double.MAX_VALUE;
                for (Entity e : mc.level.getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.TreasureSpotEntity.class, mc.player.getBoundingBox().inflate(20.0D))) {
                    double d = e.distanceToSqr(mc.player);
                    if (d < closestDist) {
                        closestDist = d;
                    }
                }

                int interval;
                if (closestDist <= 5.0 * 5.0) {
                    interval = 4; // 0.2 sec
                } else if (closestDist <= 10.0 * 10.0) {
                    interval = 10; // 0.5 sec
                } else if (closestDist <= 20.0 * 20.0) {
                    interval = 20; // 1 sec
                } else {
                    interval = 40; // 2 sec
                }

                mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.DETECTBEEP.get(), 1.0F, 0.5F));

                detectorBeepTimer = interval;
            }
        } else {
            detectorBeepTimer = 0;
        }

        net.votmdevs.voicesofthemines.entity.AtvEntity activeAtv = null;
        if (mc.player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv) {
            activeAtv = atv;
        } else {
            double closestDist = 15.0 * 15.0;
            for (Entity e : mc.level.getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.AtvEntity.class, mc.player.getBoundingBox().inflate(15.0D))) {
                if (((net.votmdevs.voicesofthemines.entity.AtvEntity) e).isEngineOn()) {
                    double dist = e.distanceToSqr(mc.player);
                    if (dist < closestDist) {
                        closestDist = dist;
                        activeAtv = (net.votmdevs.voicesofthemines.entity.AtvEntity) e;
                    }
                }
            }
        }

        if (IS_BAD_SUN && mc.level.isDay()) {
            net.minecraft.core.BlockPos eyePos = net.minecraft.core.BlockPos.containing(mc.player.getEyePosition());
            if (mc.level.canSeeSky(eyePos)) {

                float celestialAngle = mc.level.getTimeOfDay(1.0F);

                float targetYaw = (celestialAngle > 0.25F && celestialAngle < 0.75F) ? 90.0F : -90.0F;

                float targetPitch = (float) (Math.cos(celestialAngle * Math.PI * 2.0) * -90.0);

                float currentYaw = mc.player.getYRot();
                float currentPitch = mc.player.getXRot();

                float yawDiff = net.minecraft.util.Mth.wrapDegrees(targetYaw - currentYaw);
                float pitchDiff = targetPitch - currentPitch;

                mc.player.setYRot(currentYaw + yawDiff * 0.1F);
                mc.player.setXRot(currentPitch + pitchDiff * 0.1F);
            }
        }

        if (activeAtv != null) {
            float speed = Math.abs(activeAtv.getEntityData().get(net.votmdevs.voicesofthemines.entity.AtvEntity.CURRENT_SPEED));

            if (speed < 0.05f) {
                if (!currentAtvSoundState.equals("idle")) {
                    currentAtvSoundState = "idle";
                    if (atvSoundInstance != null) mc.getSoundManager().stop(atvSoundInstance);

                    atvSoundInstance = new AtvLoopSound(activeAtv, VotmSounds.IDLE.get());
                    mc.getSoundManager().play(atvSoundInstance);
                }
            } else {
                if (currentAtvSoundState.equals("idle") || currentAtvSoundState.equals("none")) {
                    currentAtvSoundState = "drive_start";
                    if (atvSoundInstance != null) mc.getSoundManager().stop(atvSoundInstance);

                    atvSoundInstance = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(VotmSounds.ATV_DRIVE_START.get(), 1.0F, 1.0F);
                    mc.getSoundManager().play(atvSoundInstance);
                    atvSoundTimer = 60;
                }

                if (currentAtvSoundState.equals("drive_start")) {
                    if (atvSoundTimer > 0) {
                        atvSoundTimer--;
                    } else if (atvSoundInstance == null || !mc.getSoundManager().isActive(atvSoundInstance)) {
                        currentAtvSoundState = "drive_loop";
                        if (atvSoundInstance != null) mc.getSoundManager().stop(atvSoundInstance);

                        atvSoundInstance = new AtvLoopSound(activeAtv, VotmSounds.ATV_DRIVE_LOOP.get());
                        mc.getSoundManager().play(atvSoundInstance);
                    }
                }
            }
        } else {
            if (atvSoundInstance != null) {
                mc.getSoundManager().stop(atvSoundInstance);
                atvSoundInstance = null;
            }
            currentAtvSoundState = "none";
        }

        net.votmdevs.voicesofthemines.entity.DroneEntity activeDrone = null;
        double closestDroneDist = Double.MAX_VALUE;
        for (Entity e : mc.level.getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.DroneEntity.class, mc.player.getBoundingBox().inflate(300.0D))) {
            double dist = e.distanceToSqr(mc.player);
            if (dist < closestDroneDist) {
                closestDroneDist = dist;
                activeDrone = (net.votmdevs.voicesofthemines.entity.DroneEntity) e;
            }
        }

        if (activeDrone != null) {
            if (droneSoundInstance == null || !mc.getSoundManager().isActive(droneSoundInstance)) {
                droneSoundInstance = new DroneLoopSound(activeDrone, VotmSounds.DRONE_AMBIENT.get());
                mc.getSoundManager().play(droneSoundInstance);
            }
        } else {
            if (droneSoundInstance != null) {
                mc.getSoundManager().stop(droneSoundInstance);
                droneSoundInstance = null;
            }
        }

        if (mc.player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity) {
            boolean isJumpKeyDown = mc.options.keyJump.isDown();
            if (isJumpKeyDown && !wasJumpKeyDown) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.AtvBrakePacket(true));
            } else if (!isJumpKeyDown && wasJumpKeyDown) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.AtvBrakePacket(false));
            }
            wasJumpKeyDown = isJumpKeyDown;
        }

        if (knockdownTicks > 0) knockdownTicks--;
        if (vignetteAlpha > 0) vignetteAlpha -= 0.015f;

        boolean isUseKeyDown = mc.options.keyUse.isDown();
        boolean isPickKeyDown = mc.options.keyPickItem.isDown();

        if (isUseKeyDown && !wasUseKeyDown && knockdownTicks == 0) {
            Entity target = mc.crosshairPickEntity;
            if (target instanceof FleshEntity flesh && flesh.getFleshLevel() < 5) {
                currentlyHeldEntity = flesh;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(flesh.getId(), true));
            } else if (target instanceof GarbageEntity garbage && garbage.getGarbageLevel() < 5) {
                currentlyHeldEntity = garbage;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(garbage.getId(), true));
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.WashSpongeEntity sponge) {
                currentlyHeldEntity = sponge;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(sponge.getId(), true));
            } else if (target instanceof MaxwellEntity maxwell) {
                if (!mc.player.isShiftKeyDown()) {
                    currentlyHeldEntity = maxwell;
                    KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(maxwell.getId(), true));
                }
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                currentlyHeldEntity = drive;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(drive.getId(), true));
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.AbstractMannequinEntity mannequin) {
                if (!mc.player.isShiftKeyDown()) {
                    currentlyHeldEntity = mannequin;
                    KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(mannequin.getId(), true));
                }
            }

        } else if (!isUseKeyDown && wasUseKeyDown) {
            if (currentlyHeldEntity != null && !(currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity)) {

                if (currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                    BlockPos targetPos = null;
                    net.minecraft.world.level.block.Block targetBlock = null;

                    // check 5 block through (for correct ... i forgor word)
                    net.minecraft.world.phys.HitResult blockRay = mc.player.pick(5.0D, 1.0F, false);

                    if (blockRay.getType() != net.minecraft.world.phys.HitResult.Type.MISS && blockRay instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                        BlockPos hitPos = blockHit.getBlockPos();
                        net.minecraft.world.level.block.Block b = mc.level.getBlockState(hitPos).getBlock();

                        if (b == VoicesOfTheMines.TERMINAL_CHECK.get() ||
                                b == VoicesOfTheMines.TERMINAL_PROCESSING.get() ||
                                b == VoicesOfTheMines.DRIVE_BOX.get()) {

                            targetPos = hitPos;
                            targetBlock = b;
                        }
                    }

                    // magnet drives to box
                    if (targetPos == null) {
                        BlockPos center = mc.player.blockPosition();
                        double closestDist = 999.0;

                        for (BlockPos checkPos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 2, 1))) {
                            net.minecraft.world.level.block.Block b = mc.level.getBlockState(checkPos).getBlock();

                            if (b == VoicesOfTheMines.TERMINAL_CHECK.get() ||
                                    b == VoicesOfTheMines.TERMINAL_PROCESSING.get() ||
                                    b == VoicesOfTheMines.DRIVE_BOX.get()) {

                                // block searching (another check)
                                double dist = checkPos.distToCenterSqr(mc.player.getEyePosition());
                                if (dist < closestDist) {
                                    closestDist = dist;
                                    targetPos = checkPos.immutable();
                                    targetBlock = b;
                                }
                            }
                        }
                    }

                    // Server packet
                    if (targetPos != null && targetBlock != null) {
                        String sigId = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID);
                        boolean isEmpty = (sigId == null || sigId.isEmpty());

                        if (targetBlock == VoicesOfTheMines.TERMINAL_PROCESSING.get() && isEmpty) {
                            mc.player.playSound(VotmSounds.BUG_ALERT.get(), 1.0F, 0.5F);
                        } else {
                            KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.InsertDrivePacket(targetPos, drive.getId()));
                        }
                    }
                }

                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(0, false));
                currentlyHeldEntity = null;
            }
        }
        wasUseKeyDown = isUseKeyDown;

        if (isPickKeyDown && !wasPickKeyDown && knockdownTicks == 0) {
            Entity target = mc.crosshairPickEntity;
            if (target instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv && !atv.isVehicle()) {
                currentlyHeldEntity = atv;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(atv.getId(), true));
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.FuelCanEntity fuelCan) {
                currentlyHeldEntity = fuelCan;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(fuelCan.getId(), true));
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.WashSpongeEntity sponge) {
                currentlyHeldEntity = sponge;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(sponge.getId(), true));
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                currentlyHeldEntity = drive;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(drive.getId(), true));
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.AbstractMannequinEntity mannequin) {
                currentlyHeldEntity = mannequin;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(mannequin.getId(), true));
            }
        } else if (!isPickKeyDown && wasPickKeyDown) {
            if (currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity ||
                    currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.FuelCanEntity ||
                    currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.WashSpongeEntity ||
                    currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.DriveEntity ||
                    currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.AbstractMannequinEntity) {

                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(0, false));
                currentlyHeldEntity = null;
            }
        }
        wasPickKeyDown = isPickKeyDown;
    }

    @SubscribeEvent
    public static void onMiddleClick(net.minecraftforge.client.event.InputEvent.MouseButton event) {
        if (event.getButton() == 2 && event.getAction() == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
            if (activeHackSafe != null) {
                if (clickWindowActive) {
                    hackDigitsUnlocked++;
                    clickWindowActive = false;

                    Minecraft.getInstance().player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1.0F);

                    if (hackDigitsUnlocked >= 4) {
                        KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.HackSafePacket(activeHackSafe));
                        activeHackSafe = null;
                        GmodNotificationManager.addNotification("Hack Successful!");
                    } else {
                        tickUntilClick = 60 + new Random().nextInt(80);
                        GmodNotificationManager.addNotification("Digit " + hackDigitsUnlocked + "/4 unlocked...");
                    }
                } else {
                    activeHackSafe = null;
                    GmodNotificationManager.addNotification("Hack failed! Missed the timing.");
                    Minecraft.getInstance().player.playSound(VotmSounds.DENY.get(), 1.0F, 1.0F);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (knockdownTicks > 0 || sleepStage > 0 || scoutStunTicks > 0) {
            event.getInput().forwardImpulse = 0;
            event.getInput().leftImpulse = 0;
            event.getInput().up = false;
            event.getInput().down = false;
            event.getInput().left = false;
            event.getInput().right = false;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }
    }

    @SubscribeEvent
    public static void onRenderSky(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() == net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY) {
            Minecraft mc = Minecraft.getInstance();

            if (IS_BAD_SUN && mc.level != null && mc.level.isDay()) {
                com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();

                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(mc.level.getTimeOfDay(event.getPartialTick()) * 360.0F));

                org.joml.Matrix4f matrix4f = poseStack.last().pose();
                com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
                com.mojang.blaze3d.vertex.BufferBuilder bufferbuilder = tesselator.getBuilder();

                ResourceLocation BAD_SUN_TEXTURE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/environment/bad_sun.png");
                com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
                com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, BAD_SUN_TEXTURE);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                float size = 6.0F;
                bufferbuilder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex(matrix4f, -size, 100.0F, -size).uv(0.0F, 0.0F).endVertex();
                bufferbuilder.vertex(matrix4f, size, 100.0F, -size).uv(1.0F, 0.0F).endVertex();
                bufferbuilder.vertex(matrix4f, size, 100.0F, size).uv(1.0F, 1.0F).endVertex();
                bufferbuilder.vertex(matrix4f, -size, 100.0F, size).uv(0.0F, 1.0F).endVertex();
                tesselator.end();

                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                poseStack.popPose();
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (knockdownTicks > 0) {
            event.setCanceled(true);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.crosshairPickEntity instanceof GarbageEntity garbage) {
            if (mc.player != null && mc.player.getMainHandItem().getItem() == VoicesOfTheMines.TRASH_ROLL.get()) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.PackGarbagePacket(garbage.getId()));
                event.setSwingHand(true);
                event.setCanceled(true);
            } else if (garbage.getGarbageLevel() > 1 && !garbage.isHeld()) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.ExtractGarbagePacket(garbage.getId()));
                event.setSwingHand(true);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (isCensorShaking) {
            float shake = (float)(Math.random() - 0.5) * 6.0f;
            event.setPitch(event.getPitch() + shake);
            event.setYaw(event.getYaw() + shake);
        }
        if (knockdownTicks > 0) {
            float progress = (80 - knockdownTicks) / 80.0f;
            float roll = 0, pitch = event.getPitch(), yaw = event.getYaw();
            float dropOffset = 0, targetDrop = 1.4f;

            if (scoutStunTicks > 0) {
                float shake = (float)(Math.random() - 0.5) * 5.0f; // Тряска 5 градусов
                event.setPitch(-90.0F + shake); // Смотрим ровно вверх (-90)
                event.setYaw(event.getYaw() + shake);
            }

            if (progress < 0.2f) {
                float p = progress / 0.2f;
                float ease = p * p * (3 - 2 * p);
                roll = net.minecraft.util.Mth.lerp(ease, 0, targetRoll);
                pitch = net.minecraft.util.Mth.lerp(ease, lockedPitch, targetPitch);
                yaw = net.minecraft.util.Mth.lerp(ease, lockedYaw, lockedYaw + targetYawOffset);
                dropOffset = net.minecraft.util.Mth.lerp(ease, 0f, targetDrop);
            } else if (progress < 0.7f) {
                float timeLying = progress - 0.2f;
                roll = targetRoll + (float) Math.sin(timeLying * 25) * 1.5f;
                pitch = targetPitch + (float) Math.cos(timeLying * 15) * 1.5f;
                yaw = lockedYaw + targetYawOffset;
                dropOffset = targetDrop + (float) Math.sin(timeLying * 20) * 0.05f;
            } else {
                float p = (progress - 0.7f) / 0.3f;
                float ease = p * p * (3 - 2 * p);
                roll = net.minecraft.util.Mth.lerp(ease, targetRoll, 0);
                pitch = net.minecraft.util.Mth.lerp(ease, targetPitch, event.getPitch());
                yaw = net.minecraft.util.Mth.lerp(ease, lockedYaw + targetYawOffset, event.getYaw());
                dropOffset = net.minecraft.util.Mth.lerp(ease, targetDrop, 0f);
            }

            event.setRoll(roll);
            event.setPitch(pitch);
            event.setYaw(yaw);

            net.minecraft.client.Camera camera = event.getCamera();
            try {
                java.lang.reflect.Method setPos = net.minecraft.client.Camera.class.getDeclaredMethod("setPosition", double.class, double.class, double.class);
                setPos.setAccessible(true);
                setPos.invoke(camera, camera.getPosition().x, camera.getPosition().y - dropOffset, camera.getPosition().z);
            } catch (Exception e) {
                try {
                    java.lang.reflect.Method setPosSrg = net.minecraft.client.Camera.class.getDeclaredMethod("m_90584_", double.class, double.class, double.class);
                    setPosSrg.setAccessible(true);
                    setPosSrg.invoke(camera, camera.getPosition().x, camera.getPosition().y - dropOffset, camera.getPosition().z);
                } catch (Exception ex) {
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.MOUNT_HEALTH.type()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getVehicle() instanceof net.votmdevs.voicesofthemines.entity.AtvEntity) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        if (event.getOverlay() == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.type()) {
            if (scoutFlashTicks > 0) {
                ResourceLocation PURPLE_OVERLAY = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/purpleoverlay.png");
                float alpha = scoutFlashTicks / 60.0f; // Плавно исчезает с 1.0 до 0.0

                com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

                event.getGuiGraphics().blit(PURPLE_OVERLAY, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
            // CENSOR GUY
            if (censorBlackoutAlpha > 0) {
                ResourceLocation BLACK_OVERLAY = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/blackoverlay.png");

                com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, censorBlackoutAlpha);

                event.getGuiGraphics().blit(BLACK_OVERLAY, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();

                censorBlackoutAlpha -= 0.005f;
            }

            //SLEEP
            if (sleepStage > 0 && sleepOverlayAlpha > 0) {
                ResourceLocation BLACK_OVERLAY = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/blackoverlay.png");

                com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, sleepOverlayAlpha);

                event.getGuiGraphics().blit(BLACK_OVERLAY, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }

            // vignette
            if (vignetteAlpha > 0) {
                ResourceLocation VIGNETTE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/damage_vignette.png");
                com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, vignetteAlpha);

                event.getGuiGraphics().blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }

            // evil
            if (evilFlashTicks > 0) {
                ResourceLocation EVIL_OVERLAY = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/evil_redoverlay.png");

                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0, 0, 500);

                com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F);

                event.getGuiGraphics().blit(EVIL_OVERLAY, 0, 0, 0, 0.0F, 0.0F, width, height, width, height);

                com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
                com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();

                event.getGuiGraphics().pose().popPose();
            }
        }


        if (event.getOverlay() == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.type()) {
            if (mc.crosshairPickEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv && (mc.player == null || mc.player.getVehicle() != atv)) {
                int boxWidth = 110;
                int boxHeight = 55;
                int x = width - boxWidth - 15;
                int y = (height - boxHeight) / 2;

                event.getGuiGraphics().fill(x, y, x + boxWidth, y + boxHeight, 0xDD222222);

                String fuelStr = String.format(java.util.Locale.US, "Fuel: %.1f", atv.getEntityData().get(net.votmdevs.voicesofthemines.entity.AtvEntity.FUEL));
                String hpStr = String.format(java.util.Locale.US, "Health: %.1f", atv.getHealth());
                boolean brakeStatus = !atv.isEngineOn() || atv.getEntityData().get(net.votmdevs.voicesofthemines.entity.AtvEntity.IS_BRAKING);
                String brakeStr = "Brake: " + (brakeStatus ? "True" : "False");

                event.getGuiGraphics().drawString(mc.font, fuelStr, x + 10, y + 10, 0xFFFFFF, false);
                event.getGuiGraphics().drawString(mc.font, hpStr, x + 10, y + 25, 0xFFFFFF, false);
                event.getGuiGraphics().drawString(mc.font, brakeStr, x + 10, y + 40, 0xFFFFFF, false);
            }

            // drone panel/transformer overlay
            if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                Block targetBlock = mc.level.getBlockState(blockHit.getBlockPos()).getBlock();

                // Drone panel
                if (targetBlock == VoicesOfTheMines.DRONE_PANEL.get()) {
                    int boxWidth = 140;
                    int boxHeight = 55;
                    int x = width - boxWidth - 15;
                    int y = (height - boxHeight) / 2;

                    event.getGuiGraphics().fill(x, y, x + boxWidth, y + boxHeight, 0xDD222222);

                    net.votmdevs.voicesofthemines.entity.DroneEntity activeDrone = null;
                    double closestDist = Double.MAX_VALUE;
                    for (Entity e : mc.level.getEntitiesOfClass(net.votmdevs.voicesofthemines.entity.DroneEntity.class, mc.player.getBoundingBox().inflate(300.0D))) {
                        double d = e.distanceTo(mc.player);
                        if (d < closestDist) {
                            closestDist = d;
                            activeDrone = (net.votmdevs.voicesofthemines.entity.DroneEntity) e;
                        }
                    }

                    if (activeDrone != null) {
                        event.getGuiGraphics().drawString(mc.font, "Drone is active", x + 10, y + 10, 0x55FF55, false);
                        event.getGuiGraphics().drawString(mc.font, String.format("Distance: %.1f blocks", closestDist), x + 10, y + 25, 0xFFFFFF, false);

                        int state = activeDrone.getEntityData().get(net.votmdevs.voicesofthemines.entity.DroneEntity.STATE);
                        String statusStr = "Status: Unknown";
                        if (state == 0) statusStr = "Status: Approaching";
                        else if (state == 1) statusStr = "Status: Descending";
                        else if (state == 2) statusStr = "Status: Waiting";
                        else if (state == 3) statusStr = "Status: Ascending";
                        else if (state == 4) statusStr = "Status: Returning";

                        event.getGuiGraphics().drawString(mc.font, statusStr, x + 10, y + 40, 0xFFAA00, false);
                    } else {
                        event.getGuiGraphics().drawString(mc.font, "Drone is inactive", x + 10, y + 10, 0xFF5555, false);
                        event.getGuiGraphics().drawString(mc.font, "Distance: N/A", x + 10, y + 25, 0x888888, false);
                        event.getGuiGraphics().drawString(mc.font, "Status: Standby", x + 10, y + 40, 0x888888, false);
                    }
                } else if (targetBlock == VoicesOfTheMines.TRANSFORMER_BLOCK.get()) {
                    net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(blockHit.getBlockPos());
                    if (be instanceof net.votmdevs.voicesofthemines.block.TransformerBlockEntity transformer) {
                        int x = 15;
                        int y = (height - 40) / 2;
                        event.getGuiGraphics().fill(x - 5, y - 5, x + 150, y + 35, 0xDD222222);

                        if (transformer.isMain) {
                            event.getGuiGraphics().drawString(mc.font, "Main Transformer", x, y, 0xFFFFFF, false);
                            event.getGuiGraphics().drawString(mc.font, "Power: " + transformer.energy + "%", x, y + 12, 0xFFFF55, false);
                            event.getGuiGraphics().drawString(mc.font, "Connected to " + transformer.connectedDevices.size() + " devices", x, y + 24, 0x55FFFF, false);
                        } else {
                            event.getGuiGraphics().drawString(mc.font, "Secondary Transformer", x, y, 0xAAAAAA, false);
                            if (transformer.mainTransformerPos != null) {
                                event.getGuiGraphics().drawString(mc.font, "Power: " + transformer.energy + "%", x, y + 12, 0xFFFF55, false);
                            } else {
                                event.getGuiGraphics().drawString(mc.font, "Unlinked", x, y + 12, 0xFF5555, false);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class AtvLoopSound extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        private final net.votmdevs.voicesofthemines.entity.AtvEntity atv;
        public AtvLoopSound(net.votmdevs.voicesofthemines.entity.AtvEntity atv, net.minecraft.sounds.SoundEvent sound) {
            super(sound, net.minecraft.sounds.SoundSource.PLAYERS, net.minecraft.util.RandomSource.create());
            this.atv = atv; this.looping = true; this.delay = 0; this.volume = 1.0F; this.pitch = 1.0F;
            this.x = atv.getX(); this.y = atv.getY(); this.z = atv.getZ();
        }
        @Override
        public void tick() {
            if (!this.atv.isAlive() || !this.atv.isEngineOn()) this.stop();
            else { this.x = this.atv.getX(); this.y = this.atv.getY(); this.z = this.atv.getZ(); }
        }
    }
    public static class CensorChaseSound extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        private boolean isFadingOut = false;

        public CensorChaseSound() {
            super(net.votmdevs.voicesofthemines.VotmSounds.CENSORATTACK.get(), net.minecraft.sounds.SoundSource.HOSTILE, net.minecraft.util.RandomSource.create());
            this.looping = true; // Зацикливаем
            this.delay = 0;
            this.volume = 1.0f;
            this.relative = true; // Звук играет "в голове", как фоновая музыка
        }

        public void fadeOut() {
            this.isFadingOut = true;
        }

        @Override
        public void tick() {
            if (this.isFadingOut) {
                this.volume -= 0.02f;
                if (this.volume <= 0.0f) {
                    this.stop();
                }
            }
        }
    }

    public static class DroneLoopSound extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        private final net.votmdevs.voicesofthemines.entity.DroneEntity drone;
        public DroneLoopSound(net.votmdevs.voicesofthemines.entity.DroneEntity drone, net.minecraft.sounds.SoundEvent sound) {
            super(sound, net.minecraft.sounds.SoundSource.NEUTRAL, net.minecraft.util.RandomSource.create());
            this.drone = drone; this.looping = true; this.delay = 0; this.volume = 2.0F; this.pitch = 1.0F;
            this.x = drone.getX(); this.y = drone.getY(); this.z = drone.getZ();
        }
        @Override
        public void tick() {
            if (!this.drone.isAlive()) this.stop();
            else { this.x = this.drone.getX(); this.y = this.drone.getY(); this.z = this.drone.getZ(); }
        }
    }

    public static class TransformerLoopSound extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        private BlockPos pos;
        public TransformerLoopSound(BlockPos pos) {
            super(VotmSounds.TRANSFORMERLOOP.get(), net.minecraft.sounds.SoundSource.BLOCKS, net.minecraft.util.RandomSource.create());
            this.pos = pos;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.x = pos.getX() + 0.5;
            this.y = pos.getY() + 0.5;
            this.z = pos.getZ() + 0.5;
        }

        public void updatePos(BlockPos newPos) {
            this.pos = newPos;
        }

        @Override
        public void tick() {
            this.x = pos.getX() + 0.5;
            this.y = pos.getY() + 0.5;
            this.z = pos.getZ() + 0.5;
        }
    }
}