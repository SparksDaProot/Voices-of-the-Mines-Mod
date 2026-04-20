package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.client.gui.GmodNotificationManager;
import net.votmdevs.voicesofthemines.network.KerfurPacketHandler;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PosterTextureManager {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> FAILED_URLS = ConcurrentHashMap.newKeySet();

    public static ResourceLocation getTexture(String urlString) {
        if (urlString == null || urlString.isEmpty() || FAILED_URLS.contains(urlString)) {
            return null;
        }
        if (CACHE.containsKey(urlString)) {
            return CACHE.get(urlString);
        }
        FAILED_URLS.add(urlString); // Временно блокируем спам запросами
        downloadTextureAsync(urlString, null, false);
        return null;
    }

    public static void validateAndApply(String urlString, BlockPos pos) {
        if (CACHE.containsKey(urlString)) {
            KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.UpdatePosterUrlPacket(pos, urlString));
            return;
        }
        downloadTextureAsync(urlString, pos, true);
    }

    private static void downloadTextureAsync(String urlString, BlockPos posToUpdate, boolean showNotifications) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                connection.setRequestProperty("Accept", "image/png,image/jpeg,*/*");

                try (InputStream is = connection.getInputStream()) {
                    NativeImage image = NativeImage.read(is);

                    if (image.getWidth() > 40 || image.getHeight() > 40) {
                        if (showNotifications) {
                            Minecraft.getInstance().execute(() -> {
                                GmodNotificationManager.addNotification("Failed: Image size is bigger than 40x40");
                            });
                        }
                        FAILED_URLS.add(urlString);
                        image.close();
                        return;
                    }

                    Minecraft.getInstance().execute(() -> {
                        DynamicTexture texture = new DynamicTexture(image);
                        ResourceLocation rl = new ResourceLocation(VoicesOfTheMines.MODID, "custom_poster_" + Math.abs(urlString.hashCode()));
                        Minecraft.getInstance().getTextureManager().register(rl, texture);

                        CACHE.put(urlString, rl);
                        FAILED_URLS.remove(urlString);

                        if (posToUpdate != null) {
                            KerfurPacketHandler.INSTANCE.sendToServer(new KerfurPacketHandler.UpdatePosterUrlPacket(posToUpdate, urlString));
                        }
                    });
                }
            } catch (Exception e) {
                if (showNotifications) {
                    Minecraft.getInstance().execute(() -> {
                        GmodNotificationManager.addNotification("Failed: Invalid image or connection error!");
                    });
                }
                FAILED_URLS.add(urlString);
                System.out.println("Failed to load poster from " + urlString + ": " + e.getMessage());
            }
        });
    }
}