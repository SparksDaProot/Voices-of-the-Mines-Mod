package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputerScreen extends Screen {
    private static final ResourceLocation LOADING_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/loading_sheet.png");

    private static final ResourceLocation DELETE_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/delete.png");
    private static final ResourceLocation EMAIL_USER_TEX = new ResourceLocation(KerfurMod.MODID, "textures/gui/terminal/email_user_generic.png");

    public static int POINTS = 0;
    public static int UPG_CURSOR = 0;
    public static int UPG_PING = 0;
    public static int UPG_PROC_SPEED = 0;
    public static int UPG_PROC_LVL = 0;

    public static List<net.votmdevs.voicesofthemines.world.PlayerData.Email> EMAILS = new ArrayList<>();
    private int selectedEmailIndex = -1;
    private float emailListScroll = 0f;
    private float emailTextScroll = 0f;

    private final BlockPos blockPos;

    private boolean isLoading = true;
    private int loadingFrame = 0;
    private int loadingTick = 0;
    private int loopCount = 0;

    private int activeTab = 0; // 0 = Upgrades, 1 = Store, 2 = Email

    private SimpleSoundInstance startupSound;
    private GuiLoopSound workingSound;

    // store system
    private static class StoreItem {
        String id; String name; ItemStack icon; int price; int size;
        public StoreItem(String id, String name, ItemStack icon, int price, int size) {
            this.id = id; this.name = name; this.icon = icon; this.price = price; this.size = size;
        }
    }

    private final List<StoreItem> allItems = new ArrayList<>();
    private List<StoreItem> filteredItems = new ArrayList<>();
    private final List<StoreItem> shoppingCart = new ArrayList<>();

    private EditBox searchBox;
    private float storeScroll = 0f;
    private float cartScroll = 0f;

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

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 200;
        int startY = centerY - 120;

        searchBox = new EditBox(this.font, startX + 20, startY + 35, 170, 15, Component.literal("Search"));
        searchBox.setMaxLength(30);
        searchBox.setBordered(true);
        searchBox.setVisible(false);
        this.addRenderableWidget(searchBox);

        // what player can buy
        if (allItems.isEmpty()) {
            allItems.add(new StoreItem("hazard_suit", "Hazard Suit", new ItemStack(KerfurMod.HAZARD_HELMET.get()), 300, 1));
            allItems.add(new StoreItem("hook", "Hook", new ItemStack(KerfurMod.HOOK_ITEM.get()), 50, 1));
            allItems.add(new StoreItem("trash_roll", "Trash bag roll", new ItemStack(KerfurMod.TRASH_ROLL.get()), 16, 1));
            allItems.add(new StoreItem("glasses", "Glasses", new ItemStack(KerfurMod.ACCESSORY_GLASSES.get()), 10, 1));
            allItems.add(new StoreItem("jacket", "Jacket", new ItemStack(KerfurMod.ACCESSORY_JACKET.get()), 10, 1));
            allItems.add(new StoreItem("keypad", "Keypad", new ItemStack(KerfurMod.KEYPAD_ITEM.get()), 30, 1));
            allItems.add(new StoreItem("poster", "Poster", new ItemStack(KerfurMod.POSTER_ITEM.get()), 10, 1));
            allItems.add(new StoreItem("taco", "Taco", new ItemStack(KerfurMod.TACO.get()), 5, 1));
            allItems.add(new StoreItem("toblerone", "Toblerone", new ItemStack(KerfurMod.TOBLERONE.get()), 10, 1));
            allItems.add(new StoreItem("cheese", "Cheese", new ItemStack(KerfurMod.CHEESE.get()), 5, 1));
            allItems.add(new StoreItem("burger", "Burger", new ItemStack(KerfurMod.BURGER.get()), 5, 1));
            allItems.add(new StoreItem("painter_black", "Painter Black", new ItemStack(KerfurMod.PAINTER_BLACK.get()), 50, 1));
            allItems.add(new StoreItem("painter_blue", "Painter Blue", new ItemStack(KerfurMod.PAINTER_BLUE.get()), 50, 1));
            allItems.add(new StoreItem("painter_red", "Painter Red", new ItemStack(KerfurMod.PAINTER_RED.get()), 50, 1));
            allItems.add(new StoreItem("painter_green", "Painter Green", new ItemStack(KerfurMod.PAINTER_GREEN.get()), 50, 1));
            allItems.add(new StoreItem("painter_pink", "Painter Pink", new ItemStack(KerfurMod.PAINTER_PINK.get()), 50, 1));
            allItems.add(new StoreItem("painter_white", "Painter White", new ItemStack(KerfurMod.PAINTER_WHITE.get()), 50, 1));
            allItems.add(new StoreItem("painter_yellow", "Painter Yellow", new ItemStack(KerfurMod.PAINTER_YELLOW.get()), 50, 1));
        }
        updateSearch();
    }

    private void updateSearch() {
        String query = searchBox.getValue().toLowerCase();
        filteredItems.clear();
        for (StoreItem item : allItems) {
            if (item.name.toLowerCase().contains(query)) {
                filteredItems.add(item);
            }
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
        } else {
            updateSearch();
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

        searchBox.setVisible(activeTab == 1);

        if (activeTab == 0) {
            // upgrades
            guiGraphics.drawString(this.font, "TERMINAL_FIND", startX + 20, startY + 40, 0xFFFFFF, false);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 55, "cursor_speed", UPG_CURSOR, 16, getCost("cursor_speed", UPG_CURSOR, 16), mouseX, mouseY);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 75, "ping_cooldown", UPG_PING, 16, getCost("ping_cooldown", UPG_PING, 16), mouseX, mouseY);
            guiGraphics.drawString(this.font, "TERMINAL_PROCESSING", startX + 20, startY + 105, 0xFFFFFF, false);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 120, "processing_speed", UPG_PROC_SPEED, 16, getCost("processing_speed", UPG_PROC_SPEED, 16), mouseX, mouseY);
            drawUpgradeRow(guiGraphics, startX + 20, startY + 140, "processing_level", UPG_PROC_LVL, 3, getCost("processing_level", UPG_PROC_LVL, 3), mouseX, mouseY);
        }
        else if (activeTab == 1) {
            // store
            int listX = startX + 20; int listY = startY + 55; int listW = 170; int listH = 160;
            int cartX = startX + 200; int cartY = startY + 35; int cartW = 180; int cartH = 180;

            double scale = this.minecraft.getWindow().getGuiScale();
            RenderSystem.enableScissor((int)(listX * scale), (int)((this.height - listY - listH) * scale), (int)(listW * scale), (int)(listH * scale));
            for (int i = 0; i < filteredItems.size(); i++) {
                StoreItem item = filteredItems.get(i);
                int itemY = listY + (i * 24) - (int)storeScroll;
                if (itemY > listY + listH || itemY + 24 < listY) continue;
                boolean isHovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= itemY && mouseY <= itemY + 22;
                guiGraphics.fill(listX, itemY, listX + listW, itemY + 22, isHovered ? 0xFF333333 : 0xFF111111);
                guiGraphics.renderItem(item.icon, listX + 2, itemY + 3);
                guiGraphics.drawString(this.font, item.name, listX + 22, itemY + 2, 0xFFFFFF, false);
                guiGraphics.drawString(this.font, "Price: " + item.price, listX + 22, itemY + 12, 0x00FFFF, false);
                guiGraphics.drawString(this.font, "Size: " + item.size, listX + 110, itemY + 12, 0x00FFFF, false);
            }
            RenderSystem.disableScissor();

            guiGraphics.fill(cartX - 1, cartY - 1, cartX + cartW + 1, cartY + cartH + 1, 0xFFFFFFFF);
            guiGraphics.fill(cartX, cartY, cartX + cartW, cartY + cartH, 0xFF000000);

            int totalSize = shoppingCart.stream().mapToInt(i -> i.size).sum();
            int totalPrice = shoppingCart.stream().mapToInt(i -> i.price).sum();

            guiGraphics.drawString(this.font, "Shopping cart", cartX + 5, cartY + 5, 0xFFFFAA00, false);
            guiGraphics.drawString(this.font, totalSize + "/50", cartX + cartW - 35, cartY + 5, 0xFF00FFFF, false);
            guiGraphics.fill(cartX, cartY + 16, cartX + cartW, cartY + 17, 0xFFFFFFFF);

            Map<StoreItem, Integer> cartCounts = new HashMap<>();
            for (StoreItem item : shoppingCart) cartCounts.put(item, cartCounts.getOrDefault(item, 0) + 1);

            RenderSystem.enableScissor((int)(cartX * scale), (int)((this.height - cartY - cartH + 25) * scale), (int)(cartW * scale), (int)((cartH - 45) * scale));
            int yOff = 0;
            for (Map.Entry<StoreItem, Integer> entry : cartCounts.entrySet()) {
                StoreItem item = entry.getKey(); int count = entry.getValue();
                int drawY = cartY + 20 + yOff - (int)cartScroll;
                if (drawY > cartY + cartH - 25 || drawY + 10 < cartY + 20) { yOff += 12; continue; }
                boolean hoverRemove = mouseX >= cartX && mouseX <= cartX + cartW && mouseY >= drawY && mouseY < drawY + 12;
                if (hoverRemove) guiGraphics.fill(cartX + 1, drawY, cartX + cartW - 1, drawY + 12, 0xFF550000);
                guiGraphics.drawString(this.font, item.name + " x" + count, cartX + 5, drawY + 2, 0xFFFFFF, false);
                guiGraphics.drawString(this.font, (item.price * count) + " pts", cartX + cartW - 40, drawY + 2, 0x00FFFF, false);
                yOff += 12;
            }
            RenderSystem.disableScissor();

            guiGraphics.fill(cartX, cartY + cartH - 20, cartX + cartW, cartY + cartH - 19, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, totalPrice + " pts", cartX + 5, cartY + cartH - 13, 0x00FFFF, false);

            int buyBtnX = cartX + cartW - 50; int buyBtnY = cartY + cartH - 16;
            boolean canBuy = totalPrice > 0 && POINTS >= totalPrice;
            boolean hoverBuy = mouseX >= buyBtnX && mouseX <= buyBtnX + 45 && mouseY >= buyBtnY && mouseY <= buyBtnY + 12;
            int buyColor = canBuy ? (hoverBuy ? 0xFF00FF00 : 0xFFFFAA00) : 0xFF555555;

            guiGraphics.fill(buyBtnX - 1, buyBtnY - 1, buyBtnX + 46, buyBtnY + 13, buyColor);
            guiGraphics.fill(buyBtnX, buyBtnY, buyBtnX + 45, buyBtnY + 12, 0xFF000000);
            guiGraphics.drawString(this.font, "BUY", buyBtnX + 13, buyBtnY + 2, buyColor, false);
        }
        else if (activeTab == 2) {
            // e-mail
            int leftX = startX + 10; int leftY = startY + 35; int leftW = 120; int leftH = 195;
            int rightX = startX + 140; int rightY = startY + 35; int rightW = 250; int rightH = 195;

            guiGraphics.fill(leftX - 1, leftY - 1, leftX + leftW + 1, leftY + leftH + 1, 0xFFFFFFFF);
            guiGraphics.fill(leftX, leftY, leftX + leftW, leftY + leftH, 0xFF000000);

            double scale = this.minecraft.getWindow().getGuiScale();
            RenderSystem.enableScissor((int)(leftX * scale), (int)((this.height - leftY - leftH) * scale), (int)(leftW * scale), (int)(leftH * scale));
            for (int i = 0; i < EMAILS.size(); i++) {
                net.votmdevs.voicesofthemines.world.PlayerData.Email e = EMAILS.get(i);
                int emailY = leftY + (i * 30) - (int)emailListScroll;

                if (emailY > leftY + leftH || emailY + 30 < leftY) continue;

                boolean isHovered = mouseX >= leftX && mouseX <= leftX + leftW && mouseY >= emailY && mouseY <= emailY + 28;
                boolean isSelected = selectedEmailIndex == i;

                guiGraphics.fill(leftX, emailY, leftX + leftW, emailY + 28, isSelected ? 0xFF333333 : (isHovered ? 0xFF1A1A1A : 0xFF000000));

                boolean hoverDelete = mouseX >= leftX + 2 && mouseX <= leftX + 26 && mouseY >= emailY + 2 && mouseY <= emailY + 26;
                RenderSystem.setShaderColor(hoverDelete ? 1.0F : 0.7F, hoverDelete ? 0.5F : 0.7F, hoverDelete ? 0.5F : 0.7F, 1.0F);
                guiGraphics.blit(DELETE_TEX, leftX + 2, emailY + 2, 0, 0, 24, 24, 24, 24);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                guiGraphics.drawString(this.font, e.sender, leftX + 30, emailY + 4, 0x55FF55, false);
                guiGraphics.drawString(this.font, e.title, leftX + 30, emailY + 16, e.isRead ? 0x888888 : 0xFFFFFF, false);
                guiGraphics.fill(leftX, emailY + 29, leftX + leftW, emailY + 30, 0xFF333333); // Линия
            }
            RenderSystem.disableScissor();

            guiGraphics.fill(rightX - 1, rightY - 1, rightX + rightW + 1, rightY + rightH + 1, 0xFFFFFFFF);
            guiGraphics.fill(rightX, rightY, rightX + rightW, rightY + rightH, 0xFF000000);

            if (selectedEmailIndex >= 0 && selectedEmailIndex < EMAILS.size()) {
                net.votmdevs.voicesofthemines.world.PlayerData.Email e = EMAILS.get(selectedEmailIndex);

                guiGraphics.blit(EMAIL_USER_TEX, rightX + 5, rightY + 3, 0, 0, 24, 24, 24, 24);
                guiGraphics.drawString(this.font, e.sender, rightX + 35, rightY + 10, 0xFFFFAA00, false);
                guiGraphics.fill(rightX, rightY + 32, rightX + rightW, rightY + 33, 0xFFFFFFFF); // Линия

                guiGraphics.drawString(this.font, e.title, rightX + 5, rightY + 38, 0x55FF55, false);
                guiGraphics.fill(rightX, rightY + 52, rightX + rightW, rightY + 53, 0xFFFFFFFF); // Линия

                RenderSystem.enableScissor((int)(rightX * scale), (int)((this.height - rightY - rightH) * scale), (int)(rightW * scale), (int)((rightH - 55) * scale));
                int textDrawY = rightY + 58 - (int)emailTextScroll;
                guiGraphics.drawWordWrap(this.font, Component.literal(e.text), rightX + 5, textDrawY, rightW - 10, 0xFFFFFF);
                RenderSystem.disableScissor();
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isLoading) return false;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startX = centerX - 200;
        int startY = centerY - 120;

        if (activeTab == 1) {
            int listX = startX + 20;
            int listY = startY + 55;
            int listW = 170;
            int listH = 160;

            int cartX = startX + 200;
            int cartY = startY + 35;
            int cartW = 180;
            int cartH = 180;

            // scroll
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                storeScroll -= delta * 15f;
                float maxScroll = Math.max(0, (filteredItems.size() * 24) - listH);
                if (storeScroll < 0) storeScroll = 0;
                if (storeScroll > maxScroll) storeScroll = maxScroll;
                return true;
            }

            if (mouseX >= cartX && mouseX <= cartX + cartW && mouseY >= cartY + 20 && mouseY <= cartY + cartH - 20) {
                cartScroll -= delta * 15f;
                long uniqueItems = shoppingCart.stream().distinct().count();
                float maxScroll = Math.max(0, (uniqueItems * 12) - (cartH - 45));
                if (cartScroll < 0) cartScroll = 0;
                if (cartScroll > maxScroll) cartScroll = maxScroll;
                return true;
            }
        } else if (activeTab == 2) {
            int leftX = startX + 10; int leftY = startY + 35; int leftW = 120; int leftH = 195;
            int rightX = startX + 140; int rightY = startY + 35; int rightW = 250; int rightH = 195;

            if (mouseX >= leftX && mouseX <= leftX + leftW && mouseY >= leftY && mouseY <= leftY + leftH) {
                emailListScroll -= delta * 15f;
                float maxScroll = Math.max(0, (EMAILS.size() * 30) - leftH);
                if (emailListScroll < 0) emailListScroll = 0;
                if (emailListScroll > maxScroll) emailListScroll = maxScroll;
                return true;
            }
            if (mouseX >= rightX && mouseX <= rightX + rightW && mouseY >= rightY + 55 && mouseY <= rightY + rightH) {
                if (selectedEmailIndex >= 0 && selectedEmailIndex < EMAILS.size()) {
                    emailTextScroll -= delta * 15f;
                    int lines = this.font.split(Component.literal(EMAILS.get(selectedEmailIndex).text), rightW - 10).size();
                    float maxScroll = Math.max(0, (lines * this.font.lineHeight) - (rightH - 55));
                    if (emailTextScroll < 0) emailTextScroll = 0;
                    if (emailTextScroll > maxScroll) emailTextScroll = maxScroll;
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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
        } else if (activeTab == 1) {
            int listX = startX + 20;
            int listY = startY + 55;
            int listW = 170;
            int listH = 160;

            int cartX = startX + 200;
            int cartY = startY + 35;
            int cartW = 180;
            int cartH = 180;

            // items in store
            if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
                for (int i = 0; i < filteredItems.size(); i++) {
                    int itemY = listY + (i * 24) - (int)storeScroll;
                    if (itemY > listY + listH || itemY + 24 < listY) continue;

                    if (mouseY >= itemY && mouseY <= itemY + 22) {
                        StoreItem item = filteredItems.get(i);
                        int totalSize = shoppingCart.stream().mapToInt(it -> it.size).sum();
                        if (totalSize + item.size <= 50) {
                            shoppingCart.add(item);
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.5F));
                        } else {
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUG_ALERT.get(), 1.0F, 0.5F));
                        }
                        return true;
                    }
                }
            }

            // shopping cart
            if (mouseX >= cartX && mouseX <= cartX + cartW && mouseY >= cartY + 20 && mouseY <= cartY + cartH - 20) {
                Map<StoreItem, Integer> cartCounts = new HashMap<>();
                List<StoreItem> uniqueItems = new ArrayList<>();
                for (StoreItem item : shoppingCart) {
                    if (!cartCounts.containsKey(item)) uniqueItems.add(item);
                    cartCounts.put(item, cartCounts.getOrDefault(item, 0) + 1);
                }

                int yOff = 0;
                for (StoreItem item : uniqueItems) {
                    int drawY = cartY + 20 + yOff - (int)cartScroll;
                    if (drawY > cartY + cartH - 25 || drawY + 10 < cartY + 20) { yOff += 12; continue; }

                    if (mouseY >= drawY && mouseY < drawY + 12) {
                        shoppingCart.remove(item); // Удаляем один экземпляр
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 0.8F));
                        return true;
                    }
                    yOff += 12;
                }
            }

 // buy button
            int buyBtnX = cartX + cartW - 50;
            int buyBtnY = cartY + cartH - 16;
            if (mouseX >= buyBtnX && mouseX <= buyBtnX + 45 && mouseY >= buyBtnY && mouseY <= buyBtnY + 12) {
                int totalPrice = shoppingCart.stream().mapToInt(i -> i.price).sum();
                if (totalPrice > 0 && POINTS >= totalPrice) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));

                    // what you buy
                    java.util.List<String> itemIds = new java.util.ArrayList<>();
                    for (StoreItem item : shoppingCart) {
                        itemIds.add(item.id);
                    }
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.BuyStorePacket(totalPrice, itemIds)
                    );

                    shoppingCart.clear();
                } else {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUG_ALERT.get(), 1.0F, 0.5F));
                }
                return true;
            }
        } else if (activeTab == 2) {
            int leftX = startX + 10; int leftY = startY + 35; int leftW = 120; int leftH = 195;

            if (mouseX >= leftX && mouseX <= leftX + leftW && mouseY >= leftY && mouseY <= leftY + leftH) {
                for (int i = 0; i < EMAILS.size(); i++) {
                    int emailY = leftY + (i * 30) - (int)emailListScroll;
                    if (emailY > leftY + leftH || emailY + 30 < leftY) continue;

                    if (mouseX >= leftX + 2 && mouseX <= leftX + 26 && mouseY >= emailY + 2 && mouseY <= emailY + 26) {
                        net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.DeleteEmailPacket(i));
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 0.8F));
                        if (selectedEmailIndex == i) selectedEmailIndex = -1;
                        else if (selectedEmailIndex > i) selectedEmailIndex--;
                        return true;
                    }

                    if (mouseY >= emailY && mouseY <= emailY + 28) {
                        selectedEmailIndex = i;
                        emailTextScroll = 0f;
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F, 1.2F));

                        if (!EMAILS.get(i).isRead) {
                            EMAILS.get(i).isRead = true;
                            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.ReadEmailPacket(i));
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
}