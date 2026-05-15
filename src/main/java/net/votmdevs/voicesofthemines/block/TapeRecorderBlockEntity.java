package net.votmdevs.voicesofthemines.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class TapeRecorderBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final List<TapeRecorderBlockEntity> LOADED_RECORDERS = new ArrayList<>();

    public String customName = "";

    // TTS
    private String ttsQueue = "";
    private int ttsTimer = 0;
    public boolean isBlahBlahMode = false; // ФЛАГ ДЛЯ МЕМНОГО РЕЖИМА

    public boolean isPlaying = false;
    public boolean isRecording = false;
    public boolean isPlayingAmbient = false;

    public String lastMessage = "";
    public int textTimer = 0;

    public List<RecordedSound> ambientRecordings = new ArrayList<>();
    public List<RecordedSound> playbackQueue = new ArrayList<>();
    public int ambientRecordTimer = 0;
    public int ambientPlaybackTimer = 0;

    public TapeRecorderBlockEntity(BlockPos pos, BlockState state) {
        super(VoicesOfTheMines.TAPE_RECORDER_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide()) {
            LOADED_RECORDERS.add(this);
            if (customName.isEmpty()) customName = "TapeRecorder" + this.worldPosition.hashCode();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && !this.level.isClientSide()) LOADED_RECORDERS.remove(this);
    }

    public void syncData() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // RECORDING SOUNDS
    public void startAmbientRecording() {
        this.isRecording = true;
        this.ambientRecordings.clear();
        this.ambientRecordTimer = 0;
        this.syncData();
    }

    public void stopAmbientRecordingAndBroadcast() {
        this.isRecording = false;
        this.syncData();
        if (this.ambientRecordings.isEmpty()) return;

        for (TapeRecorderBlockEntity be : LOADED_RECORDERS) {
            if (be != this && be.getLevel() == this.level && be.getBlockPos().distSqr(this.getBlockPos()) <= 2500) {
                List<RecordedSound> deepCopy = new ArrayList<>();
                for (RecordedSound rs : this.ambientRecordings) {
                    deepCopy.add(new RecordedSound(rs.soundLoc, rs.volume, rs.pitch, rs.delayTicks));
                }
                be.playAmbient(deepCopy);
            }
        }
        this.ambientRecordings.clear();
    }

    public void playAmbient(List<RecordedSound> sounds) {
        this.playbackQueue = sounds;
        this.ambientPlaybackTimer = 0;
        this.isPlayingAmbient = true;
        this.syncData();

        BlockState state = this.getBlockState();
        if (state.hasProperty(TapeRecorderBlock.OPEN) && !state.getValue(TapeRecorderBlock.OPEN)) {
            this.level.setBlock(this.worldPosition, state.setValue(TapeRecorderBlock.OPEN, true), 3);
        }
    }

    public static void recordAmbientSound(Level level, BlockPos soundPos, net.minecraft.sounds.SoundEvent sound, float vol, float pitch) {
        if (sound == null) return;
        ResourceLocation soundLoc = ForgeRegistries.SOUND_EVENTS.getKey(sound);
        if (soundLoc == null) return;

        for (TapeRecorderBlockEntity be : LOADED_RECORDERS) {
            if (be.getLevel() == level && be.isRecording && be.getBlockPos().distSqr(soundPos) <= 400) {
                be.ambientRecordings.add(new RecordedSound(soundLoc, vol, pitch, be.ambientRecordTimer));
            }
        }
    }

    public void playMessage(String message) {
        this.lastMessage = message;
        this.textTimer = message.length() * 4 + 60;

        this.isBlahBlahMode = message.replaceAll("[a-zA-Z0-9 \\p{Punct}]", "").length() > 0;

        if (this.isBlahBlahMode) {
            this.ttsQueue = "";
            this.ttsTimer = 10;
        } else {
            this.ttsQueue = message.toLowerCase().replaceAll("[^a-z ]", "");
            this.ttsTimer = 0;
        }

        this.isPlaying = true;
        syncData();

        BlockState state = this.getBlockState();
        if (state.hasProperty(TapeRecorderBlock.OPEN) && !state.getValue(TapeRecorderBlock.OPEN)) {
            this.level.setBlock(this.worldPosition, state.setValue(TapeRecorderBlock.OPEN, true), 3);
        }
    }

    public void tick() {
        if (this.level == null) return;

        if (this.textTimer > 0) this.textTimer--;

        if (this.level.isClientSide()) return;

        if (isRecording) {
            ambientRecordTimer++;
        }

        if (isPlayingAmbient) {
            ambientPlaybackTimer++;
            boolean allPlayed = true;
            for (RecordedSound sound : playbackQueue) {
                if (!sound.played) {
                    allPlayed = false;
                    if (ambientPlaybackTimer >= sound.delayTicks) {
                        sound.played = true;

                        float loFiPitch = sound.pitch * (0.8F + this.level.random.nextFloat() * 0.3F);
                        float loFiVol = sound.volume * 0.8F;

                        net.minecraft.sounds.SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(sound.soundLoc);
                        if (event != null) {
                            this.level.playSound(null, this.worldPosition, event, SoundSource.RECORDS, loFiVol, loFiPitch);
                        }
                    }
                }
            }
            if (allPlayed) {
                isPlayingAmbient = false;
                playbackQueue.clear();
                syncData();
            }
        }

        // TTS & meme
        if (isPlaying) {

            if (isBlahBlahMode) {
                if (ttsTimer > 0) {
                    ttsTimer--;
                    if (ttsTimer == 0) {
                        this.level.playSound(null, this.worldPosition, VotmSounds.BLAHBLAH.get(), SoundSource.RECORDS, 2.0F, 1.0F);
                    }
                }

                if (this.textTimer <= 0) {
                    isPlaying = false;
                    isBlahBlahMode = false;
                    syncData();
                }
                return;
            }

            // reading
            if (ttsQueue.isEmpty()) {
                isPlaying = false;
                syncData();
                return;
            }

            if (ttsTimer > 0) {
                ttsTimer--;
            } else {
                char c = ttsQueue.charAt(0);
                ttsQueue = ttsQueue.substring(1);

                if (c == ' ') {
                    ttsTimer = 10; // pause between sounds
                } else {
                    var soundObj = VotmSounds.TTS_SOUNDS.get(c);
                    if (soundObj != null) {
                        float pitch = 0.9F + (this.level.random.nextFloat() * 0.2F);
                        this.level.playSound(null, this.worldPosition, soundObj.get(), SoundSource.RECORDS, 2.0F, pitch);
                    }
                    ttsTimer = 2; // reading speed
                }
            }
        }
    }

    // NBT
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) this.handleUpdateTag(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("LastMessage", this.lastMessage != null ? this.lastMessage : "");
        tag.putInt("TextTimer", this.textTimer);
        tag.putBoolean("IsPlaying", this.isPlaying || this.isPlayingAmbient);
        tag.putBoolean("IsRecording", this.isRecording);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.lastMessage = tag.getString("LastMessage");
        this.textTimer = tag.getInt("TextTimer");
        this.isPlaying = tag.getBoolean("IsPlaying");
        this.isRecording = tag.getBoolean("IsRecording");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("CustomName", this.customName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.customName = tag.getString("CustomName");
    }

    // animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, event -> {
            BlockState state = this.getBlockState();
            if (!state.hasProperty(TapeRecorderBlock.OPEN)) return event.setAndContinue(RawAnimation.begin().thenLoop("close_idle"));

            boolean isOpen = state.getValue(TapeRecorderBlock.OPEN);

            if (isOpen) {
                if (isRecording) return event.setAndContinue(RawAnimation.begin().thenLoop("record"));
                if (isPlaying || isPlayingAmbient) return event.setAndContinue(RawAnimation.begin().thenLoop("play"));

                return event.setAndContinue(RawAnimation.begin().thenPlay("open").thenLoop("open_idle"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlay("close").thenLoop("close_idle"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    public static class RecordedSound {
        public ResourceLocation soundLoc;
        public float volume;
        public float pitch;
        public int delayTicks;
        public boolean played = false;

        public RecordedSound(ResourceLocation soundLoc, float volume, float pitch, int delayTicks) {
            this.soundLoc = soundLoc;
            this.volume = volume;
            this.pitch = pitch;
            this.delayTicks = delayTicks;
        }
    }
}