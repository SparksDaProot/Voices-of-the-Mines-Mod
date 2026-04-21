package net.votmdevs.voicesofthemines.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.votmdevs.voicesofthemines.VotmSounds;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

public class ConsoleScreen extends Screen {
    private final BlockPos blockPos;
    private EditBox inputField;
    private final List<String> outputHistory = new ArrayList<>();
    private String lastText = "";
    private int scrollOffset = 0;
    private final Queue<String> calQueue = new LinkedList<>();
    private String activeCalTarget = null;
    private float activeCalProgress = 0;
    private int calTimer = 0;

    public ConsoleScreen(BlockPos pos) {
        super(Component.literal("Console"));
        this.blockPos = pos;
    }

    @Override
    protected void init() {
        super.init();
        int w = 400; int h = 240;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        inputField = new EditBox(this.font, x + 15, y + h - 25, 240, 15, Component.literal(""));
        inputField.setMaxLength(64);
        inputField.setBordered(false);
        inputField.setFocused(true);
        inputField.setResponder(text -> {
            if (!text.equals(lastText)) {
                playTypingSound();
                lastText = text;
            }
        });
        this.addRenderableWidget(inputField);

        outputHistory.add("Voices of the Mines OS v1.0");
        outputHistory.add("Type a command...");
    }

