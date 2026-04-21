package net.votmdevs.voicesofthemines.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class SellItemScreen extends Screen {
    private final ItemStack stack;
    private EditBox priceBox;

    public SellItemScreen(ItemStack stack) {
        super(Component.literal("Sell Item"));
        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        priceBox = new EditBox(this.font, centerX - 50, centerY, 100, 15, Component.literal("Price"));
        priceBox.setValue("10");
        this.addRenderableWidget(priceBox);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.fill(centerX - 80, centerY - 40, centerX + 80, centerY + 40, 0xFFFFFFFF);
        guiGraphics.fill(centerX - 79, centerY - 39, centerX + 79, centerY + 39, 0xFF000000);

        guiGraphics.renderItem(stack, centerX - 70, centerY - 30);
        guiGraphics.drawString(this.font, stack.getHoverName(), centerX - 45, centerY - 30, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Size: " + stack.getCount(), centerX - 45, centerY - 18, 0x00FFFF, false);

        int btnX = centerX - 25; int btnY = centerY + 20;
        boolean hover = mouseX >= btnX && mouseX <= btnX + 50 && mouseY >= btnY && mouseY <= btnY + 15;

        guiGraphics.fill(btnX - 1, btnY - 1, btnX + 51, btnY + 16, hover ? 0xFFFFAA00 : 0xFF553300);
        guiGraphics.fill(btnX, btnY, btnX + 50, btnY + 15, 0xFF000000);
        guiGraphics.drawString(this.font, "SELL", btnX + 13, btnY + 4, 0xFFFFAA00, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int btnX = centerX - 25; int btnY = centerY + 20;

        if (mouseX >= btnX && mouseX <= btnX + 50 && mouseY >= btnY && mouseY <= btnY + 15) {
            try {
                int price = Integer.parseInt(priceBox.getValue());
                if (price > 0) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.ListCustomItemPacket(price));
                    this.minecraft.setScreen(null); // Закрываем
                }
            } catch (NumberFormatException ignored) {}
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}