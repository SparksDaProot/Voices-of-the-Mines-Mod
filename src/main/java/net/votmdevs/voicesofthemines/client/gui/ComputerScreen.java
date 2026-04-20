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

public class ComputerScreen extends Screen {
    private static final ResourceLocation LOADING_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/loading_sheet.png");

    public static int POINTS = 0;
    public static int UPG_CURSOR = 0;
    public static int UPG_PING = 0;
    public static int UPG_PROC_SPEED = 0;
    public static int UPG_PROC_LVL = 0;

    private final BlockPos blockPos;

    private boolean isLoading = true;
    private int loadingFrame = 0;
    private int loadingTick = 0;
    private int loopCount = 0;

    private int activeTab = 0; // 0 = Upgrades, 1 = Store, 2 = Email

    private SimpleSoundInstance startupSound;
    private GuiLoopSound workingSound;

    public ComputerScreen(BlockPos pos) {
        super(Component.literal("Base Computer"));
        this.blockPos = pos;
    }

    @Override
    protected void init() {
        super.init();

        if (startupSound == null) {
            startupSound = SimpleSoundInstance.forUI(KerfurSounds.PC_STARTUP.get(), 1.0f, 1.0f);
            Minecraft.getInstance().getSoundManager().play(startupSound);
        }

        if (workingSound == null) {
            workingSound = new GuiLoopSound(KerfurSounds.PC_WORKING_LOOP.get(), 0.5f);
            Minecraft.getInstance().getSoundManager().play(workingSound);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void removed() {
        if (startupSound != null) Minecraft.getInstance().getSoundManager().stop(startupSound);
        if (workingSound != null) Minecraft.getInstance().getSoundManager().stop(workingSound);
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();
        if (isLoading) {
            loadingTick++;
            if (loadingTick % 4 == 0) {
                loadingFrame++;
                if (loadingFrame >= 10) {
                    loadingFrame = 0;
                    loopCount++;
                    if (loopCount >= 3) {
                        isLoading = false;
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (isLoading) {
            RenderSystem.enableBlend();
            int drawW = 192 * 2;
            int drawH = 108 * 2;

            int x = centerX - (drawW / 2);
            int y = centerY - (drawH / 2);

            guiGraphics.blit(LOADING_TEX, x, y, drawW, drawH, 0, loadingFrame * 108, 192, 108, 192, 1080);
            RenderSystem.disableBlend();
            return;
        }

        // GUI
        int panelWidth = 400;
        int panelHeight = 240;
        int startX = centerX - panelWidth / 2;
        int startY = centerY - panelHeight / 2;

        guiGraphics.fill(startX - 2, startY - 2, startX + panelWidth + 2, startY + panelHeight + 2, 0xFFFFFFFF);
        guiGraphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xFF000000);

        guiGraphics.drawString(this.font, "Points: " + POINTS, startX + 10, startY + panelHeight - 15, 0x55FF55, false);

        drawTab(guiGraphics, startX + 10, startY + 10, 80, "Upgrades", activeTab == 0);
        drawTab(guiGraphics, startX + 100, startY + 10, 80, "Store", activeTab == 1);
        drawTab(guiGraphics, startX + 190, startY + 10, 80, "Email", activeTab == 2);

        if (activeTab == 0) {
            guiGraphics.drawString(this.font, "TERMINAL_FIND", startX + 20, startY + 40, 0xFFFFFF, false);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 55, "cursor_speed", UPG_CURSOR, 16, getCost("cursor_speed", UPG_CURSOR, 16), mouseX, mouseY);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 75, "ping_cooldown", UPG_PING, 16, getCost("ping_cooldown", UPG_PING, 16), mouseX, mouseY);

            guiGraphics.drawString(this.font, "TERMINAL_PROCESSING", startX + 20, startY + 105, 0xFFFFFF, false);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 120, "processing_speed", UPG_PROC_SPEED, 16, getCost("processing_speed", UPG_PROC_SPEED, 16), mouseX, mouseY);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 140, "processing_level", UPG_PROC_LVL, 3, getCost("processing_level", UPG_PROC_LVL, 3), mouseX, mouseY);
        } else if (activeTab == 1) {
            guiGraphics.drawString(this.font, "Store is empty right now...", startX + 20, startY + 40, 0x888888, false);
        } else if (activeTab == 2) {
            guiGraphics.drawString(this.font, "No new emails.", startX + 20, startY + 40, 0x888888, false);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private int getCost(String type, int lvl, int maxLvl) {
        if (lvl >= maxLvl) return -1;
        if (type.equals("cursor_speed")) return 5 + (lvl * 5);
        if (type.equals("ping_cooldown")) return 15 + (lvl * 5);
        if (type.equals("processing_speed")) return 20 + (lvl * 5);
        if (type.equals("processing_level")) return 30 + (lvl * 20);
        return 999;
    }

    private void drawTab(GuiGraphics gui, int x, int y, int w, String text, boolean isActive) {
        int color = isActive ? 0xFF00FFFF : 0xFFFFAA00;
        gui.fill(x, y, x + w, y + 15, color);
        gui.fill(x + 1, y + 1, x + w - 1, y + 14, 0xFF000000);
        gui.drawString(this.font, text, x + 5, y + 4, color, false);
    }

    private void drawUpgradeRow(GuiGraphics gui, int x, int y, String name, int currentLvl, int maxLvl, int cost, int mouseX, int mouseY) {
        boolean isHovered = mouseX >= x && mouseX <= x + 40 && mouseY >= y && mouseY <= y + 15;
        int btnColor = isHovered && cost != -1 && POINTS >= cost ? 0xFF00FF00 : 0xFFFFAA00;

        gui.fill(x, y, x + 40, y + 15, btnColor);
        gui.fill(x + 1, y + 1, x + 39, y + 14, 0xFF000000);
        gui.drawString(this.font, currentLvl + "/" + maxLvl, x + 5, y + 4, btnColor, false);

        int barX = x + 50;
        int barW = 100;
        int segW = (barW / maxLvl) - 1;
        if (segW < 1) segW = 1;

        for (int i = 0; i < currentLvl; i++) {
            int r = 255 - (int)(((float)i/maxLvl) * 255);
            int g = (int)(((float)i/maxLvl) * 255);
            int c = 0xFF000000 | (r << 16) | (g << 8);
            gui.fill(barX + (i * (segW + 1)), y + 2, barX + (i * (segW + 1)) + segW, y + 13, c);
        }
        gui.fill(barX, y + 14, barX + barW, y + 15, 0xFF333333);

        String costStr = cost == -1 ? "MAX" : cost + "";
        gui.drawString(this.font, costStr, barX + barW + 10, y + 4, 0xFF00FFFF, false);
        gui.drawString(this.font, name, barX + barW + 60, y + 4, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isLoading) return true;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 200;
        int startY = centerY - 120;

        if (mouseY >= startY + 10 && mouseY <= startY + 25) {
            if (mouseX >= startX + 10 && mouseX <= startX + 90) { activeTab = 0; Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F)); }
            if (mouseX >= startX + 100 && mouseX <= startX + 180) { activeTab = 1; Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F)); }
            if (mouseX >= startX + 190 && mouseX <= startX + 270) { activeTab = 2; Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F)); }
        }

        if (activeTab == 0) {
            checkUpgradeClick(mouseX, mouseY, startX + 20, startY + 55, "cursor_speed", UPG_CURSOR, 16);
            checkUpgradeClick(mouseX, mouseY, startX + 20, startY + 75, "ping_cooldown", UPG_PING, 16);
            checkUpgradeClick(mouseX, mouseY, startX + 20, startY + 120, "processing_speed", UPG_PROC_SPEED, 16);
            checkUpgradeClick(mouseX, mouseY, startX + 20, startY + 140, "processing_level", UPG_PROC_LVL, 3);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void checkUpgradeClick(double mX, double mY, int x, int y, String type, int curLvl, int maxLvl) {
        if (mX >= x && mX <= x + 40 && mY >= y && mY <= y + 15) {
            int cost = getCost(type, curLvl, maxLvl);
            if (cost != -1 && POINTS >= cost) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.BuyUpgradePacket(type));
            } else {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUG_ALERT.get(), 1.0F, 0.5F));
            }
        }
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
    }
}