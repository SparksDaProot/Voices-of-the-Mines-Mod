package net.votmdevs.voicesofthemines.block;

import net.minecraft.util.StringRepresentable;

public enum ServerType implements StringRepresentable {
    BASE("base"), KILO("kilo"), LIMA("lima"), MIKE("mike"), NOVEMBER("november"),
    OSCAR("oscar"), PAPA("papa"), QUEBEC("quebec"), ROMEO("romeo"), TANGO("tango"),
    VICTOR("victor"), ECHO("echo"), XRAY("xray"), YANKEE("yankee"), UNIFORM("uniform"),
    SIERRA("sierra"), WHISKEY("whiskey"), GOLF("golf"), DELTA("delta"), CHARLIE("charlie"),
    BRAVO("bravo"), HOTEL("hotel"), INDIA("india"), JULIETT("juliett"), FOXTROT("foxtrot");

    private final String name;

    ServerType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}