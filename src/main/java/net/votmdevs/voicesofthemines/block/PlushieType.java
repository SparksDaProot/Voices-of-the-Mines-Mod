package net.votmdevs.voicesofthemines.block;

import net.minecraft.ChatFormatting;
//TYPES
public enum PlushieType {
    BENJIKUS("benjikus", "plushie_benjikus", "beep_animation", "beep", "Silly alien :P", ChatFormatting.GREEN),
    BENJIKUS_COMMON("benjikuscommon", "plushie_benjikuscommon", "beep_animation", "beep", "Benj1kus", ChatFormatting.WHITE),
    PECORA("pecora", "plushie_pecora", "beep_animation", "beep", "Pecora", ChatFormatting.YELLOW),
    SPARKSY("sparksy", "plushie_sparksy", "beep_animation", "beep", "Artemis", ChatFormatting.BLUE),
    LIBE("libe", "plushie_libe", "beep_animation", "beep", "Libe", ChatFormatting.DARK_PURPLE),
    NIKO("niko", "plushie_niko", "beep_animation", "beep", "Da Noik", ChatFormatting.LIGHT_PURPLE),
    INVINCIBLE("invinc", "plushie_invincible", "beepinvinc_animation", "beepin", null, null),
    KEL("kel", "plushie_kel", "beep_animation", "beep", null, null);

    private final String modelName;
    private final String textureName;
    private final String animationFile;
    private final String animName;
    private final String secretText;
    private final ChatFormatting textColor;

    PlushieType(String modelName, String textureName, String animationFile, String animName, String secretText, ChatFormatting textColor) {
        this.modelName = modelName;
        this.textureName = textureName;
        this.animationFile = animationFile;
        this.animName = animName;
        this.secretText = secretText;
        this.textColor = textColor;
    }

    public String getModelName() { return modelName; }
    public String getTextureName() { return textureName; }
    public String getAnimationFile() { return animationFile; }
    public String getAnimName() { return animName; }
    public String getSecretText() { return secretText; }
    public ChatFormatting getTextColor() { return textColor; }
}