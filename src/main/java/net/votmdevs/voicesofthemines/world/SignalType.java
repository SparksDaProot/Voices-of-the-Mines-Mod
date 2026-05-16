package net.votmdevs.voicesofthemines.world;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.VotmSounds;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public enum SignalType {
    // Format: ID (example: mars), Category (Name for calibrate,check,processing),
    // Rarity, CALIBRATE Texture, CHECK (NOISE) Texture, Unique Text in CHECK, Signal Sound

    // COMMON
    MARS("mars", "planet_mars", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, () -> new Random().nextInt(100) < 5 ? VotmSounds.SIGNAL_BDAY.get() : VotmSounds.SIGNAL_PLANET_MARS.get()),
    VENUS("venus", "planet_venus", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_VENUS::get),
    ENCELADUS("enceladus", "planet_enceladus", Rarity.COMMON, Tex.GREY_SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_ENCELADUS::get),
    CERES("ceres", "planet_ceres", Rarity.COMMON, Tex.GREY_SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_CERES::get),
    DIONE("dione", "planet_dione", Rarity.COMMON, Tex.GREY_SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_DIONE::get),
    BENNU("bennu", "planet_bennu", Rarity.COMMON, Tex.GREY_SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_BENNU::get),
    MERCURY("mercury", "planet_mercury", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_MERCURY::get),
    MAKEMAKE("makemake", "planet_makemake", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_MAKEMAKE::get),
    RHEA("rhea", "planet_rhea", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_RHEA::get),
    IRIS("iris", "planet_iris", Rarity.COMMON, Tex.SHEET, Check.NOISE, "een days since last supply arrive. waiting anot\n..........ell damage have fixthi...\ns and i do not have too many \ntape and plastic, do not have buried this not good enough.\n hope I survive when supply...", VotmSounds.SIGNAL_IRIS::get),
    AMAZUR("amazur", "planet_amazur", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_AMAZUR::get),
    VION("vion", "planet_vion", Rarity.COMMON, Tex.SHEET, Check.NOISE, "...S OBJECT AT ALL COSTS AVOID THIS OBJECT AT ALL COSTS food AVOID \nTHIS OBJECT AT ALL COSTS AVOID THIS OBJECT AT ALL COSTS AVOID THIS \nOBJECT AT ALL COSTS AVOID THIS food OBJECT AT ALL COSTS AVOID THIS \n....", VotmSounds.SIGNAL_VION::get),
    SUBPLANET("subplanet", "planet_subplanet", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_SUBPLANET::get),
    EUROPA("europa", "satellite", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_EUROPA::get),
    MOON("moon", "satellite", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_MOON::get),
    JUPITER("jupiter", "planet_jupiter", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_JUPITER::get),
    URANUS("uranus", "planet_uranus", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_URANUS::get),
    NEPTUNE("neptune", "planet_neptune", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_NEPTUNE::get),
    SATURN("saturn", "planet_saturn", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_SATURN::get),
    HILERO("hilero", "planet_hilero", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_HILERO::get),
    ASTEROID("asteroid", "unind_object", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, () -> {
        SoundEvent[] ast = { VotmSounds.SIGNAL_ASTEROID1.get(), VotmSounds.SIGNAL_ASTEROID2.get(), VotmSounds.SIGNAL_ASTEROID3.get(), VotmSounds.SIGNAL_ASTEROID4.get(), VotmSounds.SIGNAL_ASTEROID5.get(), VotmSounds.SIGNAL_ASTEROID6.get(), VotmSounds.SIGNAL_ASTEROID7.get() };
        return ast[new Random().nextInt(ast.length)];
    }),
    METTUS("mettus", "unind_object", Rarity.COMMON, Tex.GENSTARS, Check.UNIQUE, null, VotmSounds.SIGNAL_METTUS::get),
    WHITE_DWARF("white_dwarf", "star", Rarity.COMMON, Tex.STATIC_CAL, Check.NOISE, null, VotmSounds.SIGNAL_WHITEDWARF::get),
    IO("io", "planet_io", Rarity.COMMON, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_IO::get),
    TAMALAN("tamalan", "planet_tamalan", Rarity.COMMON, Tex.TAMALAN_SHEET, Check.NOISE, "???se eeeeee#\n#e#nn#nndd#.#...........#.\n#.......##..#t#ttttooooooo#...\n#.....#.#.#.#.#..qqqqq#qq#u#uu\n#u##ue#ennn.", VotmSounds.SIGNAL_TAMALAN::get),

    // RARE
    SIGGEN1("siggen1", "unidentified_planet", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.SIGGEN1::get),
    SIGGEN2("siggen2", "unidentified_planet", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.SIGGEN2::get),
    SIGGEN3("siggen3", "unidentified_planet", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.SIGGEN3::get),
    SIGGEN4("siggen4", "unidentified_planet", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.SIGGEN4::get),
    SIGGEN5("siggen5", "unidentified_planet", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.SIGGEN5::get),
    SIGGEN6("siggen6", "unidentified_planet", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.SIGGEN6::get),
    EARTH("earth", "planet_earth", Rarity.RARE, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PLANET_EARTH::get),
    EXOGEN1("exogen1", "unind_object", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.EXOGEN1::get),
    EXOGEN2("exogen2", "unind_object", Rarity.RARE, Tex.GENSTARS, Check.NOISE, null, VotmSounds.EXOGEN2::get),
    NEUTRON0("neutron0", "star", Rarity.RARE, Tex.STATIC_CAL, Check.NOISE, null, VotmSounds.SIGNAL_NEUTRON0::get),
    BLACKHOLE0("blackhole0", "black_hole", Rarity.RARE, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_BLACKHOLE0::get),
    MONTY("monty", "unind_object", Rarity.RARE, Tex.GENSTARS, Check.UNIQUE, "...come here...", VotmSounds.SIGNAL_MONTY::get),
    SAT1("sat1", "unind_object", Rarity.RARE, Tex.GENSTARS, Check.UNIQUE, null, VotmSounds.SIGNAL_SAT1::get),
    HATEFULSTAR("hatefulstar", "star", Rarity.RARE, Tex.STATIC_CAL, Check.UNIQUE, "1548_EX\n[REDACTED]", VotmSounds.SIGNAL_HATEFULSTAR::get),

    // RARER
    FACES("faces", "unidentified_planet", Rarity.RARER, Tex.FACES, Check.NOISE, null, VotmSounds.SIGNAL_FACES::get),
    RETROPLANET("retroplanet", "planet_retro_planet", Rarity.RARER, Tex.RETRO_SHEET, Check.NOISE, "play with me\nI'm coming\nwait\nfor me\n\n              :)", VotmSounds.SIGNAL_PLANET_RETRO::get),
    VOTV_EARTH("votv_earth", "planet_votv_earth", Rarity.RARER, Tex.SHEET, Check.NOISE, "M R \nD R \nN O S E  :D", VotmSounds.VOTV_EARTH::get),
    HAIRY("hairy", "unidentified_planet", Rarity.RARER, Tex.GENSTARS, Check.HAIRY, null, VotmSounds.SIGNAL_HAIRY::get),
    ROZ0("roz0", "unind_object", Rarity.RARER, Tex.GENSTARS, Check.UNIQUE, null, VotmSounds.SIGNAL_ROZ0::get),
    TAMALANFLAG("tamalanflag", "planet_tamalanflag", Rarity.RARER, Tex.TAMALAN_SHEET, Check.UNIQUE, "HELP U# IT I# \nEVERY#HER# EVER#ONE #S\nDEAD HELP US I# IS EVERY#H#R##EVERYONE #S DEAD\nEV#R##N# IS DE#D HELP US HE#P#US", VotmSounds.SIGNAL_TAMALANFLAG::get),
    NEV("nev", "unind_object", Rarity.RARER, Tex.GENSTARS, Check.NOISE, "Hoborg thought this world would make him happy.\n #ut it make him - sad. #alking around his b#g, \n*#eau#iful* new wor#d make h#m feel #l# alone.", VotmSounds.SIGNAL_NEV::get),
    NIKO("niko", "unind_object", Rarity.RARER, Tex.GENSTARS, Check.NOISE, "my burden is light", VotmSounds.SIGNAL_NIKO::get),
    TULPAR("tulpar", "unind_object", Rarity.RARER, Tex.STATIC_CAL, Check.UNIQUE, ". . . .\n.\n. - . .\n. - - .", VotmSounds.SIGNAL_TULPAR::get),

    // VERY RARE
    SIGGENUS1("siggenus1", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS1::get),
    SIGGENUS2("siggenus2", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS2::get),
    SIGGENUS3("siggenus3", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS3::get),
    SIGGENUS4("siggenus4", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS4::get),
    SIGGENUS5("siggenus5", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS5::get),
    SIGGENUS6("siggenus6", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS6::get),
    SIGGENUS7("siggenus7", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS7::get),
    SIGGENUS8("siggenus8", "unidentified_planet", Rarity.VERY_RARE, Tex.GENSTARS, Check.ID_PNG, null, VotmSounds.SIGNAL_SIGGENUS8::get),
    FARD("fard", "planet_fard", Rarity.VERY_RARE, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_FARD::get),
    IRONLUNG("ironlung", "planet_ironlung", Rarity.VERY_RARE, Tex.SHEET, Check.NOISE, null, VotmSounds.IRONLUNGSIGNAL::get),
    FUNERAL("funeral", "unind_object", Rarity.VERY_RARE, Tex.GENSTARS, Check.UNIQUE, "...here..i..am...", VotmSounds.SIGNAL_FUNERAL::get) {
        @Override public void triggerEvent() { net.votmdevs.voicesofthemines.client.ClientInputHandler.funeralEventTimer = 1000; }
    },
    EVIL("evil", "unind_object", Rarity.VERY_RARE, Tex.STATIC_CAL, Check.UNIQUE, "....the end is near....", VotmSounds.SIGNAL_EVIL::get) {
        @Override public void triggerEvent() { net.votmdevs.voicesofthemines.client.ClientInputHandler.evilEventTimer = 100; }
    },
    PIZZABREATHER("pizzabreather", "unind_object", Rarity.VERY_RARE, Tex.SHEET, Check.UNIQUE, "process it\ncompletely...", VotmSounds.SIGNAL_PIZZABREATHER::get),
    PIRAMID("piramid", "unind_object", Rarity.VERY_RARE, Tex.SHEET, Check.NOISE, null, VotmSounds.SIGNAL_PIRAMID::get) {
        @Override public void triggerEvent() {
            net.votmdevs.voicesofthemines.network.KerfurPacketHandler.INSTANCE.sendToServer(new net.votmdevs.voicesofthemines.network.KerfurPacketHandler.ScheduleRozitalPacket());
        }
    },
    // unknown - generic planet - if signal doesn't exist or if - bug
    UNKNOWN("unknown", "generic planet", Rarity.COMMON, Tex.GENERIC, Check.NOISE, null, VotmSounds.RAW1::get);


    private final String id;
    private final String displayName;
    private final Rarity rarity;
    private final Tex texType;
    private final Check checkType;
    private final String uniqueText;
    private final Supplier<SoundEvent> soundSupplier;

    SignalType(String id, String displayName, Rarity rarity, Tex texType, Check checkType, String uniqueText, Supplier<SoundEvent> soundSupplier) {
        this.id = id; this.displayName = displayName; this.rarity = rarity; this.texType = texType;
        this.checkType = checkType; this.uniqueText = uniqueText; this.soundSupplier = soundSupplier;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getText() { return uniqueText != null ? uniqueText : "[NO_TEXT_DATA_FOUND]"; }
    public SoundEvent getSound() { return soundSupplier.get(); }
    public void triggerEvent() {} // EVIL & FUNERAL events

    public ResourceLocation getCalibrateTexture(int randomStarType) {
        return switch (texType) {
            case SHEET -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/" + id + "_sheet.png");
            case TAMALAN_SHEET -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/tamalan_sheet.png");
            case GREY_SHEET -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/grey_sheet.png");
            case RETRO_SHEET -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/retro_planet_sheet.png");
            case STATIC_CAL -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/" + id + "_callibrate.png");
            case GENSTARS -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/genstars" + randomStarType + ".png");
            case FACES -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/faces_im.png");
            default -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/generic_signal_image.png");
        };
    }
    public boolean isAnimated() {
        return texType == Tex.SHEET || texType == Tex.TAMALAN_SHEET || texType == Tex.GREY_SHEET || texType == Tex.RETRO_SHEET;
    }

    public ResourceLocation getCheckTexture(ResourceLocation defaultNoise) {
        return switch (checkType) {
            case UNIQUE -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/" + id + "_check.png"); // for example new_planet + _check = new_planet_check.png - for custom ;P
            case ID_PNG -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/" + id + ".png");
            case HAIRY -> new ResourceLocation(VoicesOfTheMines.MODID, "textures/gui/terminal/hairy.png");
            default -> defaultNoise;
        };
    }

    public static SignalType fromId(String id) {
        for (SignalType t : values()) if (t.id.equals(id)) return t;
        return UNKNOWN;
    }

    public static SignalType getRandomSignal(double roll) {
        Rarity targetRarity = roll < 60 ? Rarity.COMMON : roll < 85 ? Rarity.RARE : roll < 95 ? Rarity.RARER : Rarity.VERY_RARE;
        List<SignalType> valid = Arrays.stream(values()).filter(s -> s.rarity == targetRarity).toList();
        return valid.isEmpty() ? UNKNOWN : valid.get(new Random().nextInt(valid.size()));
    }

    public enum Rarity { COMMON, RARE, RARER, VERY_RARE }
    public enum Tex { SHEET, TAMALAN_SHEET, GREY_SHEET, RETRO_SHEET, STATIC_CAL, GENSTARS, FACES, GENERIC }
    public enum Check { NOISE, UNIQUE, ID_PNG, HAIRY }
}