    private void playTypingSound() {
        if (this.minecraft == null || this.minecraft.getSoundManager() == null) return;

        int rand = (int)(Math.random() * 5) + 1;
        net.minecraft.sounds.SoundEvent sound = switch (rand) {
            case 1 -> VotmSounds.CLICK1.get();
            case 2 -> VotmSounds.CLICK2.get();
            case 3 -> VotmSounds.CLICK3.get();
            case 4 -> VotmSounds.CLICK4.get();
            default -> VotmSounds.CLICK5.get();
        };

        float pitch = 0.9F + (float)(Math.random() * 0.2F);
        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(sound, pitch, 1.0F));
    }

    public void addOutput(String line) {
        outputHistory.add(line);
        if (line.startsWith("[IMG]")) {
            for (int i = 0; i < 7; i++) {
                outputHistory.add("[PAD]");
            }
        }

        while (outputHistory.size() > 200) {
            outputHistory.remove(0);
        }
        scrollOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!outputHistory.isEmpty()) {
            scrollOffset -= (int) Math.signum(delta) * 2;

            // Ограничители
            if (scrollOffset < 0) scrollOffset = 0;
            int maxScroll = Math.max(0, outputHistory.size() - 14);
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;

            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();

        if (activeCalTarget != null) {
            calTimer++;
            if (calTimer >= 5) {
                calTimer = 0;
                activeCalProgress += (float)(Math.random() * 4 + 2); // прыжки по 2-6%
                if (activeCalProgress >= 100) {
                    addOutput("> Satellite precision [\u00A7a100.00%\u00A7f]");
                    addOutput("\u00A7a> Completed\u00A7f");
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SetCalibrationPacket(activeCalTarget)
                    );
                    activeCalTarget = null;
                } else {
                    addOutput(String.format("> Satellite precision [%.2f%%]", activeCalProgress));
                }
            }
        } else if (!calQueue.isEmpty()) {
            activeCalTarget = calQueue.poll();
            activeCalTarget = activeCalTarget.substring(0, 1).toUpperCase() + activeCalTarget.substring(1).toLowerCase();
            addOutput("> Pinging server \"" + activeCalTarget + "\"");
            addOutput("\u00A7b> Server found\u00A7f");
            activeCalProgress = (float)(Math.random() * 40 + 40); // Начинаем с 40-80%
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // ENTER
            String cmd = inputField.getValue().trim();
            if (!cmd.isEmpty()) {
                inputField.setValue("");
                addOutput(cmd);

                String lowerCmd = cmd.toLowerCase();
                if (lowerCmd.equals("sd.calall")) {
                    String[] sats = {"Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo", "Tango", "Victor", "Echo", "Xray", "Yankee", "Uniform", "Sierra", "Whiskey", "Golf", "Delta", "Charlie", "Bravo", "Hotel", "India", "Juliett", "Foxtrot"};
                    for (String s : sats) calQueue.add(s);
                } else if (lowerCmd.startsWith("sd.call ")) {
                    String[] args = cmd.split(" ");
                    if (args.length > 1) calQueue.add(args[1]);
                } else if (lowerCmd.equals("clear")) {
                    outputHistory.clear();
                } else {
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.ConsoleCommandPacket(cmd, blockPos)
                    );
                }
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int w = 400; int h = 240;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        guiGraphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFFFFAA00);
        guiGraphics.fill(x, y, x + w, y + h, 0xFF000000); // Фон

        guiGraphics.drawString(this.font, "CONSOLE", x + 10, y + 5, 0xFFFFFF, false);
        guiGraphics.fill(x, y + 18, x + w, y + 19, 0xFFFFAA00);

        int rightX = x + 270;
        guiGraphics.fill(rightX - 1, y + 19, rightX, y + h, 0xFFFFAA00);

        int ty = y + 25;
        guiGraphics.drawString(this.font, "sv.ping", rightX + 5, ty, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Ping the server(s)", rightX + 5, ty + 10, 0x55FF55, false);

        guiGraphics.drawString(this.font, "sd.calall", rightX + 5, ty + 30, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Callibrate all", rightX + 5, ty + 40, 0x55FF55, false);

        guiGraphics.drawString(this.font, "sd.call \"name\"", rightX + 5, ty + 60, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Callibrate satellite", rightX + 5, ty + 70, 0x55FF55, false);

        guiGraphics.drawString(this.font, "day", rightX + 5, ty + 90, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Prints out current day", rightX + 5, ty + 100, 0x55FF55, false);

        guiGraphics.drawString(this.font, "sv.hash", rightX + 5, ty + 120, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Prints hashcode", rightX + 5, ty + 130, 0x55FF55, false);

        int drawY = y + h - 45;

        int startIndex = outputHistory.size() - 1 - scrollOffset;

        for (int i = startIndex; i >= 0; i--) {
            if (drawY < y + 25) break;

            String line = outputHistory.get(i);

            if (line.equals("[PAD]")) {
                drawY -= 12;
            } else if (line.startsWith("[IMG]")) {
                String imageName = line.substring(5);
                net.minecraft.resources.ResourceLocation location = getImageLocation(imageName);
                if (location != null) {
                    int targetWidth = 120;
                    int targetHeight = 87;
                    guiGraphics.blit(location, x + 10, drawY, 0, 0, targetWidth, targetHeight, targetWidth, targetHeight);
                }
                drawY -= 12;
            } else {
                guiGraphics.drawString(this.font, line, x + 10, drawY, 0xFFFFFF, false);
                drawY -= 12;
            }
        }

        guiGraphics.drawString(this.font, ">", x + 5, y + h - 25, 0xFFFFFF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private net.minecraft.resources.ResourceLocation getImageLocation(String name) {
        return switch (name) {
            case "floppa" -> new net.minecraft.resources.ResourceLocation(net.votmdevs.voicesofthemines.VoicesOfTheMines.MODID, "textures/gui/floppa_ascii.png");
            case "bingus" -> new net.minecraft.resources.ResourceLocation(net.votmdevs.voicesofthemines.VoicesOfTheMines.MODID, "textures/gui/bingus_ascii.png");
            case "alien" -> new net.minecraft.resources.ResourceLocation(net.votmdevs.voicesofthemines.VoicesOfTheMines.MODID, "textures/gui/alien_ascii.png");
            case "argemia" -> new net.minecraft.resources.ResourceLocation(net.votmdevs.voicesofthemines.VoicesOfTheMines.MODID, "textures/gui/argemia_ascii.png");
            default -> null;
        };
    }
}