package net.votmdevs.voicesofthemines.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.votmdevs.voicesofthemines.VotmSounds;
import java.util.Random;

public class ServerMinigameScreen extends Screen {
    private final BlockPos serverPos;
    private final long startTime;
    private int currentRow = 0;
    private final Equation[] equations = new Equation[8];
    private boolean isFailed = false;

    private static class Equation {
        int a, b;
        boolean isAdd;
        int expectedAnswer;
        int selectedAnswer = -1;

        Equation(Random rand) {
            this.a = rand.nextInt(10);
            this.b = rand.nextInt(10);
            this.isAdd = rand.nextBoolean();
            int res = isAdd ? (a + b) : (a - b);
            this.expectedAnswer = Math.abs(res) % 10;
        }
    }

    public ServerMinigameScreen(BlockPos pos) {
        super(Component.literal("Server Maintenance"));
        this.serverPos = pos;
        this.startTime = System.currentTimeMillis();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) {
            equations[i] = new Equation(rand);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = 50000 - elapsed;

        if (remaining <= 0 || isFailed) {
            this.minecraft.setScreen(null);
            return;
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int w = 260; int h = 220;
        int x = centerX - w / 2;
        int y = centerY - h / 2;

        guiGraphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFF00FF00); // Зеленая рамка
        guiGraphics.fill(x, y, x + w, y + h, 0xFF000000); // Черный фон

        guiGraphics.fill(x + 10, y + 10, x + 40, y + 25, 0xFF00FF00);
        guiGraphics.fill(x + 11, y + 11, x + 39, y + 24, 0xFF000000);
        guiGraphics.drawString(this.font, "IN", x + 18, y + 14, 0xFFFF00FF, false);

        int seconds = (int) (remaining / 1000);
        int millis = (int) ((remaining % 1000) / 10);
        String timerText = String.format("0:00:%02d:%02d", seconds, millis);
        guiGraphics.fill(x + 50, y + 10, x + 130, y + 25, 0xFF00FF00);
        guiGraphics.fill(x + 51, y + 11, x + 129, y + 24, 0xFF000000);
        guiGraphics.drawString(this.font, timerText, x + 60, y + 14, 0xFFFF00FF, false);

        for (int i = 0; i < 8; i++) {
            int rowY = y + 35 + (i * 18);
            boolean isActiveRow = (i == currentRow);

            // Окно самого примера
            guiGraphics.fill(x + 10, rowY, x + 110, rowY + 15, 0xFFFFFF00);
            guiGraphics.fill(x + 11, rowY + 1, x + 109, rowY + 14, 0xFF000000);

            Equation eq = equations[i];
            String eqText = "MOD( " + eq.a + (eq.isAdd ? " + " : " - ") + eq.b + " )";
            guiGraphics.drawString(this.font, eqText, x + 20, rowY + 4, 0xFF00FF00, false);

            for (int num = 0; num <= 9; num++) {
                int numX = x + 120 + (num * 14);
                boolean isHovered = isActiveRow && mouseX >= numX - 2 && mouseX <= numX + 8 && mouseY >= rowY && mouseY <= rowY + 15;

                if (eq.selectedAnswer == num) {
                    guiGraphics.fill(numX - 2, rowY, numX + 8, rowY + 15, 0xFFFF00FF);
                    guiGraphics.fill(numX - 1, rowY + 1, numX + 7, rowY + 14, 0xFF000000);
                } else if (isHovered) {
                    guiGraphics.fill(numX - 2, rowY, numX + 8, rowY + 15, 0xFF333333);
                }

                guiGraphics.drawString(this.font, String.valueOf(num), numX, rowY + 4, 0xFF00FFFF, false);
            }
        }

        guiGraphics.fill(x + 10, y + 185, x + 40, y + 200, 0xFF00FF00);
        guiGraphics.fill(x + 11, y + 186, x + 39, y + 199, 0xFF000000);
        guiGraphics.drawString(this.font, "OUT", x + 15, y + 189, 0xFFFF00FF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentRow >= 8) return true;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int w = 260; int h = 220;
        int x = centerX - w / 2;
        int y = centerY - h / 2;

        int rowY = y + 35 + (currentRow * 18);

        if (mouseY >= rowY && mouseY <= rowY + 15) {
            for (int num = 0; num <= 9; num++) {
                int numX = x + 120 + (num * 14);
                if (mouseX >= numX - 2 && mouseX <= numX + 8) {

                    if (num == equations[currentRow].expectedAnswer) {
                        equations[currentRow].selectedAnswer = num;
                        currentRow++;
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.5F));

                        if (currentRow >= 8) {
                            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                                    new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.FixServerPacket(serverPos)
                            );
                            this.minecraft.setScreen(null);
                        }
                    } else {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUG_ALERT.get(), 1.0F, 0.5F));
                        isFailed = true;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}