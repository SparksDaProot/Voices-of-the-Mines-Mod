package net.votmdevs.voicesofthemines.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class SendEmailScreen extends Screen {
    private EditBox toBox, fromBox, topicBox, textBox;

    public SendEmailScreen() { super(Component.literal("Send Email")); }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        toBox = new EditBox(this.font, centerX - 90, centerY - 60, 180, 15, Component.literal("To"));
        fromBox = new EditBox(this.font, centerX - 90, centerY - 30, 180, 15, Component.literal("From"));
        topicBox = new EditBox(this.font, centerX - 90, centerY, 180, 15, Component.literal("Topic"));
        textBox = new EditBox(this.font, centerX - 90, centerY + 30, 180, 15, Component.literal("Message"));
        textBox.setMaxLength(500); // Сообщение может быть длинным

        this.addRenderableWidget(toBox);
        this.addRenderableWidget(fromBox);
        this.addRenderableWidget(topicBox);
        this.addRenderableWidget(textBox);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.fill(centerX - 100, centerY - 80, centerX + 100, centerY + 80, 0xFFFFFFFF);
        guiGraphics.fill(centerX - 99, centerY - 79, centerX + 99, centerY + 79, 0xFF000000);

        guiGraphics.drawString(this.font, "To (Username):", centerX - 90, centerY - 72, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "From:", centerX - 90, centerY - 42, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Topic:", centerX - 90, centerY - 12, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Message:", centerX - 90, centerY + 18, 0xFFFFFF, false);

        int btnX = centerX - 25; int btnY = centerY + 55;
        boolean hover = mouseX >= btnX && mouseX <= btnX + 50 && mouseY >= btnY && mouseY <= btnY + 15;

        guiGraphics.fill(btnX - 1, btnY - 1, btnX + 51, btnY + 16, hover ? 0xFFFFAA00 : 0xFF553300);
        guiGraphics.fill(btnX, btnY, btnX + 50, btnY + 15, 0xFF000000);
        guiGraphics.drawString(this.font, "SEND", btnX + 13, btnY + 4, 0xFFFFAA00, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int btnX = centerX - 25; int btnY = centerY + 55;

        if (mouseX >= btnX && mouseX <= btnX + 50 && mouseY >= btnY && mouseY <= btnY + 15) {
            String to = toBox.getValue().trim();
            if (!to.isEmpty()) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F));
                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SendEmailPacket(to, fromBox.getValue(), topicBox.getValue(), textBox.getValue())
                );
                this.minecraft.setScreen(null); // Закрываем
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}