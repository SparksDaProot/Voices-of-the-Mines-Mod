package net.votmdevs.voicesofthemines;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VotmSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VoicesOfTheMines.MODID);

    public static final RegistryObject<net.minecraft.sounds.SoundEvent> EMAIL_ALERT = registerSoundEvent("email");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> DRONE_AMBIENT = registerSoundEvent("drone");
    public static final RegistryObject<SoundEvent> IDLE = registerSoundEvent("idle");
    public static final RegistryObject<SoundEvent> JUMP = registerSoundEvent("jump");
    public static final RegistryObject<SoundEvent> MEOW = registerSoundEvent("meow");
    public static final RegistryObject<SoundEvent> SHUTDOWN = registerSoundEvent("shutdown");
    public static final RegistryObject<SoundEvent> WALK = registerSoundEvent("walk");
    public static final RegistryObject<SoundEvent> WARNING = registerSoundEvent("warning");
    public static final RegistryObject<SoundEvent> DENY = registerSoundEvent("deny");
    public static final RegistryObject<SoundEvent> CRAFT = registerSoundEvent("craft");
    public static final RegistryObject<SoundEvent> OPEN_STORAGE = registerSoundEvent("open_storage");
    public static final RegistryObject<SoundEvent> FLASHLIGHT = registerSoundEvent("flashlight");
    public static final RegistryObject<SoundEvent> BUBBLE = registerSoundEvent("bubble");
    public static final RegistryObject<SoundEvent> CONCRETE_SCRAPE = registerSoundEvent("concrete_scrape");
    public static final RegistryObject<SoundEvent> PC_STARTUP = registerSoundEvent("pcstartup");
    public static final RegistryObject<SoundEvent> PC_WORKING_LOOP = registerSoundEvent("computerworking_loop");
    public static final RegistryObject<SoundEvent> EVIL_SCREAM = registerSoundEvent("evil_scream");
// <CLEANING> sounds
    public static final RegistryObject<SoundEvent> WATER_SPLASH = registerSoundEvent("water_splash");
    public static final RegistryObject<SoundEvent> SPLAT1 = registerSoundEvent("splat1");
    public static final RegistryObject<SoundEvent> SPLAT2 = registerSoundEvent("splat2");
    public static final RegistryObject<SoundEvent> SPLAT3 = registerSoundEvent("splat3");
    public static final RegistryObject<SoundEvent> SPONGE1 = registerSoundEvent("sponge1");
    public static final RegistryObject<SoundEvent> SPONGE2 = registerSoundEvent("sponge2");
    public static final RegistryObject<SoundEvent> SPONGE3 = registerSoundEvent("sponge3");

    public static final RegistryObject<SoundEvent> DETECTBEEP = registerSoundEvent("detectbeep");

    public static final RegistryObject<SoundEvent> ALARM = registerSoundEvent("alarm");

