package net.votmdevs.voicesofthemines.block;

import net.minecraft.util.StringRepresentable;

public enum KerfurColor implements StringRepresentable {
    NONE("none"),
    BLACK("black"),
    BLUE("blue"),
    GREEN("green"),
    PINK("pink"),
    RED("red"),
    WHITE("white"),
    YELLOW("yellow");

    private final String name;

    KerfurColor(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}