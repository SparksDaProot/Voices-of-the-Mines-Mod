package net.votmdevs.voicesofthemines.client.gui;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = VoicesOfTheMines.MODID, value = Dist.CLIENT)
public class GmodNotificationManager {
    private static final ResourceLocation ICON = new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/icons/notification_icon.png");
    private static final List<Notification> notifications = new ArrayList<>();

    public static void addNotification(String message) {
        notifications.add(new Notification(message));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(VotmSounds.BUBBLE.get(), 1.0F));
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        Font font = mc.font;

        long currentTime = System.currentTimeMillis();
        Iterator<Notification> iterator = notifications.iterator();

        int yOffset = screenHeight - 80;

        while (iterator.hasNext()) {
            Notification notif = iterator.next();
            long aliveTime = currentTime - notif.startTime;

            float totalTime = 6000.0F;
            float animTime = 500.0F;

            if (aliveTime > totalTime) {
                iterator.remove();
                continue;
            }

            int textWidth = font.width(notif.message);
            int boxWidth = textWidth + 40;
            int boxHeight = 32;

            float xShift = 0;

            if (aliveTime < animTime) {
                float t = aliveTime / animTime;
                float back = 1.0f + 1.70158f * (float)Math.pow(t - 1, 3) + 1.70158f * (float)Math.pow(t - 1, 2);
                xShift = boxWidth * (1.0f - Math.max(0, back));
            } else if (aliveTime > totalTime - animTime) {
                float t = (aliveTime - (totalTime - animTime)) / animTime;
                xShift = boxWidth * t;
            }

            int x = screenWidth - boxWidth + (int)xShift - 20;
            int y = yOffset;

            graphics.fill(x, y, x + boxWidth, y + boxHeight, 0xCC000000);
            graphics.blit(ICON, x + 6, y + 8, 0, 0, 16, 16, 16, 16);
            graphics.drawString(font, notif.message, x + 28, y + 12, 0xFFFFFF);

            yOffset -= (boxHeight + 5);
        }
    }

    private static class Notification {
        String message;
        long startTime;

        Notification(String message) {
            this.message = message;
            this.startTime = System.currentTimeMillis();
        }
    }
}