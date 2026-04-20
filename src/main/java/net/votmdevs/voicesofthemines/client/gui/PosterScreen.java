package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.KerfurSounds;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PosterScreen extends Screen {
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(KerfurMod.MODID, "textures/gui/save_poster_button.png");
    private final BlockPos posterPos;
    private final String initialUrl;
    private EditBox urlEditBox;

    public PosterScreen(BlockPos pos, String currentUrl) {
        super(Component.literal("Edit Poster URL"));
        this.posterPos = pos;
        this.initialUrl = currentUrl;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.urlEditBox = new EditBox(this.font, centerX - 150, centerY - 20, 300, 20, Component.literal("Image URL"));
        this.urlEditBox.setMaxLength(500);
        this.urlEditBox.setValue(this.initialUrl);
        this.urlEditBox.setTextColor(0xFFA500);
        this.addRenderableWidget(this.urlEditBox);
        this.setInitialFocus(this.urlEditBox);

        Button saveButton = new Button(Button.builder(Component.empty(), button -> saveUrlAndClose()).bounds(centerX - 16, centerY + 10, 32, 16)) {
            @Override
            public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
                handler.play(SimpleSoundInstance.forUI(KerfurSounds.BUTTON_CLICK.get(), 1.0F));
            }
        };

        this.addRenderableWidget(saveButton);
    }

    private void saveUrlAndClose() {
        String inputUrl = this.urlEditBox.getValue().trim();

        if (!inputUrl.isEmpty() && !inputUrl.startsWith("http")) {
            GmodNotificationManager.addNotification("Failed: Invalid URL format!");
            this.minecraft.setScreen(null);
            return;
        }

        if (!inputUrl.isEmpty()) {
            net.votmdevs.voicesofthemines.client.PosterTextureManager.validateAndApply(inputUrl, this.posterPos);
        } else {
            KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.UpdatePosterUrlPacket(this.posterPos, ""));
        }

        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.drawCenteredString(this.font, "Enter Imgur Direct Link (.png, .jpg):", centerX, centerY - 35, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);

        RenderSystem.setShaderTexture(0, BUTTON_TEXTURE);

        boolean isHovered = mouseX >= centerX - 16 && mouseX <= centerX + 16 && mouseY >= centerY + 10 && mouseY <= centerY + 26;

        if (isHovered) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
        }

        graphics.blit(BUTTON_TEXTURE,
                centerX - 16, centerY + 10,
                0, 0,
                32, 16,
                32, 16
        );
    }
}