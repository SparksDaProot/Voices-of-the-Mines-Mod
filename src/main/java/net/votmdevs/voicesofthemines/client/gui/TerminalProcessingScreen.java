package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.KerfurSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class TerminalProcessingScreen extends Screen {
    public static boolean HAS_ACTIVE_SIGNAL = false;
    public static String CURRENT_SIGNAL_TYPE = "";
    public static int CURRENT_SIGNAL_LEVEL = 0;

    private final BlockPos terminalPos;
    private boolean isProcessing = false;
    private float progress = 0f;
    private float waveTimer = 0f;

    public TerminalProcessingScreen(BlockPos pos) {
        super(Component.literal("Terminal Processing"));
        this.terminalPos = pos;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        super.tick();
        waveTimer += 0.15f;

        if (isProcessing) {
            float speed = 0.2f + (ComputerScreen.UPG_PROC_SPEED * 0.1f);
            progress += speed;

            if (progress >= 100f) {
                progress = 100f;
                isProcessing = false;

                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.ACHIEVEMENT.get(), 1.0F, 1.0F));
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.FinishProcessingPacket(terminalPos));
                this.minecraft.setScreen(null);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int panelWidth = 400;
        int panelHeight = 240;
        int startX = centerX - panelWidth / 2;
        int startY = centerY - panelHeight / 2;

        guiGraphics.fill(startX - 2, startY - 2, startX + panelWidth + 2, startY + panelHeight + 2, 0xFFFFFFFF);
        guiGraphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xFF000000);

        // Progress bar
        int topWinX = startX + 20;
        int topWinY = startY + 20;
        int topWinW = 360;
        int topWinH = 40;

        guiGraphics.fill(topWinX - 1, topWinY - 1, topWinX + topWinW + 1, topWinY + topWinH + 1, 0xFFFFFFFF);
        guiGraphics.fill(topWinX, topWinY, topWinX + topWinW, topWinY + topWinH, 0xFF000000);

        int segments = 10;
        int spacing = 4;
        int segWidth = (topWinW - (spacing * (segments + 1))) / segments;
        int currentSegments = (int) (progress / 10f);

        for (int i = 0; i < currentSegments; i++) {
            int segX = topWinX + spacing + (i * (segWidth + spacing));
            int r = 255 - (i * 25);
            int g = i * 25;
            int color = 0xFF000000 | (r << 16) | (g << 8);
            guiGraphics.fill(segX, topWinY + 5, segX + segWidth, topWinY + topWinH - 5, color);
        }

        // Info
        int botWinY = startY + 80;
        int botLeftW = 200;
        int botWinH = 140;

        guiGraphics.fill(topWinX - 1, botWinY - 1, topWinX + botLeftW + 1, botWinY + botWinH + 1, 0xFFFFFFFF);
        guiGraphics.fill(topWinX, botWinY, topWinX + botLeftW, botWinY + botWinH, 0xFF000000);

        if (HAS_ACTIVE_SIGNAL) {
            String displayName = "UNKNOWN";
            String t = CURRENT_SIGNAL_TYPE;

            if (t.equals("mars") || t.equals("venus") || t.equals("earth") || t.equals("mercury") ||
                    t.equals("enceladus") || t.equals("ceres") || t.equals("dione") || t.equals("bennu") ||
                    t.equals("makemake") || t.equals("rhea") || t.equals("iris") || t.equals("amazur") ||
                    t.equals("vion") || t.equals("subplanet") || t.equals("europa") || t.equals("moon") ||
                    t.equals("jupiter") || t.equals("uranus") || t.equals("neptune") || t.equals("saturn") ||
                    t.equals("hilero") || t.equals("votv_earth") || t.equals("fard") || t.equals("ironlung")) {
                displayName = "planet_" + t;
            } else if (t.equals("retroplanet")) {
                displayName = "planet_retro_planet";
            } else if (t.startsWith("exogen")) {
                displayName = "unind_object";
            } else if (t.startsWith("siggen") || t.startsWith("siggenus") || t.equals("faces") || t.equals("hairy")) {
                displayName = "unidentified_planet";
            } else if (!t.isEmpty()) {
                displayName = "generic planet";
            }

            guiGraphics.drawString(this.font, "DATA:", topWinX + 10, botWinY + 20, 0xFFAA00, false);
            guiGraphics.drawString(this.font, displayName, topWinX + 50, botWinY + 20, 0xFFFFFF, false);

            guiGraphics.drawString(this.font, "Progress:", topWinX + 10, botWinY + 40, 0xFFAA00, false);
            guiGraphics.drawString(this.font, String.format("%.1f %%", progress), topWinX + 70, botWinY + 40, 0x55FF55, false);

            guiGraphics.drawString(this.font, "Target level:", topWinX + 10, botWinY + 60, 0xFFAA00, false);

            boolean isMax = CURRENT_SIGNAL_LEVEL >= ComputerScreen.UPG_PROC_LVL;
            String targetLvlStr = isMax ? "[ LOCKED ]" : "[ " + (CURRENT_SIGNAL_LEVEL + 1) + " ]";
            guiGraphics.drawString(this.font, targetLvlStr, topWinX + 90, botWinY + 60, 0xFFFFFF, false);

            int btnX = topWinX + 50;
            int btnY = botWinY + 100;

            guiGraphics.fill(btnX, btnY, btnX + 100, btnY + 20, isProcessing ? 0xFF555555 : (isMax ? 0xFF555555 : 0xFF55FF55));
            guiGraphics.drawString(this.font, isMax ? "MAX LEVEL" : (isProcessing ? "PROCESSING..." : "START"), btnX + (isMax ? 20 : (isProcessing ? 15 : 35)), btnY + 6, 0xFF000000, false);
        } else {
            guiGraphics.drawString(this.font, "NO DRIVE INSERTED", topWinX + 10, botWinY + 20, 0xFF5555, false);
        }

        int botRightX = topWinX + botLeftW + 20;
        int botRightW = 140;

        guiGraphics.fill(botRightX - 1, botWinY - 1, botRightX + botRightW + 1, botWinY + botWinH + 1, 0xFFFFFFFF);
        guiGraphics.fill(botRightX, botWinY, botRightX + botRightW, botWinY + botWinH, 0xFF000000);

        if (HAS_ACTIVE_SIGNAL) {
            int waveCenterY = botWinY + (botWinH / 2);
            for (int i = 0; i < botRightW; i += 2) {
                float amplitude = isProcessing ? 30f : 5f;
                int yOffset = (int)(Math.sin((i * 0.05f) + waveTimer) * amplitude);
                guiGraphics.fill(botRightX + i, waveCenterY + yOffset, botRightX + i + 2, waveCenterY + yOffset + 2, 0xFFFFFF00);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!HAS_ACTIVE_SIGNAL || isProcessing) return super.mouseClicked(mouseX, mouseY, button);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 200;
        int startY = centerY - 120;

        int topWinX = startX + 20;
        int botWinY = startY + 80;

        int btnX = topWinX + 50;
        int btnY = botWinY + 100;

        if (mouseX >= btnX && mouseX <= btnX + 100 && mouseY >= btnY && mouseY <= btnY + 20) {
            if (CURRENT_SIGNAL_LEVEL >= ComputerScreen.UPG_PROC_LVL) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUG_ALERT.get(), 1.0F, 0.5F));
                return true; // Блокируем, если уровень сигнала уже равен или выше купленного
            }

            isProcessing = true;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}