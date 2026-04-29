package net.votmdevs.voicesofthemines.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;

import java.util.ArrayList;
import java.util.List;

public class PaperSheetScreen extends Screen {
    private static final ResourceLocation NOTEPAD_TEX = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/paper_sheet_notepad.png");
    private static final ResourceLocation BTN_DONE = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/done_button.png");
    private static final ResourceLocation BTN_PENCIL = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/pencil_button.png");
    private static final ResourceLocation BTN_ERASER = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/eraser_button.png");
    private static final ResourceLocation BTN_REPORT = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/report_button.png");

    private static final ResourceLocation ICON_PENCIL = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/pencil_icon.png");
    private static final ResourceLocation ICON_ERASER = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/eraser_icon.png");

    private final InteractionHand hand;
    private final ItemStack stack;
    private final List<EditBox> textLines = new ArrayList<>();

    private final int bgWidth = 345;
    private final int bgHeight = 492;
    private int leftPos, topPos;

    private int drawMode = 0;
    private boolean isDrawing = false;

    private final byte[][] pixelData = new byte[bgWidth][bgHeight];
    private boolean isReadOnly = false;

    public PaperSheetScreen(InteractionHand hand, ItemStack stack) {
        super(Component.literal("Notepad"));
        this.hand = hand;
        this.stack = stack;
    }

    private float getScale() {
        float s = (this.height * 0.95f) / bgHeight;
        return Math.min(s, 1.0f);
    }