// GEOMOCT

    public static final RegistryObject<SoundEvent> GEOMOCT_LOOP = registerSoundEvent("geomoct_loop");
    public static final RegistryObject<SoundEvent> OCTCHARGE = registerSoundEvent("octcharge");
    public static final RegistryObject<SoundEvent> OCTSHOOT = registerSoundEvent("octshoot");

    //WISPS
    public static final RegistryObject<SoundEvent> BLACKWISP1 = registerSoundEvent("blackwisp1");
    public static final RegistryObject<SoundEvent> BLACKWISP2 = registerSoundEvent("blackwisp2");
    public static final RegistryObject<SoundEvent> BLACKWISP3 = registerSoundEvent("blackwisp3");
    public static final RegistryObject<SoundEvent> WISPBLACKSCREEN = registerSoundEvent("wispblackscreen");

    public static final RegistryObject<SoundEvent> YELLOW_IDLE = registerSoundEvent("yellow_idle");
    public static final RegistryObject<SoundEvent> PINK_IDLE = registerSoundEvent("pink_idle");

    public static final RegistryObject<SoundEvent> PLUSHBEEP = registerSoundEvent("plushbeep");



    public static final RegistryObject<SoundEvent> LIGHTSWITCH = registerSoundEvent("lightswitch");
    public static final RegistryObject<SoundEvent> CONNECT = registerSoundEvent("connect");

    public static final RegistryObject<SoundEvent> MANNEQUIN_HURT = registerSoundEvent("mannequin_hurt");
    public static final RegistryObject<SoundEvent> MANNEQUIN_ATTACK = registerSoundEvent("mannequin_attack");

    public static final RegistryObject<net.minecraft.sounds.SoundEvent> CLICK1 = registerSoundEvent("click1");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> CLICK2 = registerSoundEvent("click2");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> CLICK3 = registerSoundEvent("click3");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> CLICK4 = registerSoundEvent("click4");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> CLICK5 = registerSoundEvent("click5");

    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER1 = registerSoundEvent("server1");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER2 = registerSoundEvent("server2");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER3 = registerSoundEvent("server3");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER4 = registerSoundEvent("server4");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER5 = registerSoundEvent("server5");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER6 = registerSoundEvent("server6");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER7 = registerSoundEvent("server7");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVER8 = registerSoundEvent("server8");

    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVOMOTOR_LOOP = registerSoundEvent("servomotor_loop");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> SERVERDOWN = registerSoundEvent("serverdown");

    public static final RegistryObject<net.minecraft.sounds.SoundEvent> ALIENMEME = registerSoundEvent("alienmeme");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> DELETEMEME = registerSoundEvent("delete");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> CLARKE = registerSoundEvent("clarke");
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> AMOGUS = registerSoundEvent("amogus");

    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_MARS = registerSoundEvent("signal_planet_mars");
    public static final RegistryObject<SoundEvent> SIGNAL_BDAY = registerSoundEvent("signal_bday");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_VENUS = registerSoundEvent("signal_planet_venus");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_ENCELADUS = registerSoundEvent("signal_planet_enceladus");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_CERES = registerSoundEvent("signal_planet_ceres");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_DIONE = registerSoundEvent("signal_planet_dione");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_BENNU = registerSoundEvent("signal_planet_bennu");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_MERCURY = registerSoundEvent("signal_planet_mercury");

    public static final RegistryObject<SoundEvent> SIGGEN1 = registerSoundEvent("siggen1");
    public static final RegistryObject<SoundEvent> SIGGEN2 = registerSoundEvent("siggen2");
    public static final RegistryObject<SoundEvent> SIGGEN3 = registerSoundEvent("siggen3");
    public static final RegistryObject<SoundEvent> SIGGEN4 = registerSoundEvent("siggen4");
    public static final RegistryObject<SoundEvent> SIGGEN5 = registerSoundEvent("siggen5");
    public static final RegistryObject<SoundEvent> SIGGEN6 = registerSoundEvent("siggen6");

    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_EARTH = registerSoundEvent("signal_planet_earth");
    public static final RegistryObject<SoundEvent> SIGNAL_FACES = registerSoundEvent("signal_faces");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_RETRO = registerSoundEvent("signal_planet_retro");

    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS1 = registerSoundEvent("signal_siggenus1");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS2 = registerSoundEvent("signal_siggenus2");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS3 = registerSoundEvent("signal_siggenus3");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS4 = registerSoundEvent("signal_siggenus4");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS5 = registerSoundEvent("signal_siggenus5");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS6 = registerSoundEvent("signal_siggenus6");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS7 = registerSoundEvent("signal_siggenus7");
    public static final RegistryObject<SoundEvent> SIGNAL_SIGGENUS8 = registerSoundEvent("signal_siggenus8");

    // NEW SIGNALS
    //ASTEROIDs
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID1 = registerSoundEvent("signal_asteroid1");
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID2 = registerSoundEvent("signal_asteroid2");
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID3 = registerSoundEvent("signal_asteroid3");
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID4 = registerSoundEvent("signal_asteroid4");
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID5 = registerSoundEvent("signal_asteroid5");
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID6 = registerSoundEvent("signal_asteroid6");
    public static final RegistryObject<SoundEvent> SIGNAL_ASTEROID7 = registerSoundEvent("signal_asteroid7");

    public static final RegistryObject<SoundEvent> SIGNAL_BLACKHOLE0 = registerSoundEvent("signal_blackhole0");
    public static final RegistryObject<SoundEvent> SIGNAL_EVIL = registerSoundEvent("signal_evil");
    public static final RegistryObject<SoundEvent> SIGNAL_FUNERAL = registerSoundEvent("signal_funeral");
    public static final RegistryObject<SoundEvent> SIGNAL_METTUS = registerSoundEvent("signal_mettus");
    public static final RegistryObject<SoundEvent> SIGNAL_MONTY = registerSoundEvent("signal_monty");
    public static final RegistryObject<SoundEvent> SIGNAL_NEUTRON0 = registerSoundEvent("signal_neutron0");
    public static final RegistryObject<SoundEvent> SIGNAL_NEV = registerSoundEvent("signal_nev");
    public static final RegistryObject<SoundEvent> SIGNAL_NIKO = registerSoundEvent("signal_niko");
    public static final RegistryObject<SoundEvent> SIGNAL_PIZZABREATHER = registerSoundEvent("signal_pizzabreather");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_IO = registerSoundEvent("signal_planet_io");
    public static final RegistryObject<SoundEvent> SIGNAL_ROZ0 = registerSoundEvent("signal_roz0");
    public static final RegistryObject<SoundEvent> SIGNAL_SAT1 = registerSoundEvent("signal_sat1");
    public static final RegistryObject<SoundEvent> SIGNAL_TAMALAN = registerSoundEvent("signal_tamalan");
    public static final RegistryObject<SoundEvent> SIGNAL_TAMALANFLAG = registerSoundEvent("signal_tamalanflag");
    public static final RegistryObject<SoundEvent> SIGNAL_WHITEDWARF = registerSoundEvent("signal_whitedwarf");

    public static final RegistryObject<SoundEvent> SIGNAL_TULPAR = registerSoundEvent("signal_tulpar");
    public static final RegistryObject<SoundEvent> SIGNAL_HATEFULSTAR = registerSoundEvent("signal_hatefulstar");

    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_MAKEMAKE = registerSoundEvent("signal_planet_makemake");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_RHEA = registerSoundEvent("signal_planet_rhea");
    public static final RegistryObject<SoundEvent> SIGNAL_IRIS = registerSoundEvent("signal_iris");
    public static final RegistryObject<SoundEvent> SIGNAL_AMAZUR = registerSoundEvent("signal_amazur");
    public static final RegistryObject<SoundEvent> SIGNAL_VION = registerSoundEvent("signal_vion");
    public static final RegistryObject<SoundEvent> SIGNAL_SUBPLANET = registerSoundEvent("signal_subplanet");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_EUROPA = registerSoundEvent("signal_planet_europa");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_MOON = registerSoundEvent("signal_planet_moon");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_JUPITER = registerSoundEvent("signal_planet_jupiter");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_URANUS = registerSoundEvent("signal_planet_uranus");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_NEPTUNE = registerSoundEvent("signal_planet_neptune");
    public static final RegistryObject<SoundEvent> SIGNAL_PLANET_SATURN = registerSoundEvent("signal_planet_saturn");
    public static final RegistryObject<SoundEvent> SIGNAL_HILERO = registerSoundEvent("signal_hilero");


    public static final RegistryObject<SoundEvent> EXOGEN1 = registerSoundEvent("exogen1");
    public static final RegistryObject<SoundEvent> EXOGEN2 = registerSoundEvent("exogen2");

    public static final RegistryObject<SoundEvent> VOTV_EARTH = registerSoundEvent("votv_earth");
    public static final RegistryObject<SoundEvent> SIGNAL_HAIRY = registerSoundEvent("signal_hairy");

    public static final RegistryObject<SoundEvent> SIGNAL_FARD = registerSoundEvent("signal_fard");
    public static final RegistryObject<SoundEvent> IRONLUNGSIGNAL = registerSoundEvent("ironlungsignal");


    public static final RegistryObject<SoundEvent> RAW1 = registerSoundEvent("raw1");
    public static final RegistryObject<SoundEvent> RAW2 = registerSoundEvent("raw2");
    public static final RegistryObject<SoundEvent> RAW3 = registerSoundEvent("raw3");
    public static final RegistryObject<SoundEvent> RAW4 = registerSoundEvent("raw4");
    public static final RegistryObject<SoundEvent> RAW5 = registerSoundEvent("raw5");
    public static final RegistryObject<SoundEvent> RAW6 = registerSoundEvent("raw6");
    public static final RegistryObject<SoundEvent> RAW7 = registerSoundEvent("raw7");
    public static final RegistryObject<SoundEvent> RAW8 = registerSoundEvent("raw8");

    public static final RegistryObject<SoundEvent> NOISY1 = registerSoundEvent("noisy1");
    public static final RegistryObject<SoundEvent> NOISY2 = registerSoundEvent("noisy2");
    public static final RegistryObject<SoundEvent> NOISY3 = registerSoundEvent("noisy3");
    public static final RegistryObject<SoundEvent> NOISY4 = registerSoundEvent("noisy4");
    public static final RegistryObject<SoundEvent> NOISY5 = registerSoundEvent("noisy5");
    public static final RegistryObject<SoundEvent> NOISY6 = registerSoundEvent("noisy6");
    public static final RegistryObject<SoundEvent> NOISY7 = registerSoundEvent("noisy7");
    public static final RegistryObject<SoundEvent> NOISY8 = registerSoundEvent("noisy8");

    public static final RegistryObject<SoundEvent> LOW1 = registerSoundEvent("low1");
    public static final RegistryObject<SoundEvent> LOW2 = registerSoundEvent("low2");
    public static final RegistryObject<SoundEvent> LOW3 = registerSoundEvent("low3");
    public static final RegistryObject<SoundEvent> LOW4 = registerSoundEvent("low4");
    public static final RegistryObject<SoundEvent> LOW5 = registerSoundEvent("low5");
    public static final RegistryObject<SoundEvent> LOW6 = registerSoundEvent("low6");
    public static final RegistryObject<SoundEvent> LOW7 = registerSoundEvent("low7");
    public static final RegistryObject<SoundEvent> LOW8 = registerSoundEvent("low8");

    // keypad
    public static final RegistryObject<SoundEvent> KEYPAD_PRESS = registerSoundEvent("keypad_press");
    public static final RegistryObject<SoundEvent> KEYPAD_CANCELED = registerSoundEvent("keypad_canceled");
    public static final RegistryObject<SoundEvent> KEYPAD_ACCESS = registerSoundEvent("keypad_access");
    public static final RegistryObject<SoundEvent> KEYPAD_DENIED = registerSoundEvent("keypad_denied");

    public static final RegistryObject<SoundEvent> VOTV_DOOR_SOUND = registerSoundEvent("dooropen1");

    public static final RegistryObject<SoundEvent> BREATH = registerSoundEvent("breath");
    public static final RegistryObject<SoundEvent> BREAKING_BAD = registerSoundEvent("breakingbad");
    public static final RegistryObject<SoundEvent> FALLDEATH = registerSoundEvent("falldeath"); // ДОБАВИЛИ ЗВУК ПАДЕНИЯ
    // maxwell
    public static final RegistryObject<SoundEvent> MAXWELL_THEME = registerSoundEvent("maxwell_theme");
    public static final RegistryObject<SoundEvent> MEOW_1 = registerSoundEvent("meow1");
    public static final RegistryObject<SoundEvent> MEOW_2 = registerSoundEvent("meow2");
    public static final RegistryObject<SoundEvent> DUDUDU = registerSoundEvent("dududu");

    public static final RegistryObject<SoundEvent> DRIVEBOX_OPEN = registerSoundEvent("drivebox_open");
    public static final RegistryObject<SoundEvent> DRIVEBOX_CLOSE = registerSoundEvent("drivebox_close");


    public static final RegistryObject<SoundEvent> MARACAS_1 = registerSoundEvent("maracas1");
    public static final RegistryObject<SoundEvent> MARACAS_2 = registerSoundEvent("maracas2");
    public static final RegistryObject<SoundEvent> MARACAS_3 = registerSoundEvent("maracas3");
    public static final RegistryObject<SoundEvent> MARACAS_4 = registerSoundEvent("maracas4");

    public static final RegistryObject<SoundEvent> OMEGA_SPRINT = registerSoundEvent("omega_sprint");
    public static final RegistryObject<SoundEvent> OMEGA_BONK = registerSoundEvent("omega_bonk");
    public static final RegistryObject<SoundEvent> OMEGA_STEP_DIRT = registerSoundEvent("omega_step_dirt");
    public static final RegistryObject<SoundEvent> OMEGA_STEP_WOOD = registerSoundEvent("omega_step_wood");
    public static final RegistryObject<SoundEvent> OMEGA_STEP_DEFAULT = registerSoundEvent("omega_step_default");

    // fuel can
    public static final RegistryObject<SoundEvent> FUEL_CAN_DROP = registerSoundEvent("fuel_can_drop");
    public static final RegistryObject<SoundEvent> FUEL_POUR = registerSoundEvent("fuel1"); // Назвали как сам файл, можно и fuel_pour

    // ATV
    public static final RegistryObject<SoundEvent> ATV_ON = registerSoundEvent("car_on");
    public static final RegistryObject<SoundEvent> ATV_OFF = registerSoundEvent("car_off");
    public static final RegistryObject<SoundEvent> ATV_BRAKE = registerSoundEvent("car_drive_brake");
    public static final RegistryObject<SoundEvent> ATV_DRIVE_START = registerSoundEvent("car_drive_start");
    public static final RegistryObject<SoundEvent> ATV_DRIVE_LOOP = registerSoundEvent("car_drive_loop");
    public static final RegistryObject<SoundEvent> ATV_CRASH = registerSoundEvent("car_damage");

    // Terminals
    public static final RegistryObject<SoundEvent> BUG_ALERT = registerSoundEvent("bug_alert");
    public static final RegistryObject<SoundEvent> FIND_AMBIENT = registerSoundEvent("find_ambient");
    public static final RegistryObject<SoundEvent> SONAR = registerSoundEvent("sonar");
    public static final RegistryObject<SoundEvent> DETECT_BEEP = registerSoundEvent("detect_beep");
    public static final RegistryObject<SoundEvent> FIND_HELP = registerSoundEvent("find_help");
    public static final RegistryObject<SoundEvent> CALIBRATE_LOOP = registerSoundEvent("calibrate_loop");
    public static final RegistryObject<SoundEvent> TUMBLER = registerSoundEvent("tumbler");
    public static final RegistryObject<SoundEvent> ACHIEVEMENT = registerSoundEvent("achievement");

    public static final RegistryObject<SoundEvent> BUTTON_CLICK = registerSoundEvent("buttonclick");

    public static final RegistryObject<SoundEvent> DRIVE_IN = registerSoundEvent("drive_in");
    public static final RegistryObject<SoundEvent> IMPACT_DRIVE_1 = registerSoundEvent("impact_drive_0001");
    public static final RegistryObject<SoundEvent> IMPACT_DRIVE_2 = registerSoundEvent("impact_drive_0002");
    public static final RegistryObject<SoundEvent> IMPACT_DRIVE_3 = registerSoundEvent("impact_drive_0003");

    // Flesh
    public static final RegistryObject<SoundEvent> FLESH_GRAB = registerSoundEvent("flesh_grab");
    public static final RegistryObject<SoundEvent> FLESH_DROP = registerSoundEvent("flesh_drop");

    public static final RegistryObject<SoundEvent> MURDER_MEOW = registerSoundEvent("murder_meow");
    public static final RegistryObject<SoundEvent> MURDER_RANDOM = registerSoundEvent("murder_random");
    public static final RegistryObject<SoundEvent> PLAYER_COUGH = registerSoundEvent("player_cough");

    public static final RegistryObject<SoundEvent> GARBAGE_GRAB = registerSoundEvent("garbage_grab");
    public static final RegistryObject<SoundEvent> GARBAGE_DROP = registerSoundEvent("garbage_drop");
    public static final RegistryObject<SoundEvent> PACK_GARBAGE = registerSoundEvent("pack_garbage");

    public static final RegistryObject<SoundEvent> COCKROACH_EAT = registerSoundEvent("cockroach_eat");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(VoicesOfTheMines.MODID, name)));
    }
}