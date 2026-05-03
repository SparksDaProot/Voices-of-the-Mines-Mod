package net.votmdevs.voicesofthemines.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import javax.sound.sampled.*;
import java.io.File;
import java.util.*;

public class ClientRadioManager {
    private static final Map<BlockPos, RadioInstance> activeRadios = new HashMap<>();
    private static final int MAX_DISTANCE = 30; // Звук радио будет слышно на 30 блоков

    private static class RadioInstance {
        Clip clip;
        String track;
        float baseVolume;

        public RadioInstance(Clip clip, String track, float baseVolume) {
            this.clip = clip;
            this.track = track;
            this.baseVolume = baseVolume;
        }
    }

    public static String getNextTrack(String currentTrack) {
        File dir = new File(Minecraft.getInstance().gameDirectory, "votm_radio");
        if (!dir.exists()) dir.mkdirs(); // create folder

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".wav"));
        if (files == null || files.length == 0) return "";

        List<String> tracks = new ArrayList<>();
        for (File f : files) tracks.add(f.getName());
        Collections.sort(tracks); // alphabet sorting

        if (currentTrack == null || currentTrack.isEmpty() || !tracks.contains(currentTrack)) {
            return tracks.get(0);
        }

        int idx = tracks.indexOf(currentTrack);
        if (idx + 1 < tracks.size()) return tracks.get(idx + 1);
        return tracks.get(0); // start
    }

    public static void handleSync(BlockPos pos, boolean isPlaying, String track, float volume) {
        if (!isPlaying || track.isEmpty()) {
            stopRadio(pos);
            return;
        }

        RadioInstance existing = activeRadios.get(pos);

        if (existing == null || !existing.track.equals(track)) {
            stopRadio(pos);
            playRadio(pos, track, volume);
        } else {
            existing.baseVolume = volume;
        }
    }

    private static void playRadio(BlockPos pos, String track, float volume) {
        File dir = new File(Minecraft.getInstance().gameDirectory, "votm_radio");
        File audioFile = new File(dir, track);

        if (!audioFile.exists()) return;

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

            activeRadios.put(pos, new RadioInstance(clip, track, volume));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stopRadio(BlockPos pos) {
        RadioInstance instance = activeRadios.remove(pos);
        if (instance != null && instance.clip != null) {
            instance.clip.stop();
            instance.clip.close();
        }
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            if (!activeRadios.isEmpty()) {
                activeRadios.values().forEach(r -> {
                    if (r.clip != null) { r.clip.stop(); r.clip.close(); }
                });
                activeRadios.clear();
            }
            return;
        }

        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, RadioInstance> entry : activeRadios.entrySet()) {
            BlockPos pos = entry.getKey();
            RadioInstance instance = entry.getValue();

            double distance = Math.sqrt(mc.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));

            if (distance > MAX_DISTANCE) {
                setClipVolume(instance.clip, 0.0f);
                continue;
            }

            float distanceFactor = (float) (1.0f - (distance / MAX_DISTANCE));
            float finalLinearVolume = instance.baseVolume * distanceFactor;

            setClipVolume(instance.clip, finalLinearVolume);
        }
    }

    private static void setClipVolume(Clip clip, float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            if (volume <= 0.01f) {
                gainControl.setValue(gainControl.getMinimum());
            } else {
                float db = (float) (Math.log10(volume) * 20.0);
                db = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db));
                gainControl.setValue(db);
            }
        }
    }
}