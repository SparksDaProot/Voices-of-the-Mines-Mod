package net.votmdevs.voicesofthemines.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.votmdevs.voicesofthemines.VotmSounds;

public class AlarmSoundInstance extends AbstractTickableSoundInstance {
    private final BlockPos pos;
    private boolean isStopping = false;

    public AlarmSoundInstance(BlockPos pos) {
        super(VotmSounds.ALARM.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.pos = pos;
        this.looping = true;
        this.delay = 0;
        this.volume = 3.125F;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
        this.relative = false;
    }

    public void fadeOutAndStop() {
        this.isStopping = true;
    }

    @Override
    public void tick() {
        if (this.isStopping) {
            this.volume -= 0.1F;
            if (this.volume <= 0.0F) {
                this.stop();
            }
        }
    }
}