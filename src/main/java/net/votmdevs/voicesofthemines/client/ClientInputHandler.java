package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
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

@Mod.EventBusSubscriber(modid = KerfurMod.MODID, value = Dist.CLIENT)
public class ClientInputHandler {
    private static boolean wasUseKeyDown = false;
    private static Entity currentlyHeldEntity = null;

    private static boolean wasJumpKeyDown = false;
    private static boolean wasPickKeyDown = false;

    private static net.minecraft.client.resources.sounds.SoundInstance atvSoundInstance = null;
    private static String currentAtvSoundState = "none"; // none, idle, drive_start, drive_loop
    private static int atvSoundTimer = 0;

    public static int knockdownTicks = 0;
    public static float vignetteAlpha = 0.0f;

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
            mc.player.playSound(KerfurSounds.FALLDEATH.get(), 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getMainHandItem().getItem() == KerfurMod.HOOK_ITEM.get()) {
            double scroll = -event.getScrollDelta();
            if (scroll != 0) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.HookPullPacket(scroll));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClickHook(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.player != null && mc.player.getMainHandItem().getItem() == KerfurMod.HOOK_ITEM.get()) {
            KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.HookDetachPacket());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

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

        if (activeAtv != null) {
            float speed = Math.abs(activeAtv.getEntityData().get(net.votmdevs.voicesofthemines.entity.AtvEntity.CURRENT_SPEED));

            if (speed < 0.05f) {
                if (!currentAtvSoundState.equals("idle")) {
                    currentAtvSoundState = "idle";
                    if (atvSoundInstance != null) mc.getSoundManager().stop(atvSoundInstance);

                    atvSoundInstance = new AtvLoopSound(activeAtv, KerfurSounds.IDLE.get());
                    mc.getSoundManager().play(atvSoundInstance);
                }
            } else {
                if (currentAtvSoundState.equals("idle") || currentAtvSoundState.equals("none")) {
                    currentAtvSoundState = "drive_start";
                    if (atvSoundInstance != null) mc.getSoundManager().stop(atvSoundInstance);

                    atvSoundInstance = net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(KerfurSounds.ATV_DRIVE_START.get(), 1.0F, 1.0F);
                    mc.getSoundManager().play(atvSoundInstance);
                    atvSoundTimer = 60;
                }

                if (currentAtvSoundState.equals("drive_start")) {
                    if (atvSoundTimer > 0) {
                        atvSoundTimer--;
                    } else if (atvSoundInstance == null || !mc.getSoundManager().isActive(atvSoundInstance)) {
                        currentAtvSoundState = "drive_loop";
                        if (atvSoundInstance != null) mc.getSoundManager().stop(atvSoundInstance);

                        atvSoundInstance = new AtvLoopSound(activeAtv, KerfurSounds.ATV_DRIVE_LOOP.get());
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
            } else if (target instanceof MaxwellEntity maxwell) {
                if (!mc.player.isShiftKeyDown()) {
                    currentlyHeldEntity = maxwell;
                    KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(maxwell.getId(), true));
                }
                else if (target instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                    currentlyHeldEntity = drive;
                    KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(drive.getId(), true));
                }
            }
        } else if (!isUseKeyDown && wasUseKeyDown) {
            if (currentlyHeldEntity != null && !(currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity)) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(0, false));
                currentlyHeldEntity = null;
            }

        } else if (!isUseKeyDown && wasUseKeyDown) {
            if (currentlyHeldEntity != null && !(currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity)) {

                if (currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                    if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                        net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
                        net.minecraft.world.level.block.Block block = state.getBlock();

                        if (block == KerfurMod.TERMINAL_CHECK.get() || block == KerfurMod.TERMINAL_PROCESSING.get()) {

                            String sigId = drive.getEntityData().get(net.votmdevs.voicesofthemines.entity.DriveEntity.SIGNAL_ID);
                            boolean isEmpty = (sigId == null || sigId.isEmpty());

                            if (block == KerfurMod.TERMINAL_PROCESSING.get() && isEmpty) {
                                mc.player.playSound(KerfurSounds.BUG_ALERT.get(), 1.0F, 0.5F);
                            } else {
                                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.InsertDrivePacket(blockHit.getBlockPos(), drive.getId()));
                            }
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
            } else if (target instanceof net.votmdevs.voicesofthemines.entity.DriveEntity drive) {
                currentlyHeldEntity = drive;
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(drive.getId(), true));
            }
        } else if (!isPickKeyDown && wasPickKeyDown) {
            if (currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity || currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.FuelCanEntity || currentlyHeldEntity instanceof net.votmdevs.voicesofthemines.entity.DriveEntity) {
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.GrabPacket(0, false));
                currentlyHeldEntity = null;
            }
        }
        wasPickKeyDown = isPickKeyDown;
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (knockdownTicks > 0) {
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
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (knockdownTicks > 0) {
            event.setCanceled(true);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.crosshairPickEntity instanceof GarbageEntity garbage) {
            if (mc.player != null && mc.player.getMainHandItem().getItem() == KerfurMod.TRASH_ROLL.get()) {
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
        if (knockdownTicks > 0) {
            float progress = (80 - knockdownTicks) / 80.0f;
            float roll = 0, pitch = event.getPitch(), yaw = event.getYaw();
            float dropOffset = 0, targetDrop = 1.4f;

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
                } catch (Exception ex) {}
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

        if (event.getOverlay() == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.HOTBAR.type() && vignetteAlpha > 0) {
            ResourceLocation VIGNETTE = new ResourceLocation(KerfurMod.MODID, "textures/gui/damage_vignette.png");
            com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, vignetteAlpha);

            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            event.getGuiGraphics().blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);

            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
            com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }

        if (event.getOverlay() == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.type()) {
            if (mc.crosshairPickEntity instanceof net.votmdevs.voicesofthemines.entity.AtvEntity atv && (mc.player == null || mc.player.getVehicle() != atv)) {
                int width = event.getWindow().getGuiScaledWidth();
                int height = event.getWindow().getGuiScaledHeight();
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
        }
    }

    public static class AtvLoopSound extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        private final net.votmdevs.voicesofthemines.entity.AtvEntity atv;

        public AtvLoopSound(net.votmdevs.voicesofthemines.entity.AtvEntity atv, net.minecraft.sounds.SoundEvent sound) {
            super(sound, net.minecraft.sounds.SoundSource.PLAYERS, net.minecraft.util.RandomSource.create());
            this.atv = atv;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.x = atv.getX();
            this.y = atv.getY();
            this.z = atv.getZ();
        }

        @Override
        public void tick() {
            if (!this.atv.isAlive() || !this.atv.isEngineOn()) {
                this.stop();
            } else {
                this.x = this.atv.getX();
                this.y = this.atv.getY();
                this.z = this.atv.getZ();
            }
        }
    }
}