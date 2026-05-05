package net.votmdevs.voicesofthemines.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import org.lwjgl.glfw.GLFW;

public class SafeScreen extends Screen {
    private final BlockPos blockPos;
    private final boolean isSettingNewCode;
    private final int[] currentCode = new int[]{-1, -1, -1, -1};
    private int activeIndex = 0;

    public SafeScreen(BlockPos pos, boolean isSettingNewCode) {
        super(Component.literal("Safe Passcode"));
        this.blockPos = pos;
        this.isSettingNewCode = isSettingNewCode;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            handleNumberInput(keyCode - GLFW.GLFW_KEY_0);
            return true;
        } else if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            handleNumberInput(keyCode - GLFW.GLFW_KEY_KP_0);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (activeIndex == 4) {
                StringBuilder codeBuilder = new StringBuilder();
                for (int digit : currentCode) {
                    codeBuilder.append(digit);
                }

                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.SafeCodePacket(blockPos, codeBuilder.toString(), isSettingNewCode));
                this.minecraft.setScreen(null);
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void handleNumberInput(int number) {
        if (activeIndex < 4) {
            currentCode[activeIndex] = number;
            activeIndex++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.drawCenteredString(this.font, isSettingNewCode ? "Set New Passcode" : "Enter Passcode", centerX, centerY - 50, 0xFFFFFF);

        int boxSize = 30;
        int spacing = 10;
        int totalWidth = (boxSize * 4) + (spacing * 3);
        int startX = centerX - (totalWidth / 2);

        for (int i = 0; i < 4; i++) {
            int currentX = startX + (i * (boxSize + spacing));

            int borderColor = (i == activeIndex) ? 0xFFFF0000 : 0xFFFFFFFF;

            guiGraphics.fill(currentX - 1, centerY - 1, currentX + boxSize + 1, centerY + boxSize + 1, borderColor);
            guiGraphics.fill(currentX, centerY, currentX + boxSize, centerY + boxSize, 0xFF000000);

            String digitToDraw = (currentCode[i] == -1) ? "0" : String.valueOf(currentCode[i]);
            guiGraphics.drawCenteredString(this.font, digitToDraw, currentX + (boxSize / 2), centerY + (boxSize / 2) - 4, 0xFFFFFF);
        }

        if (activeIndex == 4) {
            guiGraphics.drawCenteredString(this.font, "Press ENTER to confirm", centerX, centerY + 50, 0x55FF55);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}