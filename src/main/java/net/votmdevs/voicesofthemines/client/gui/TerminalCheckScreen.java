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

        String[] images = {"np1", "np2", "np3", "np4", "np5", "np6", "np7", "np8", "np10", "np19"};
        String chosenImg = images[new Random().nextInt(images.length)];
        this.selectedImage = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/" + chosenImg + ".png");
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

        if (HAS_ACTIVE_SIGNAL && arrivalTimer <= 0) {
            guiGraphics.drawString(this.font, "0", topLeftX + 5, topLeftY + 5, 0xFF5555, false);
            guiGraphics.drawString(this.font, ">", topLeftX + 15, topLeftY + 5, 0xFFAA00, false);

            String objectNameText = "UNKNOWN";
            String t = CURRENT_SIGNAL_TYPE;

            if (t.equals("mars") || t.equals("venus") || t.equals("earth") || t.equals("mercury") ||
                    t.equals("enceladus") || t.equals("ceres") || t.equals("dione") || t.equals("bennu") ||
                    t.equals("makemake") || t.equals("rhea") || t.equals("iris") || t.equals("amazur") ||
                    t.equals("vion") || t.equals("subplanet") || t.equals("europa") || t.equals("moon") ||
                    t.equals("jupiter") || t.equals("uranus") || t.equals("neptune") || t.equals("saturn") ||
                    t.equals("hilero") || t.equals("votv_earth") || t.equals("fard") || t.equals("ironlung")) {
                objectNameText = "planet_" + t;
            } else if (t.equals("retroplanet")) {
                objectNameText = "planet_retro_planet";
            } else if (t.startsWith("exogen")) {
                objectNameText = "unind_object";
            } else if (t.startsWith("siggen") || t.startsWith("siggenus") || t.equals("faces") || t.equals("hairy")) {
                objectNameText = "unidentified_planet";
            } else if (!t.isEmpty()) {
                objectNameText = "generic planet";
            }

            guiGraphics.drawString(this.font, objectNameText, topLeftX + 25, topLeftY + 5, 0xFFFF55, false);
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
        guiGraphics.fill(topLeftX, btnY, topLeftX + 60, btnY + 20, isPlaying ? 0xFFFF5555 : 0xFF55FF55);
        guiGraphics.drawString(this.font, isPlaying ? "STOP" : "PLAY", topLeftX + 15, btnY + 6, 0xFF000000, false);

        int finishBtnX = topLeftX + 70;
        guiGraphics.fill(finishBtnX, btnY, finishBtnX + 60, btnY + 20, 0xFF5555FF);
        guiGraphics.drawString(this.font, "FINISH", finishBtnX + 15, btnY + 6, 0xFFFFFFFF, false);

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

                ResourceLocation imageToDraw = selectedImage;
                if (CURRENT_SIGNAL_TYPE.startsWith("siggenus")) {
                    imageToDraw = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/" + CURRENT_SIGNAL_TYPE + ".png");
                } else if (CURRENT_SIGNAL_TYPE.equals("hairy")) {
                    imageToDraw = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/hairy.png");
                }

                guiGraphics.blit(imageToDraw, rightX, topLeftY, 0, 0, 128, 128, 128, 128);
                RenderSystem.disableScissor();
            }
        }
