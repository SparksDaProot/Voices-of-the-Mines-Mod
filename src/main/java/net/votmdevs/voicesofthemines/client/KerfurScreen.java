package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.KerfurMod;
import net.votmdevs.voicesofthemines.inventory.KerfurMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class KerfurScreen extends AbstractContainerScreen<KerfurMenu> {
    private final ResourceLocation GUI_TEXTURE;

    public KerfurScreen(KerfurMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageHeight = menu.isOmega ? 222 : 166;
        this.inventoryLabelY = this.imageHeight - 94; // Чтобы надпись "Inventory" была на правильном месте
        this.GUI_TEXTURE = getTextureForColor(menu.kerfurColor, menu.isOmega);
    }

    private ResourceLocation getTextureForColor(String color, boolean isOmega) {
        if (isOmega) {
            if (color.equals("blue") || color.equals("none")) return new ResourceLocation(KerfurMod.MODID, "textures/gui/omega_kerfur_storage.png");
            return new ResourceLocation(KerfurMod.MODID, "textures/gui/omega_kerfur_storage_" + color + ".png");
        } else {
            if (color.equals("blue") || color.equals("none")) return new ResourceLocation(KerfurMod.MODID, "textures/gui/kerfur_storage.png");
            if (color.equals("abandoned")) return new ResourceLocation(KerfurMod.MODID, "textures/gui/kerfur_storage_abandoned.png");
            return new ResourceLocation(KerfurMod.MODID, "textures/gui/kerfur_storage_" + color + ".png");
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI_TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}