package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import net.votmdevs.voicesofthemines.world.SignalManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalFindScreen extends Screen {
    private static final ResourceLocation SPACE_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/space.png");
    private static final ResourceLocation CROSSHAIR_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/coordcrosshair.png");
    private static final ResourceLocation ARROW_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/red_arrow.png");
    private static final ResourceLocation ANIM_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/found_anim_sheet.png");

    public static List<SignalManager.VotvSignal> CLIENT_SIGNALS = new ArrayList<>();

    private static class TerminalData {
        float x = 0f;
        float y = 0f;
        float cooldown = 100f;
    }
    private static final Map<BlockPos, TerminalData> SESSION_DATA = new HashMap<>();
    private final BlockPos terminalPos;
    private final TerminalData data;

    public static boolean IS_PROCESSING_ACTIVE = false;

    private final List<Float> arrowAngles = new ArrayList<>();
    private boolean isScanning = false;

    private final List<String> recLog = new ArrayList<>();
    private String statusText = "";
    private int statusColor = 0x00FF00;
    private int logTickCounter = 0;

    private boolean showArrow = false;
    private int arrowTimer = 0;
    private int animFrame = 0;
    private int animTimer = 0;
    private int scanTimer = 0;

    private SignalManager.VotvSignal currentlyScanningSignal = null;

    private GuiLoopSound ambientSound;
    private GuiLoopSound sonarSound;
    private GuiLoopSound proximitySound;

    public TerminalFindScreen(BlockPos pos) {
        super(Component.literal("Terminal Find"));
        this.terminalPos = pos;
        this.data = SESSION_DATA.computeIfAbsent(pos, p -> new TerminalData());
    }

    @Override
    protected void init() {
        super.init();
        if (ambientSound == null) {
            ambientSound = new GuiLoopSound(KerfurSounds.FIND_AMBIENT.get(), 0.3f);
            sonarSound = new GuiLoopSound(KerfurSounds.SONAR.get(), 0.15f);
            proximitySound = new GuiLoopSound(KerfurSounds.FIND_HELP.get(), 0f); // Изначально тихий

            Minecraft.getInstance().getSoundManager().play(ambientSound);
            Minecraft.getInstance().getSoundManager().play(sonarSound);
            Minecraft.getInstance().getSoundManager().play(proximitySound);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void removed() {
        SESSION_DATA.put(terminalPos, data);
        Minecraft.getInstance().getSoundManager().stop(ambientSound);
        Minecraft.getInstance().getSoundManager().stop(sonarSound);
        Minecraft.getInstance().getSoundManager().stop(proximitySound);
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();

        if (CLIENT_SIGNALS.isEmpty()) {
            proximitySound.setVolume(0f);
        } else {
            float minDist = Float.MAX_VALUE;
            for (SignalManager.VotvSignal s : CLIENT_SIGNALS) {
                float dist = (float) Math.sqrt(Math.pow(s.x - data.x, 2) + Math.pow(s.y - data.y, 2));
                if (dist < minDist) minDist = dist;
            }
            if (minDist < 1500f) {
                proximitySound.setVolume(1.0f - (minDist / 1500f));
            } else {
                proximitySound.setVolume(0f);
            }
        }

        animTimer++;
        if (animTimer % 3 == 0) {
            animFrame = (animFrame + 1) % 11;
        }

        if (isScanning && currentlyScanningSignal != null) {
            scanTimer--;
            if (scanTimer <= 0) {
                isScanning = false;
                statusText = "SUCCESS: SIGNAL FOUND";
                statusColor = 0x00FF00;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.ACHIEVEMENT.get(), 1.0F, 1.0F));
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.CatchSignalPacket(currentlyScanningSignal.id));
                CLIENT_SIGNALS.remove(currentlyScanningSignal);
                currentlyScanningSignal = null;
                showArrow = false;
            }
        }

        if (showArrow && arrowTimer > 0) {
            arrowTimer--;
            if (arrowTimer == 0) showArrow = false;
        }

        float maxCooldown = 100f - (ComputerScreen.UPG_PING * 3f);
        if (maxCooldown < 20f) maxCooldown = 20f;

        if (data.cooldown < maxCooldown) {
            data.cooldown += 0.4f;
            if (data.cooldown > maxCooldown) data.cooldown = maxCooldown;

            logTickCounter++;
            if (logTickCounter >= 5) {
                logTickCounter = 0;

                float percent = (data.cooldown / maxCooldown) * 100f;
                int bars = (int) (percent / 10f);

                StringBuilder barStr = new StringBuilder();
                for (int i = 0; i < 10; i++) barStr.append(i < bars ? "|" : ".");

                addLog(String.format("REC: [%s] %.1f%%", barStr.toString(), percent));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int spaceSize = 160, logWidth = 130, gap = 15, bottomHeight = 45;
        int totalWidth = spaceSize + gap + logWidth;
        int totalHeight = spaceSize + gap + bottomHeight;

        int startX = (this.width - totalWidth) / 2;
        int startY = (this.height - totalHeight) / 2;

        int spaceX = startX, spaceY = startY;
        int logX = startX + spaceSize + gap;
        int coordY = startY + spaceSize + gap;
        int statusY = startY + spaceSize + gap;

        guiGraphics.fill(spaceX - 2, spaceY - 2, spaceX + spaceSize + 2, spaceY + spaceSize + 2, 0xFFFFFFFF);
        guiGraphics.fill(spaceX, spaceY, spaceX + spaceSize, spaceY + spaceSize, 0xFF000000);

        double scale = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) (spaceX * scale), (int) ((this.height - spaceY - spaceSize) * scale), (int) (spaceSize * scale), (int) (spaceSize * scale));

        int texOffsetX = (int) (data.x % 4096);
        int texOffsetY = (int) (data.y % 4096);
        if (texOffsetX < 0) texOffsetX += 4096;
        if (texOffsetY < 0) texOffsetY += 4096;
        guiGraphics.blit(SPACE_TEX, spaceX, spaceY, texOffsetX, texOffsetY, spaceSize, spaceSize, 4096, 4096);

        int centerX = spaceX + spaceSize / 2;
        int centerY = spaceY + spaceSize / 2;

        for (SignalManager.VotvSignal s : CLIENT_SIGNALS) {
            float screenPosX = centerX + (s.x - data.x);
            float screenPosY = centerY + (s.y - data.y);

            if (Math.abs(screenPosX - centerX) < 100 && Math.abs(screenPosY - centerY) < 100) {
                guiGraphics.blit(ANIM_TEX, (int) screenPosX - 20, (int) screenPosY - 20, 40, 40, 0, animFrame * 128, 128, 128, 128, 1408);
            }
        }

        guiGraphics.fill(spaceX, centerY, spaceX + spaceSize, centerY + 1, 0x88FFFFFF);
        guiGraphics.fill(centerX, spaceY, centerX + 1, spaceY + spaceSize, 0x88FFFFFF);
        guiGraphics.blit(CROSSHAIR_TEX, centerX - 20, centerY - 20, 40, 40, 0, 0, 128, 128, 128, 128);

        if (showArrow) {
            for (float angle : arrowAngles) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(centerX, centerY, 0);
                guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
                guiGraphics.blit(ARROW_TEX, -8, -35, 16, 16, 0, 0, 128, 128, 128, 128);
                guiGraphics.pose().popPose();
            }
        }
        RenderSystem.disableScissor();

        guiGraphics.fill(spaceX - 2, coordY - 2, spaceX + spaceSize + 2, coordY + bottomHeight, 0xFFFFFFFF);
        guiGraphics.fill(spaceX, coordY, spaceX + spaceSize, coordY + bottomHeight - 2, 0xFF000000);

        guiGraphics.drawString(this.font, "COORDINATES", spaceX + 45, coordY + 5, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "X: " + String.format("%.1f", data.x), spaceX + 10, coordY + 18, 0xFF5555, false);
        guiGraphics.drawString(this.font, "Y: " + String.format("%.1f", data.y), spaceX + 10, coordY + 31, 0x55FF55, false);

        guiGraphics.fill(logX - 2, spaceY - 2, logX + logWidth + 2, spaceY + spaceSize + 2, 0xFFFFFFFF);
        guiGraphics.fill(logX, spaceY, logX + logWidth, spaceY + spaceSize, 0xFF000000);

        for (int i = 0; i < recLog.size(); i++) {
            guiGraphics.drawString(this.font, recLog.get(i), logX + 5, spaceY + 10 + (i * 12), 0x55FF55, false);
        }

        guiGraphics.fill(logX - 2, statusY - 2, logX + logWidth + 2, statusY + bottomHeight, 0xFFFFFFFF);
        guiGraphics.fill(logX, statusY, logX + logWidth, statusY + bottomHeight - 2, 0xFF000000);

        if (!statusText.isEmpty()) guiGraphics.drawString(this.font, statusText, logX + 5, statusY + 15, statusColor, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        float speed = 15.0f + (ComputerScreen.UPG_CURSOR * 1.5f);

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UP || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_W) data.y -= speed;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_S) data.y += speed;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_A) data.x -= speed;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_D) data.x += speed;

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) {
            float maxCooldown = 100f - (ComputerScreen.UPG_PING * 3f);
            if (maxCooldown < 20f) maxCooldown = 20f;

            if (data.cooldown >= maxCooldown) {
                if (CLIENT_SIGNALS.isEmpty()) {
                    addLog("ERR: No signals in space.");
                    playErrorSound();
                } else {
                    data.cooldown = 0f;
                    arrowAngles.clear();
                    for (SignalManager.VotvSignal s : CLIENT_SIGNALS) {
                        float angle = (float) Math.toDegrees(Math.atan2(s.y - data.y, s.x - data.x)) + 90f;
                        arrowAngles.add(angle);
                    }
                    showArrow = true;
                    arrowTimer = 60;
                    addLog("PING: " + CLIENT_SIGNALS.size() + " signals detected.");

                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.DETECT_BEEP.get(), 1.0F, 0.8F));
                }
            } else {
                addLog("ERR: Radar charging.");
                playErrorSound();
            }
        }

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
            if (IS_PROCESSING_ACTIVE) {
                statusText = "ERR: Calibration busy.";
                statusColor = 0xFF5555;
                playErrorSound();
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            if (CLIENT_SIGNALS.isEmpty()) {
                statusText = "ERR: Space is empty.";
                statusColor = 0xFF5555;
                playErrorSound();
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            SignalManager.VotvSignal target = null;
            for (SignalManager.VotvSignal s : CLIENT_SIGNALS) {
                float dist = (float) Math.sqrt(Math.pow(s.x - data.x, 2) + Math.pow(s.y - data.y, 2));
                if (dist < 60.0f) {
                    target = s;
                    break;
                }
            }

            if (target != null) {
                currentlyScanningSignal = target;
                isScanning = true;
                scanTimer = 60;
                statusText = "Init quick scan...";
                statusColor = 0x55FF55;
            } else {
                statusText = "FAIL: Target missed.";
                statusColor = 0xFF5555;
                playErrorSound();
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void addLog(String text) {
        recLog.add(text);
        if (recLog.size() > 11) recLog.remove(0);
    }

    private void playErrorSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUG_ALERT.get(), 1.0F, 0.4F));
    }

    public static class GuiLoopSound extends net.minecraft.client.resources.sounds.AbstractTickableSoundInstance {
        public GuiLoopSound(net.minecraft.sounds.SoundEvent sound, float volume) {
            super(sound, net.minecraft.sounds.SoundSource.MASTER, net.minecraft.util.RandomSource.create());
            this.looping = true;
            this.delay = 0;
            this.volume = volume;
            this.relative = true;
        }
        @Override
        public void tick() {}
        public void setVolume(float v) { this.volume = v; }
    }
}