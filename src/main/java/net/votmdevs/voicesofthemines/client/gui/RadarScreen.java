package net.votmdevs.voicesofthemines.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class RadarScreen extends Screen {
    private final BlockPos radarPos;
    private float scanRotation = 0f;

    // Радиус сканирования радара (в блоках)
    private final float RADAR_RADIUS = 64.0f;

    // Лог
    private final List<String> logMessages = new ArrayList<>();
    private final List<Integer> logColors = new ArrayList<>();

    private int scanTimer = 0;
    private List<EntityData> currentEntities = new ArrayList<>();

    public RadarScreen(BlockPos pos) {
        super(Component.literal("Radar"));
        this.radarPos = pos;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        super.tick();
        scanRotation += 2.5f; // Скорость вращения стрелки
        if (scanRotation >= 360f) scanRotation -= 360f;

        scanTimer++;
        if (scanTimer >= 20) { // Обновляем список мобов раз в секунду
            scanTimer = 0;
            scanSurroundings();
        }
    }

    private void scanSurroundings() {
        currentEntities.clear();
        if (this.minecraft == null || this.minecraft.level == null) return;

        List<Entity> entities = this.minecraft.level.getEntitiesOfClass(Entity.class,
                new net.minecraft.world.phys.AABB(radarPos).inflate(RADAR_RADIUS));

        for (Entity e : entities) {
            if (e == this.minecraft.player) continue; // Не показываем самого себя
            if (!(e instanceof LivingEntity)) continue; // Игнорируем предметы на земле

            double dist = Math.sqrt(e.distanceToSqr(radarPos.getX(), radarPos.getY(), radarPos.getZ()));
            if (dist <= RADAR_RADIUS) {
                int color;
                String type;

                if (e instanceof Enemy) {
                    color = 0xFFFF5555; type = "HOSTILE";
                } else if (e instanceof Player) {
                    color = 0xFF55FFFF; type = "PLAYER";
                } else if (e instanceof NeutralMob) {
                    color = 0xFFFFFF55; type = "NEUTRAL";
                } else if (e instanceof Animal) {
                    color = 0xFF55FF55; type = "PEACEFUL";
                } else {
                    color = 0xFFFFFFFF; type = "UNKNOWN";
                }

                currentEntities.add(new EntityData(e.getX(), e.getZ(), color));

                // Формируем строчку для лога
                String logLine = String.format("%s [%d, %d]", type, (int)e.getX(), (int)e.getZ());
                addLog(logLine, color);
            }
        }

        // ПРОИГРЫВАЕМ ЗВУК, ЕСЛИ РАДАР КОГО-ТО ЗАСЕК
        if (!currentEntities.isEmpty()) {
            // Писк радара, громкость немного снижена (0.6F), чтобы не раздражать при долгом использовании
            this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.votmdevs.voicesofthemines.VotmSounds.RADAR.get(), 1.0F, 0.6F));
        }
    }

    private void addLog(String text, int color) {
        // Проверяем, нет ли уже такой строки, чтобы не спамить
        if (!logMessages.contains(text)) {
            logMessages.add(text);
            logColors.add(color);
            if (logMessages.size() > 15) { // Максимум 15 строк
                logMessages.remove(0);
                logColors.remove(0);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int guiWidth = 400;
        int guiHeight = 240;
        int startX = (this.width - guiWidth) / 2;
        int startY = (this.height - guiHeight) / 2;

        // --- ЛЕВАЯ ЧАСТЬ (РАДАР) ---
        int radarSize = 220;
        int radarX = startX + 10;
        int radarY = startY + 10;
        int radarCenterX = radarX + radarSize / 2;
        int radarCenterY = radarY + radarSize / 2;

        // Черный фон и белая рамка
        guiGraphics.fill(radarX - 2, radarY - 2, radarX + radarSize + 2, radarY + radarSize + 2, 0xFFFFFFFF);
        guiGraphics.fill(radarX, radarY, radarX + radarSize, radarY + radarSize, 0xFF000000);

        // Тёмно-серая сетка
        for (int i = 0; i < radarSize; i += 20) {
            guiGraphics.fill(radarX + i, radarY, radarX + i + 1, radarY + radarSize, 0xFF222222);
            guiGraphics.fill(radarX, radarY + i, radarX + radarSize, radarY + i + 1, 0xFF222222);
        }

        // Центральный белый квадрат
        guiGraphics.fill(radarCenterX - 2, radarCenterY - 2, radarCenterX + 2, radarCenterY + 2, 0xFFFFFFFF);

        // Отрисовка точек сущностей
        for (EntityData ed : currentEntities) {
            // Конвертируем мировые координаты в координаты радара
            double relX = ed.x - radarPos.getX();
            double relZ = ed.z - radarPos.getZ();

            // Масштабируем: радиус радара (64) = половина экрана радара (110)
            int screenX = radarCenterX + (int)((relX / RADAR_RADIUS) * (radarSize / 2));
            int screenY = radarCenterY + (int)((relZ / RADAR_RADIUS) * (radarSize / 2));

            // Рисуем квадрат 3x3 нужного цвета
            guiGraphics.fill(screenX - 1, screenY - 1, screenX + 2, screenY + 2, ed.color);
        }

        // Вращающаяся линия сканирования (через PoseStack)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(radarCenterX, radarCenterY, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(scanRotation));

        // Рисуем полупрозрачный зелёный луч радара
        RenderSystem.enableBlend();
        guiGraphics.fill(0, 0, 2, radarSize / 2, 0xFFFFFFFF);
        RenderSystem.disableBlend();

        guiGraphics.pose().popPose();

        // --- ПРАВАЯ ЧАСТЬ (ЛОГ) ---
        int logX = radarX + radarSize + 15;
        int logY = startY + 10;
        int logWidth = guiWidth - radarSize - 35;
        int logHeight = radarSize;

        // Фон лога
        guiGraphics.fill(logX - 2, logY - 2, logX + logWidth + 2, logY + logHeight + 2, 0xFFFFFFFF);
        guiGraphics.fill(logX, logY, logX + logWidth, logY + logHeight, 0xFF000000);

        guiGraphics.drawString(this.font, "RADAR LOG", logX + 5, logY + 5, 0xFFFFFF, false);
        guiGraphics.fill(logX, logY + 15, logX + logWidth, logY + 16, 0xFFFFFFFF);

        // Отрисовка строчек лога снизу вверх
        int textY = logY + logHeight - 15;
        for (int i = logMessages.size() - 1; i >= 0; i--) {
            if (textY < logY + 20) break; // Не вылезаем за рамки

            // Рисуем цветной квадратик и текст
            guiGraphics.fill(logX + 5, textY + 1, logX + 10, textY + 6, logColors.get(i));
            guiGraphics.drawString(this.font, logMessages.get(i), logX + 15, textY, logColors.get(i), false);
            textY -= 12;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // Вспомогательный класс для хранения данных сущностей
    private static class EntityData {
        double x, z;
        int color;
        public EntityData(double x, double z, int color) {
            this.x = x; this.z = z; this.color = color;
        }
    }
}