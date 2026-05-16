package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.votmdevs.voicesofthemines.world.SignalType; // ИМПОРТИРУЕМ НАШ ENUM

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TerminalCheckScreen extends Screen {
    public static boolean HAS_ACTIVE_SIGNAL = false;
    public static String CURRENT_SIGNAL_TYPE = "";
    public static int CURRENT_SIGNAL_LEVEL = 0;

    private final BlockPos terminalPos;

    private int arrivalTimer = 60;
    private float imageProgress = 0f;

    private boolean isPlaying = false;
    private SimpleSoundInstance currentSound;
    private float[] eqBars = new float[16];

    private final ResourceLocation selectedImage;

    public TerminalCheckScreen(BlockPos pos) {
        super(Component.literal("Terminal Check"));
        this.terminalPos = pos;
        // generic noises
        String[] images = {"np1", "np2", "np3", "np4", "np5", "np6", "np7", "np8", "np10", "np19"};
        String chosenImg = images[new Random().nextInt(images.length)];
        this.selectedImage = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/" + chosenImg + ".png");
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void removed() {
        if (currentSound != null) Minecraft.getInstance().getSoundManager().stop(currentSound);
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();

        if (HAS_ACTIVE_SIGNAL) {
            if (arrivalTimer > 0) {
                arrivalTimer--;
            } else {
                if (imageProgress < 1.0f) {
                    imageProgress += 0.01f;
                }
            }
        }

        if (isPlaying) {
            Random rand = new Random();
            for (int i = 0; i < eqBars.length; i++) {
                eqBars[i] += (rand.nextFloat() - 0.5f) * 0.4f;
                if (eqBars[i] < 0.1f) eqBars[i] = 0.1f;
                if (eqBars[i] > 1.0f) eqBars[i] = 1.0f;
            }
        } else {
            for (int i = 0; i < eqBars.length; i++) eqBars[i] = 0.0f;
        }

        if (isPlaying && currentSound != null && !Minecraft.getInstance().getSoundManager().isActive(currentSound)) {
            isPlaying = false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 380;
        int panelHeight = 240;
        int startX = centerX - panelWidth / 2;
        int startY = centerY - panelHeight / 2;

        guiGraphics.fill(startX - 2, startY - 2, startX + panelWidth + 2, startY + panelHeight + 2, 0xFFFFFFFF);
        guiGraphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xFF000000);

        int topLeftX = startX + 20;
        int topLeftY = startY + 20;
        guiGraphics.fill(topLeftX - 1, topLeftY - 1, topLeftX + 160, topLeftY + 40, 0xFFFFFFFF);
        guiGraphics.fill(topLeftX, topLeftY, topLeftX + 159, topLeftY + 39, 0xFF000000);

        SignalType currentType = SignalType.fromId(CURRENT_SIGNAL_TYPE); // БЕРЕМ ENUM ЗДЕСЬ

        if (HAS_ACTIVE_SIGNAL && arrivalTimer <= 0) {
            guiGraphics.drawString(this.font, "0", topLeftX + 5, topLeftY + 5, 0xFF5555, false);
            guiGraphics.drawString(this.font, ">", topLeftX + 15, topLeftY + 5, 0xFFAA00, false);

            guiGraphics.drawString(this.font, currentType.getDisplayName(), topLeftX + 25, topLeftY + 5, 0xFFFF55, false); // ИМЯ
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH-mm");
            guiGraphics.drawString(this.font, dtf.format(LocalDateTime.now()), topLeftX + 5, topLeftY + 20, 0xFFFFFF, false);
        }

        int botLeftY = startY + 80;
        guiGraphics.fill(topLeftX - 1, botLeftY - 1, topLeftX + 160, botLeftY + 120, 0xFFFFFFFF);
        guiGraphics.fill(topLeftX, botLeftY, topLeftX + 159, botLeftY + 119, 0xFF000000);

        int barWidth = 6;
        int eqSpacing = 3;
        for (int i = 0; i < eqBars.length; i++) {
            int barHeight = (int) (eqBars[i] * 100);
            int barX = topLeftX + 10 + (i * (barWidth + eqSpacing));
            int barY = botLeftY + 110 - barHeight;
            int blueAmount = (int) ((i / (float)eqBars.length) * 255);
            int color = 0xFF000000 | (255 << 16) | (0 << 8) | blueAmount;
            guiGraphics.fill(barX, barY, barX + barWidth, botLeftY + 110, color);
        }

        int btnY = startY + 210;
        int eraseX = topLeftX - 15;
        int playX = topLeftX + 10;
        int finishBtnX = playX + 65;
        int ejectX = finishBtnX + 65;

        // BUTTONS
        if (!isPlaying) {
            //  Erase
            guiGraphics.fill(eraseX, btnY, eraseX + 20, btnY + 20, 0xFF3333FF);
            guiGraphics.fill(eraseX + 1, btnY + 1, eraseX + 19, btnY + 19, 0xFF5555FF);

            // Eject
            guiGraphics.fill(ejectX, btnY, ejectX + 20, btnY + 20, 0xFFFF3333);
            guiGraphics.fill(ejectX + 1, btnY + 1, ejectX + 19, btnY + 19, 0xFFFF5555);
        }

        // PLAY / STOP
        guiGraphics.fill(playX, btnY, playX + 60, btnY + 20, isPlaying ? 0xFFFF5555 : 0xFF55FF55);
        guiGraphics.drawString(this.font, isPlaying ? "STOP" : "PLAY", playX + 15, btnY + 6, 0xFF000000, false);

        // FINISH
        guiGraphics.fill(finishBtnX, btnY, finishBtnX + 60, btnY + 20, 0xFF5555FF);
        guiGraphics.drawString(this.font, "FINISH", finishBtnX + 15, btnY + 6, 0xFFFFFFFF, false);


        // noise image panel
        int rightX = startX + 200;
        guiGraphics.fill(rightX - 1, topLeftY - 1, rightX + 128 + 1, topLeftY + 128 + 1, 0xFFFFFFFF);
        guiGraphics.fill(rightX, topLeftY, rightX + 128, topLeftY + 128, 0xFF000000);


        if (HAS_ACTIVE_SIGNAL) {
            if (arrivalTimer > 0) {
                guiGraphics.drawString(this.font, "RECEIVING...", rightX + 40, topLeftY + 60, 0x888888, false);
            } else {
                double scale = this.minecraft.getWindow().getGuiScale();
                int scissorHeight = (int) (128 * imageProgress);
                RenderSystem.enableScissor((int)(rightX * scale), (int)(this.height * scale) - (int)((topLeftY + scissorHeight) * scale), (int)(128 * scale), (int)(scissorHeight * scale));

                // ENUM TEXTURE
                ResourceLocation imageToDraw = currentType.getCheckTexture(selectedImage);
                guiGraphics.blit(imageToDraw, rightX, topLeftY, 0, 0, 128, 128, 128, 128);
                RenderSystem.disableScissor();
            }
        }

        // SIGNAL UNIQ TEXTS
        int textWinY = topLeftY + 140;
        guiGraphics.fill(rightX - 1, textWinY - 1, rightX + 160, textWinY + 80, 0xFFFFFFFF);
        guiGraphics.fill(rightX, textWinY, rightX + 159, textWinY + 79, 0xFF000000);

        if (HAS_ACTIVE_SIGNAL && arrivalTimer <= 0) {
            // ENUM TEXT
            guiGraphics.drawWordWrap(this.font, Component.literal(currentType.getText()), rightX + 5, textWinY + 5, 150, 0x55FF55);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 190;
        int startY = centerY - 120;
        int topLeftX = startX + 20;
        int btnY = startY + 210;
        int eraseX = topLeftX - 15;
        int finishBtnX = topLeftX + 70;
        int ejectX = finishBtnX + 70;

        // erase
        if (!isPlaying && mouseX >= eraseX && mouseX <= eraseX + 20 && mouseY >= btnY && mouseY <= btnY + 20) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.EraseDrivePacket(terminalPos));

            HAS_ACTIVE_SIGNAL = false;
            this.minecraft.setScreen(null);
            return true;
        }

        // eject
        if (!isPlaying && mouseX >= ejectX && mouseX <= ejectX + 20 && mouseY >= btnY && mouseY <= btnY + 20) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.EjectDrivePacket(terminalPos));

            HAS_ACTIVE_SIGNAL = false;
            this.minecraft.setScreen(null);
            return true;
        }

        if (!HAS_ACTIVE_SIGNAL || arrivalTimer > 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (mouseX >= topLeftX && mouseX <= topLeftX + 60 && mouseY >= btnY && mouseY <= btnY + 20) {
            if (isPlaying) {
                if (currentSound != null) Minecraft.getInstance().getSoundManager().stop(currentSound);
                isPlaying = false;
            } else {
                net.minecraft.sounds.SoundEvent[] sounds;
                SignalType currentType = SignalType.fromId(CURRENT_SIGNAL_TYPE);

                // ENUM EVENT AND SOUNDS
                if (currentType == SignalType.EVIL) {
                    sounds = new net.minecraft.sounds.SoundEvent[]{ VotmSounds.SIGNAL_EVIL.get() };
                } else if (CURRENT_SIGNAL_LEVEL >= 3) {
                    sounds = new net.minecraft.sounds.SoundEvent[]{ currentType.getSound() };
                } else if (CURRENT_SIGNAL_LEVEL == 2) {
                    sounds = new net.minecraft.sounds.SoundEvent[]{VotmSounds.LOW1.get(), VotmSounds.LOW2.get(), VotmSounds.LOW3.get(), VotmSounds.LOW4.get(), VotmSounds.LOW5.get(), VotmSounds.LOW6.get(), VotmSounds.LOW7.get(), VotmSounds.LOW8.get()};
                } else if (CURRENT_SIGNAL_LEVEL == 1) {
                    sounds = new net.minecraft.sounds.SoundEvent[]{VotmSounds.NOISY1.get(), VotmSounds.NOISY2.get(), VotmSounds.NOISY3.get(), VotmSounds.NOISY4.get(), VotmSounds.NOISY5.get(), VotmSounds.NOISY6.get(), VotmSounds.NOISY7.get(), VotmSounds.NOISY8.get()};
                } else {
                    sounds = new net.minecraft.sounds.SoundEvent[]{VotmSounds.RAW1.get(), VotmSounds.RAW2.get(), VotmSounds.RAW3.get(), VotmSounds.RAW4.get(), VotmSounds.RAW5.get(), VotmSounds.RAW6.get(), VotmSounds.RAW7.get(), VotmSounds.RAW8.get()};
                }

                net.minecraft.sounds.SoundEvent selectedSnd = sounds[new Random().nextInt(sounds.length)];
                currentSound = SimpleSoundInstance.forUI(selectedSnd, 1.0f, 1.0f);
                Minecraft.getInstance().getSoundManager().play(currentSound);
                isPlaying = true;

                // EVENT TRIGGER
                currentType.triggerEvent();
            }
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            return true;
        }

        if (mouseX >= finishBtnX && mouseX <= finishBtnX + 60 && mouseY >= btnY && mouseY <= btnY + 20) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            if (currentSound != null) Minecraft.getInstance().getSoundManager().stop(currentSound);
            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.FinishCheckPacket(terminalPos));
            HAS_ACTIVE_SIGNAL = false;
            isPlaying = false;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}