// SIGNAL UNIQ TEXTS
        int textWinY = topLeftY + 140;
        guiGraphics.fill(rightX - 1, textWinY - 1, rightX + 160, textWinY + 80, 0xFFFFFFFF);
        guiGraphics.fill(rightX, textWinY, rightX + 159, textWinY + 79, 0xFF000000);

        if (HAS_ACTIVE_SIGNAL && arrivalTimer <= 0) {
            String text = "[NO_TEXT_DATA_FOUND]";

            if (CURRENT_SIGNAL_TYPE.equals("retroplanet")) {
                text = "play with me\nI'm coming\nwait\nfor me\n\n              :)";
            } else if (CURRENT_SIGNAL_TYPE.equals("iris")) {
                text = "een days since last supply arrive. waiting anot\n..........ell damage have fixthi...\ns and i do not have too many \ntape and plastic, do not have buried this not good enough.\n hope I survive when supply...";
            } else if (CURRENT_SIGNAL_TYPE.equals("votv_earth")) {
                text = "M R \nD R \nN O S E  :D";
            } else if (CURRENT_SIGNAL_TYPE.equals("vion")) {
                text = "...S OBJECT AT ALL COSTS AVOID THIS OBJECT AT ALL COSTS food AVOID \nTHIS OBJECT AT ALL COSTS AVOID THIS OBJECT AT ALL COSTS AVOID THIS \nOBJECT AT ALL COSTS AVOID THIS food OBJECT AT ALL COSTS AVOID THIS \n....";
            }

            guiGraphics.drawWordWrap(this.font, Component.literal(text), rightX + 5, textWinY + 5, 150, 0x55FF55);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private net.minecraft.sounds.SoundEvent getStage3Sound(String type) {
        if (type.equals("mars")) {
            return new Random().nextInt(100) < 5 ? KerfurSounds.SIGNAL_BDAY.get() : KerfurSounds.SIGNAL_PLANET_MARS.get();
        }
        switch (type) {
            // Старые
            case "venus": return KerfurSounds.SIGNAL_PLANET_VENUS.get();
            case "enceladus": return KerfurSounds.SIGNAL_PLANET_ENCELADUS.get();
            case "ceres": return KerfurSounds.SIGNAL_PLANET_CERES.get();
            case "dione": return KerfurSounds.SIGNAL_PLANET_DIONE.get();
            case "bennu": return KerfurSounds.SIGNAL_PLANET_BENNU.get();
            case "mercury": return KerfurSounds.SIGNAL_PLANET_MERCURY.get();
            case "siggen1": return KerfurSounds.SIGGEN1.get();
            case "siggen2": return KerfurSounds.SIGGEN2.get();
            case "siggen3": return KerfurSounds.SIGGEN3.get();
            case "siggen4": return KerfurSounds.SIGGEN4.get();
            case "siggen5": return KerfurSounds.SIGGEN5.get();
            case "siggen6": return KerfurSounds.SIGGEN6.get();
            case "earth": return KerfurSounds.SIGNAL_PLANET_EARTH.get();
            case "faces": return KerfurSounds.SIGNAL_FACES.get();
            case "retroplanet": return KerfurSounds.SIGNAL_PLANET_RETRO.get();
            case "siggenus1": return KerfurSounds.SIGNAL_SIGGENUS1.get();
            case "siggenus2": return KerfurSounds.SIGNAL_SIGGENUS2.get();
            case "siggenus3": return KerfurSounds.SIGNAL_SIGGENUS3.get();
            case "siggenus4": return KerfurSounds.SIGNAL_SIGGENUS4.get();
            case "siggenus5": return KerfurSounds.SIGNAL_SIGGENUS5.get();
            case "siggenus6": return KerfurSounds.SIGNAL_SIGGENUS6.get();
            case "siggenus7": return KerfurSounds.SIGNAL_SIGGENUS7.get();
            case "siggenus8": return KerfurSounds.SIGNAL_SIGGENUS8.get();
            // Новые
            case "makemake": return KerfurSounds.SIGNAL_PLANET_MAKEMAKE.get();
            case "rhea": return KerfurSounds.SIGNAL_PLANET_RHEA.get();
            case "iris": return KerfurSounds.SIGNAL_IRIS.get();
            case "amazur": return KerfurSounds.SIGNAL_AMAZUR.get();
            case "vion": return KerfurSounds.SIGNAL_VION.get();
            case "subplanet": return KerfurSounds.SIGNAL_SUBPLANET.get();
            case "europa": return KerfurSounds.SIGNAL_PLANET_EUROPA.get();
            case "moon": return KerfurSounds.SIGNAL_PLANET_MOON.get();
            case "jupiter": return KerfurSounds.SIGNAL_PLANET_JUPITER.get();
            case "uranus": return KerfurSounds.SIGNAL_PLANET_URANUS.get();
            case "neptune": return KerfurSounds.SIGNAL_PLANET_NEPTUNE.get();
            case "saturn": return KerfurSounds.SIGNAL_PLANET_SATURN.get();
            case "hilero": return KerfurSounds.SIGNAL_HILERO.get();
            case "exogen1": return KerfurSounds.EXOGEN1.get();
            case "exogen2": return KerfurSounds.EXOGEN2.get();
            case "votv_earth": return KerfurSounds.VOTV_EARTH.get();
            case "hairy": return KerfurSounds.SIGNAL_HAIRY.get();
            case "fard": return KerfurSounds.SIGNAL_FARD.get();
            case "ironlung": return KerfurSounds.IRONLUNGSIGNAL.get();
            default: return KerfurSounds.RAW1.get();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!HAS_ACTIVE_SIGNAL || arrivalTimer > 0) return super.mouseClicked(mouseX, mouseY, button);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 190;
        int startY = centerY - 120;
        int topLeftX = startX + 20;
        int btnY = startY + 210;
        int finishBtnX = topLeftX + 70;

        if (mouseX >= topLeftX && mouseX <= topLeftX + 60 && mouseY >= btnY && mouseY <= btnY + 20) {
            if (isPlaying) {
                if (currentSound != null) Minecraft.getInstance().getSoundManager().stop(currentSound);
                isPlaying = false;
            } else {
                net.minecraft.sounds.SoundEvent[] sounds;

                if (CURRENT_SIGNAL_LEVEL >= 3) {
                    // === СТАДИЯ 3 ===
                    sounds = new net.minecraft.sounds.SoundEvent[]{ getStage3Sound(CURRENT_SIGNAL_TYPE) };
                } else if (CURRENT_SIGNAL_LEVEL == 2) {
                    // === СТАДИЯ 2 ===
                    sounds = new net.minecraft.sounds.SoundEvent[]{KerfurSounds.LOW1.get(), KerfurSounds.LOW2.get(), KerfurSounds.LOW3.get(), KerfurSounds.LOW4.get(), KerfurSounds.LOW5.get(), KerfurSounds.LOW6.get(), KerfurSounds.LOW7.get(), KerfurSounds.LOW8.get()};
                } else if (CURRENT_SIGNAL_LEVEL == 1) {
                    // === СТАДИЯ 1 ===
                    sounds = new net.minecraft.sounds.SoundEvent[]{KerfurSounds.NOISY1.get(), KerfurSounds.NOISY2.get(), KerfurSounds.NOISY3.get(), KerfurSounds.NOISY4.get(), KerfurSounds.NOISY5.get(), KerfurSounds.NOISY6.get(), KerfurSounds.NOISY7.get(), KerfurSounds.NOISY8.get()};
                } else {
                    // === СТАДИЯ 0 (RAW) ===
                    sounds = new net.minecraft.sounds.SoundEvent[]{KerfurSounds.RAW1.get(), KerfurSounds.RAW2.get(), KerfurSounds.RAW3.get(), KerfurSounds.RAW4.get(), KerfurSounds.RAW5.get(), KerfurSounds.RAW6.get(), KerfurSounds.RAW7.get(), KerfurSounds.RAW8.get()};
                }

                net.minecraft.sounds.SoundEvent selectedSnd = sounds[new Random().nextInt(sounds.length)];
                currentSound = SimpleSoundInstance.forUI(selectedSnd, 1.0f, 1.0f);
                Minecraft.getInstance().getSoundManager().play(currentSound);
                isPlaying = true;
            }
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            return true;
        }

        if (mouseX >= finishBtnX && mouseX <= finishBtnX + 60 && mouseY >= btnY && mouseY <= btnY + 20) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
            if (currentSound != null) Minecraft.getInstance().getSoundManager().stop(currentSound);
            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.FinishCheckPacket(terminalPos));
            HAS_ACTIVE_SIGNAL = false;
            isPlaying = false;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}