    @Override
    protected void init() {
        super.init();

        this.clearWidgets();
        textLines.clear();

        float scale = getScale();
        int scaledWidth = (int) (this.width / scale);
        int scaledHeight = (int) (this.height / scale);

        this.leftPos = (scaledWidth - this.bgWidth) / 2;
        this.topPos = (scaledHeight - this.bgHeight) / 2;

        int lineHeight = 18;
        int startY = topPos + 40;

        // DATA
        CompoundTag tag = stack.getTag();
        ListTag savedLines = null;

        if (tag != null && tag.getBoolean("Written")) {
            this.isReadOnly = true;
            if (tag.contains("Lines")) savedLines = tag.getList("Lines", 8); // 8 = StringTag
            if (tag.contains("Pixels")) {
                byte[] flatPixels = tag.getByteArray("Pixels");
                if (flatPixels.length == bgWidth * bgHeight) {
                    int index = 0;
                    for (int x = 0; x < bgWidth; x++) {
                        for (int y = 0; y < bgHeight; y++) {
                            pixelData[x][y] = flatPixels[index++];
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 24; i++) {
            EditBox editBox = new EditBox(this.font, leftPos + 30, startY + (i * lineHeight), bgWidth - 60, 12, Component.empty());
            editBox.setMaxLength(40);
            editBox.setBordered(false);
            editBox.setTextColor(0xFFEEEEEE);

            if (i < 2 || isReadOnly) {
                editBox.setEditable(false);
                editBox.setCanLoseFocus(false);
            }

            if (savedLines != null && i < savedLines.size()) {
                editBox.setValue(savedLines.getString(i));
            }

            textLines.add(editBox);
            this.addRenderableWidget(editBox);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        float scale = getScale();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);

        int smX = (int) (mouseX / scale);
        int smY = (int) (mouseY / scale);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(NOTEPAD_TEX, leftPos, topPos, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        for (int x = 0; x < bgWidth; x++) {
            for (int y = 0; y < bgHeight; y++) {
                if (pixelData[x][y] == 1) {
                    guiGraphics.fill(leftPos + x, topPos + y, leftPos + x + 2, topPos + y + 2, 0xFF000000);
                }
            }
        }

        super.render(guiGraphics, smX, smY, partialTick);

        int btnX = leftPos + bgWidth + 5;
        int btnY = topPos + 20;

        renderButton(guiGraphics, BTN_DONE, btnX, btnY, smX, smY, "Done", 24);
        renderButton(guiGraphics, BTN_PENCIL, btnX, btnY + 30, smX, smY, "Pencil", 24);
        renderButton(guiGraphics, BTN_ERASER, btnX, btnY + 60, smX, smY, "Eraser", 24);
        renderButton(guiGraphics, BTN_REPORT, btnX, btnY + 90, smX, smY, "Report", 48);

        if (drawMode == 1) {
            guiGraphics.blit(ICON_PENCIL, smX - 4, smY - 16, 0, 0, 24, 24, 24, 24);
        } else if (drawMode == 2) {
            guiGraphics.blit(ICON_ERASER, smX - 12, smY - 10, 0, 0, 24, 24, 24, 24);
        }

        if (isDrawing && !isReadOnly) {
            int relX = smX - leftPos;
            int relY = smY - topPos;

            if (relX >= 0 && relX < bgWidth && relY >= 0 && relY < bgHeight) {
                int brushSize = 1;
                for (int dx = -brushSize; dx <= brushSize; dx++) {
                    for (int dy = -brushSize; dy <= brushSize; dy++) {
                        int drawX = relX + dx;
                        int drawY = relY + dy;
                        if (drawX >= 0 && drawX < bgWidth && drawY >= 0 && drawY < bgHeight) {
                            pixelData[drawX][drawY] = (byte) (drawMode == 1 ? 1 : 0);
                        }
                    }
                }
            }
        }

        guiGraphics.pose().popPose();
    }

    private void renderButton(GuiGraphics gui, ResourceLocation tex, int x, int y, int mX, int mY, String tooltip, int width) {
        boolean hovered = mX >= x && mX <= x + width && mY >= y && mY <= y + 24;

        if ((hovered) || (tooltip.equals("Pencil") && drawMode == 1) || (tooltip.equals("Eraser") && drawMode == 2)) {
            RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        gui.blit(tex, x, y, 0, 0, width, 24, width, 24);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // filling satellite name
    public void fillReportData(List<String> satellites) {
        if (isReadOnly) return;

        if (satellites == null || satellites.isEmpty()) {
            textLines.get(2).setValue("ERR: NO TASKS FOUND");
            return;
        }

        int currentLine = 2; // 3rd line start
        for (String sat : satellites) {
            if (currentLine < textLines.size()) {
                EditBox box = textLines.get(currentLine);
                box.setValue(sat.toUpperCase() + " : ");
                box.setCursorPosition(box.getValue().length());
                currentLine++;
            }
        }

        // focus write cursor
        if (textLines.size() > 2) {
            textLines.get(2).setFocused(true);
            this.setFocused(textLines.get(2));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = getScale();
        double smX = mouseX / scale;
        double smY = mouseY / scale;

        if (button == 0) {
            int btnX = leftPos + bgWidth + 5;
            int btnY = topPos + 20;

            if (smX >= btnX && smX <= btnX + 24 && smY >= btnY && smY <= btnY + 24) {
                if (!isReadOnly) {
                    isReadOnly = true;
                    drawMode = 0;
                    for (EditBox box : textLines) box.setEditable(false);
                    Minecraft.getInstance().player.playSound(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F);

                    // Server
                    CompoundTag newTag = new CompoundTag();
                    ListTag newLines = new ListTag();
                    for (EditBox box : textLines) {
                        newLines.add(StringTag.valueOf(box.getValue()));
                    }
                    newTag.put("Lines", newLines);

                    byte[] flatPixels = new byte[bgWidth * bgHeight];
                    int index = 0;
                    for (int x = 0; x < bgWidth; x++) {
                        for (int y = 0; y < bgHeight; y++) {
                            flatPixels[index++] = pixelData[x][y];
                        }
                    }
                    newTag.putByteArray("Pixels", flatPixels);

                    net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                            new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.SavePaperSheetPacket(hand, newTag)
                    );

                    this.minecraft.setScreen(null);
                }
                return true;
            }
            else if (smX >= btnX && smX <= btnX + 24 && smY >= btnY + 30 && smY <= btnY + 54 && !isReadOnly) {
                drawMode = (drawMode == 1) ? 0 : 1;
                Minecraft.getInstance().player.playSound(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F);
                return true;
            }
            else if (smX >= btnX && smX <= btnX + 24 && smY >= btnY + 60 && smY <= btnY + 84 && !isReadOnly) {
                drawMode = (drawMode == 2) ? 0 : 2;
                Minecraft.getInstance().player.playSound(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F);
                return true;
            }
            else if (smX >= btnX && smX <= btnX + 48 && smY >= btnY + 90 && smY <= btnY + 114 && !isReadOnly) {
                Minecraft.getInstance().player.playSound(VotmSounds.BUTTON_CLICK.get(), 1.0F, 1.0F);

                textLines.get(2).setValue("FETCHING DATA...");

                net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(
                        new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.RequestReportDataPacket()
                );
                return true;
            }

            if ((drawMode == 1 || drawMode == 2) && !isReadOnly) {
                if (smX >= leftPos && smX <= leftPos + bgWidth && smY >= topPos && smY <= topPos + bgHeight) {
                    isDrawing = true;
                    for (EditBox box : textLines) box.setFocused(false);
                    return true;
                }
            }
        }
        return super.mouseClicked(smX, smY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        float scale = getScale();
        if (button == 0) isDrawing = false;
        return super.mouseReleased(mouseX / scale, mouseY / scale, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = getScale();
        return super.mouseDragged(mouseX / scale, mouseY / scale, button, dragX / scale, dragY / scale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float scale = getScale();
        return super.mouseScrolled(mouseX / scale, mouseY / scale, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}