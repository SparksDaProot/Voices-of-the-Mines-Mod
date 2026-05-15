package net.votmdevs.voicesofthemines.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TapeRecorderScreen extends Screen {
    private final List<RecorderData> recorders = new ArrayList<>();
    private float scrollOffset = 0;
    private final BlockPos recorderPos;

    private MultiLineEditBox messageBox;
    private EditBox renameBox;

    private RecorderData selectedRecorder = null;
    private RecorderData renamingRecorder = null;

    public TapeRecorderScreen(String rawData, BlockPos pos) {
        super(Component.literal("Tape Recorder System"));
        this.recorderPos = pos;

        // Парсим строку X,Y,Z|Name|Dist|Closed;
        if (!rawData.isEmpty()) {
            for (String entry : rawData.split(";")) {
                if (entry.isEmpty()) continue;
                String[] parts = entry.split("\\|");
                String[] coords = parts[0].split(",");

                // ИЗМЕНЕНО: Назвали переменную recPos вместо pos
                BlockPos recPos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
                recorders.add(new RecorderData(recPos, parts[1], parts[2], Boolean.parseBoolean(parts[3])));
            }
        }
    }

    // Добавь этот метод в любом месте класса:
    @Override
    public void onClose() {
        KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.TapeGuiClosePacket(this.recorderPos));
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Поле ввода сообщения
        messageBox = new MultiLineEditBox(font, centerX - 100, centerY - 40, 200, 80, Component.literal(""), Component.literal("Enter message..."));
        messageBox.visible = false; // Прячем до выбора
        this.addRenderableWidget(messageBox);

        // Поле переименования
        renameBox = new EditBox(font, centerX - 50, 20, 100, 20, Component.literal(""));
        renameBox.visible = false;
        this.addRenderableWidget(renameBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int listX = 20;
        int listY = 20;
        int listW = 180;
        int listH = height - 40;

        // Фон списка (Черный с белой рамкой)
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF000000);
        graphics.renderOutline(listX - 1, listY - 1, listW + 2, listH + 2, 0xFFFFFFFF);

        // Рендер элементов списка
        int yOffset = listY + 5 - (int)scrollOffset;
        for (RecorderData rec : recorders) {
            if (yOffset > listY && yOffset < listY + listH - 25) { // Видимая зона
                boolean hovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= yOffset && mouseY <= yOffset + 25;

                int color;
                if (hovered) color = 0xFFFFFF55; // Желтый при наведении
                else if (rec.isClosed) color = 0xFFFF5555; // Красный если закрыт
                else color = 0xFF55FFFF; // Голубой

                graphics.drawString(font, rec.name, listX + 5, yOffset, color, false);
                graphics.drawString(font, "Distance: " + rec.distance + "m", listX + 5, yOffset + 12, 0xFFAAAAAA, false);
            }
            yOffset += 30; // Шаг между элементами
        }

        // Рендер выбранного меню
        if (selectedRecorder != null) {
            int mx = width / 2 - 105;
            int my = height / 2 - 45;
            graphics.fill(mx, my, mx + 210, my + 90, 0xFF000000);
            graphics.renderOutline(mx, my, 210, 90, 0xFFFFFFFF);
            graphics.drawString(font, "Sending to: " + selectedRecorder.name, mx + 5, my - 15, 0xFFFFFFFF, true);
            graphics.drawString(font, "Press ENTER to send", mx + 5, my + 95, 0xFFAAAAAA, true);
        }

        if (renamingRecorder != null) {
            graphics.drawString(font, "Rename (Press Enter):", width / 2 - 50, 8, 0xFFFFFFFF, true);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset -= delta * 15;
        if (scrollOffset < 0) scrollOffset = 0;
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = 20;
        int listY = 20;
        int listW = 180;
        int listH = height - 40;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int clickedIndex = (int) ((mouseY - listY + scrollOffset) / 30);
            if (clickedIndex >= 0 && clickedIndex < recorders.size()) {
                RecorderData clicked = recorders.get(clickedIndex);

                // Shift + Клик = Переименовать. Обычный клик = Открыть чат
                if (Screen.hasShiftDown()) {
                    renamingRecorder = clicked;
                    renameBox.setValue(clicked.name);
                    renameBox.visible = true;
                    renameBox.setFocused(true);

                    selectedRecorder = null;
                    messageBox.visible = false;
                } else {
                    selectedRecorder = clicked;
                    messageBox.visible = true;
                    messageBox.setFocused(true);

                    renamingRecorder = null;
                    renameBox.visible = false;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (selectedRecorder != null && messageBox.isFocused() && !messageBox.getValue().isEmpty()) {
                // ОТПРАВЛЯЕМ СООБЩЕНИЕ
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.TapeActionPacket(selectedRecorder.pos, messageBox.getValue(), false));
                this.onClose(); // Закрываем GUI
                return true;
            }
            if (renamingRecorder != null && renameBox.isFocused() && !renameBox.getValue().isEmpty()) {
                // ПЕРЕИМЕНОВЫВАЕМ
                KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.TapeActionPacket(renamingRecorder.pos, renameBox.getValue(), true));
                renamingRecorder.name = renameBox.getValue(); // Локальное обновление
                renamingRecorder = null;
                renameBox.visible = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // Класс-холдер для данных
    private static class RecorderData {
        BlockPos pos; String name; String distance; boolean isClosed;
        public RecorderData(BlockPos pos, String name, String distance, boolean isClosed) {
            this.pos = pos; this.name = name; this.distance = distance; this.isClosed = isClosed;
        }
    }
}