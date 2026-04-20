package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TerminalCalibrateScreen extends Screen {
    private static final ResourceLocation NOISE_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/noise_sheet.png");
    private static final ResourceLocation NOISE_RED_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/noise_sheet_not_found.png");

    public static boolean HAS_ACTIVE_SIGNAL = false;
    public static float CURRENT_TARGET_LINE = 0f;
    public static float CURRENT_TARGET_WAVE = 0f;
    public static String CURRENT_SIGNAL_TYPE = "";

    private static class CalibrateData {
        float currentLine = 0f;
        float currentWave = 0f;
        float loadingProgress = 0f;
        boolean isLineRowActive = false;
        boolean isWaveRowActive = false;
    }
    public static final Map<BlockPos, CalibrateData> SESSION_DATA = new HashMap<>();

    private final BlockPos terminalPos;
    private final CalibrateData data;

    private float efficiency = 0f;
    private float lineRotation = 0f;
    private float waveOffset = 0f;
    private int noiseFrame = 0;
    private int noiseTimer = 0;

    private int randomStarType = 1;

    private TerminalFindScreen.GuiLoopSound ambientSound;

    public TerminalCalibrateScreen(BlockPos pos) {
        super(Component.literal("Terminal Calibrate"));
        this.terminalPos = pos;
        this.data = SESSION_DATA.computeIfAbsent(pos, p -> new CalibrateData());

        if (!HAS_ACTIVE_SIGNAL || (Math.abs(data.currentLine - CURRENT_TARGET_LINE) > 0.1f && Math.abs(data.currentWave - CURRENT_TARGET_WAVE) > 0.1f && data.loadingProgress >= 100f)) {
            data.loadingProgress = 0f;
            data.isLineRowActive = false;
            data.isWaveRowActive = false;
            data.currentLine = 0f;
            data.currentWave = 0f;
        }

        randomStarType = Math.random() > 0.5 ? 1 : 2;
    }

    @Override
    protected void init() {
        super.init();
        if (ambientSound == null) {
            ambientSound = new TerminalFindScreen.GuiLoopSound(KerfurSounds.CALIBRATE_LOOP.get(), 0.3f);
            Minecraft.getInstance().getSoundManager().play(ambientSound);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void removed() {
        SESSION_DATA.put(terminalPos, data);
        Minecraft.getInstance().getSoundManager().stop(ambientSound);
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();

        noiseTimer++;
        if (noiseTimer % 2 == 0) noiseFrame = (noiseFrame + 1) % 40;

        if (!HAS_ACTIVE_SIGNAL) {
            lineRotation += 0.5f;
            waveOffset += 0.5f;
            efficiency = 0f;
            return;
        }

        float diffLine = Math.abs(CURRENT_TARGET_LINE - data.currentLine);
        float diffWave = Math.abs(CURRENT_TARGET_WAVE - data.currentWave);

        lineRotation += diffLine * 0.1f;
        waveOffset += diffWave * 0.1f;

        if (diffLine <= 5f && diffWave <= 5f) {
            if (data.loadingProgress >= 100f) {
                efficiency = 0f;
                return;
            }

            efficiency = 12.5f - ((diffLine + diffWave) / 2f);
            data.loadingProgress += efficiency * 0.05f;

            if (data.loadingProgress >= 100f) {
                data.loadingProgress = 100f;
                efficiency = 0f;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.ACHIEVEMENT.get(), 1.0F, 1.0F));
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.FinishCalibrationPacket());
            }
        } else {
            efficiency = 0f;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int panelWidth = 350;
        int panelHeight = 220;
        int startX = centerX - panelWidth / 2;
        int startY = centerY - panelHeight / 2;

        guiGraphics.fill(startX - 2, startY - 2, startX + panelWidth + 2, startY + panelHeight + 2, 0xFFFFFFFF);
        guiGraphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xFF000000);

        int circleRadius = 30;
        int lineCircleX = startX + 50;
        int lineCircleY = startY + 50;
        int waveCircleY = startY + 130;

        guiGraphics.fill(lineCircleX - 35, lineCircleY - 35, lineCircleX + 35, lineCircleY + 35, 0xFF222222);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(lineCircleX, lineCircleY, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(lineRotation));
        guiGraphics.fill(-1, 0, 1, circleRadius, 0xFFFFFFFF);
        guiGraphics.pose().popPose();

        guiGraphics.drawString(this.font, String.format("%.2f", data.currentLine), lineCircleX + 45, lineCircleY - 5, 0xFFFF00, false);

        guiGraphics.fill(lineCircleX - 35, waveCircleY - 35, lineCircleX + 35, waveCircleY + 35, 0xFF222222);
        for(int i = -30; i < 30; i += 2) {
            int yOffset = (int)(Math.sin((i + waveOffset) * 0.2) * 15);
            guiGraphics.fill(lineCircleX + i, waveCircleY + yOffset, lineCircleX + i + 2, waveCircleY + yOffset + 2, 0xFFFFFFFF);
        }

        guiGraphics.drawString(this.font, String.format("%.2f", data.currentWave), lineCircleX + 45, waveCircleY - 5, 0xFFFF00, false);

        int btnsY = startY + 180;
        guiGraphics.fill(startX + 10, btnsY, startX + 25, btnsY + 15, data.isLineRowActive ? 0xFF00FF00 : 0xFFFF0000);
        guiGraphics.drawString(this.font, "+5", startX + 35, btnsY + 4, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "+10", startX + 65, btnsY + 4, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "+15", startX + 95, btnsY + 4, 0xFFFFFF, false);

        guiGraphics.fill(startX + 10, btnsY + 20, startX + 25, btnsY + 35, data.isWaveRowActive ? 0xFF00FF00 : 0xFFFF0000);
        guiGraphics.drawString(this.font, "+5", startX + 35, btnsY + 24, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "+10", startX + 65, btnsY + 24, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "+15", startX + 95, btnsY + 24, 0xFFFFFF, false);

        int barX = startX + 150;
        int barY = startY + 30;
        int litBars = 0;
        if (HAS_ACTIVE_SIGNAL) {
            float totalDiff = Math.abs(CURRENT_TARGET_LINE - data.currentLine) + Math.abs(CURRENT_TARGET_WAVE - data.currentWave);
            litBars = 10 - (int)((totalDiff / 600f) * 10f);
            if (litBars < 1) litBars = 1;
        }

        for (int i = 0; i < 10; i++) {
            int yPos = barY + (9 - i) * 14;
            int color = 0x55333333;
            if (i < litBars) {
                int r = 255 - (i * 25);
                int g = (i * 25);
                color = 0xFF000000 | (r << 16) | (g << 8);
            }
            guiGraphics.fill(barX, yPos, barX + 20, yPos + 10, color);
        }

        int imgX = startX + 200;
        int imgY = startY + 20;

        guiGraphics.fill(imgX - 2, imgY - 2, imgX + 128 + 2, imgY + 128 + 2, 0xFFFFFFFF);
        guiGraphics.fill(imgX, imgY, imgX + 128, imgY + 128, 0xFF000000);

        RenderSystem.enableBlend();

        String objectNameText = "no";

        if (HAS_ACTIVE_SIGNAL) {
            float alphaSignal = data.loadingProgress / 100f;
            RenderSystem.setShaderColor(1f, 1f, 1f, alphaSignal);

            ResourceLocation targetTex = null;
            boolean isAnimatedSheet = false;
            String t = CURRENT_SIGNAL_TYPE;

            if (t.equals("mars") || t.equals("venus") || t.equals("earth") || t.equals("mercury") ||
                    t.equals("makemake") || t.equals("rhea") || t.equals("iris") || t.equals("amazur") ||
                    t.equals("vion") || t.equals("subplanet") || t.equals("europa") || t.equals("moon") ||
                    t.equals("jupiter") || t.equals("uranus") || t.equals("neptune") || t.equals("saturn") ||
                    t.equals("hilero") || t.equals("votv_earth") || t.equals("fard") || t.equals("ironlung")) {

                targetTex = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/" + t + "_sheet.png");
                isAnimatedSheet = true;
                if (data.loadingProgress >= 100f) objectNameText = "planet_" + t;
            }
            else if (t.equals("retroplanet")) {
                targetTex = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/retro_planet_sheet.png");
                isAnimatedSheet = true;
                if (data.loadingProgress >= 100f) objectNameText = "planet_retro_planet";
            }
            else if (t.equals("enceladus") || t.equals("ceres") || t.equals("dione") || t.equals("bennu")) {
                targetTex = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/grey_sheet.png");
                isAnimatedSheet = true;
                if (data.loadingProgress >= 100f) objectNameText = "planet_" + t;
            }
            else if (t.startsWith("siggen") || t.startsWith("exogen") || t.startsWith("siggenus") || t.equals("hairy")) {
                targetTex = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/genstars" + randomStarType + ".png");
                if (data.loadingProgress >= 100f) {
                    if (t.startsWith("exogen")) objectNameText = "unind_object";
                    else objectNameText = "unidentified_planet";
                }
            }
            else if (t.equals("faces")) {
                targetTex = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/faces_im.png");
                if (data.loadingProgress >= 100f) objectNameText = "unidentified_planet";
            }
            else {
                targetTex = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/generic_signal_image.png");
                if (data.loadingProgress >= 100f) objectNameText = "generic planet";
            }

            if (isAnimatedSheet) {
                guiGraphics.blit(targetTex, imgX, imgY, 0, noiseFrame * 128, 128, 128, 128, 5120);
            } else {
                guiGraphics.blit(targetTex, imgX, imgY, 0, 0, 128, 128, 128, 128);
            }

            float alphaNoise = 1f - alphaSignal;
            if (alphaNoise > 0) {
                RenderSystem.setShaderColor(1f, 1f, 1f, alphaNoise);
                guiGraphics.blit(NOISE_TEX, imgX, imgY, 0, (noiseFrame % 4) * 128, 128, 128, 128, 512);
            }
        } else {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            guiGraphics.blit(NOISE_RED_TEX, imgX, imgY, 0, (noiseFrame % 4) * 128, 128, 128, 128, 512);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        int dataY = startY + 160;
        guiGraphics.drawString(this.font, "Object:", imgX, dataY, 0xFFAA00, false);
        guiGraphics.drawString(this.font, objectNameText, imgX + 50, dataY, 0xFFFF55, false);

        guiGraphics.drawString(this.font, "Efficiency:", imgX, dataY + 15, 0xFFAA00, false);
        guiGraphics.drawString(this.font, String.format("%.1f B/s", efficiency), imgX + 70, dataY + 15, 0xFFFF55, false);

        guiGraphics.drawString(this.font, "Loading:", imgX, dataY + 30, 0xFFAA00, false);
        guiGraphics.drawString(this.font, String.format("%.1f%%", data.loadingProgress), imgX + 55, dataY + 30, 0xFFFF55, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!HAS_ACTIVE_SIGNAL) return super.mouseClicked(mouseX, mouseY, button);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 175;
        int startY = centerY - 110;
        int btnsY = startY + 180;

        if (mouseX >= startX + 10 && mouseX <= startX + 25) {
            if (mouseY >= btnsY && mouseY <= btnsY + 15) {
                data.isLineRowActive = !data.isLineRowActive;
                if (data.isLineRowActive) data.isWaveRowActive = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
                return true;
            }
            if (mouseY >= btnsY + 20 && mouseY <= btnsY + 35) {
                data.isWaveRowActive = !data.isWaveRowActive;
                if (data.isWaveRowActive) data.isLineRowActive = false;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!HAS_ACTIVE_SIGNAL) return super.mouseScrolled(mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 175;
        int startY = centerY - 110;
        int btnsY = startY + 180;

        boolean scrolled = false;

        if (data.isLineRowActive && mouseY >= btnsY && mouseY <= btnsY + 15) {
            if (mouseX >= startX + 30 && mouseX <= startX + 50) { data.currentLine += 5f * Math.signum(delta); scrolled = true; }
            if (mouseX >= startX + 60 && mouseX <= startX + 80) { data.currentLine += 10f * Math.signum(delta); scrolled = true; }
            if (mouseX >= startX + 90 && mouseX <= startX + 110) { data.currentLine += 15f * Math.signum(delta); scrolled = true; }

            if(data.currentLine < 0) data.currentLine = 0;
            if(data.currentLine > 300) data.currentLine = 300;
        }

        if (data.isWaveRowActive && mouseY >= btnsY + 20 && mouseY <= btnsY + 35) {
            if (mouseX >= startX + 30 && mouseX <= startX + 50) { data.currentWave += 5f * Math.signum(delta); scrolled = true; }
            if (mouseX >= startX + 60 && mouseX <= startX + 80) { data.currentWave += 10f * Math.signum(delta); scrolled = true; }
            if (mouseX >= startX + 90 && mouseX <= startX + 110) { data.currentWave += 15f * Math.signum(delta); scrolled = true; }

            if(data.currentWave < 0) data.currentWave = 0;
            if(data.currentWave > 300) data.currentWave = 300;
        }

        if (scrolled) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.TUMBLER.get(), 1.0F, 0.8F));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}