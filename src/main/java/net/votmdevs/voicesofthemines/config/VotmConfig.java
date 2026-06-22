package net.votmdevs.voicesofthemines.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class VotmConfig {

    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.DoubleValue SERVER_BREAK_INTERVAL_MINUTES;
    public static final ForgeConfigSpec.DoubleValue RECENTLY_FIXED_PROTECTION_MINUTES;
    public static final ForgeConfigSpec.BooleanValue DEBUG_SIGNAL_BREAKS;
    public static final ForgeConfigSpec.DoubleValue TERMINAL_PUNCH_DAMAGE;
    public static final ForgeConfigSpec.IntValue COMMON_WEIGHT;
    public static final ForgeConfigSpec.IntValue RARE_WEIGHT;
    public static final ForgeConfigSpec.IntValue RARER_WEIGHT;
    public static final ForgeConfigSpec.IntValue VERY_RARE_WEIGHT;
    public static final ForgeConfigSpec.IntValue TRANSFORMER_BASE_ENERGY;
    public static final ForgeConfigSpec.DoubleValue TRANSFORMER_BASE_DRAIN;
    public static final ForgeConfigSpec.DoubleValue TRANSFORMER_DEVICE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue TRANSFORMER_LOG_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue TRANSFORMER_MIN_TICKS;

    public static final ForgeConfigSpec.IntValue PYRAMID_KILL_QUOTA;

    private static final double TICKS_PER_MINUTE = 20.0D * 60.0D;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "Signal/server settings.",
                "Time values are in real-time minutes at normal 20 TPS.",
                "Decimals are allowed: 0.5 = 30 seconds, 0.1 = 6 seconds.",
                "For timer options, 0 disables that feature."
        );
        builder.push("signals");

        // Signal rare config
        COMMON_WEIGHT = builder
                .comment("Weight for COMMON signals to spawn (higher value = spawns more often). Default: 60")
                .defineInRange("commonSignalWeight", 50, 0, 10000);

        RARE_WEIGHT = builder
                .comment("Weight for RARE signals to spawn. Default: 25")
                .defineInRange("rareSignalWeight", 25, 0, 10000);

        RARER_WEIGHT = builder
                .comment("Weight for RARER signals to spawn. Default: 10")
                .defineInRange("rarerSignalWeight", 10, 0, 10000);

        VERY_RARE_WEIGHT = builder
                .comment("Weight for VERY RARE signals to spawn. Default: 5")
                .defineInRange("veryRareSignalWeight", 5, 0, 10000);

        SERVER_BREAK_INTERVAL_MINUTES = builder
                .comment("How often one random server/satellite loses calibration.")
                .defineInRange("serverBreakIntervalMinutes", 5.0D, 0.0D, 1440.0D);

        RECENTLY_FIXED_PROTECTION_MINUTES = builder
                .comment("How long a fixed server/satellite is protected from random calibration loss.")
                .defineInRange("recentlyFixedProtectionMinutes", 10.0D, 0.0D, 1440.0D);

        DEBUG_SIGNAL_BREAKS = builder
                .comment("Logs break timer, degradation, and recently-fixed protection checks.")
                .define("debugSignalBreaks", false);

        TERMINAL_PUNCH_DAMAGE = builder
                .comment("Damage dealt to players when they punch terminals, servers, or consoles.")
                .defineInRange("terminalPunchDamage", 5.0D, 0.0D, 100.0D);

        builder.pop();

        builder.push("transformer");
        TRANSFORMER_BASE_ENERGY = builder
                .comment("Starting energy of transformer network (ticks worth of operation). 36000 = 30 min")
                .defineInRange("baseEnergy", 40000, 1000, 1000000);

        TRANSFORMER_BASE_DRAIN = builder
                .comment("Base drain time before device scaling. Higher = slower energy usage")
                .defineInRange("baseDrain", 1200.0D, 1.0D, 100000.0D);

        TRANSFORMER_DEVICE_FACTOR = builder
                .comment("How strongly devices affect drain speed 0.5~100, 0.5 - almost 0 impact")
                .defineInRange("deviceFactor", 1.5D, 0.0D, 20.0D);

        TRANSFORMER_LOG_MULTIPLIER = builder
                .comment("Multiplier for log1p(deviceCount)")
                .defineInRange("logMultiplier", 1.5D, 0.0D, 20.0D);

        TRANSFORMER_MIN_TICKS = builder
                .comment("Minimum ticks between energy drains")
                .defineInRange("minTicks", 40, 1, 10000);

        builder.pop();

        builder.push("events");

        PYRAMID_KILL_QUOTA = builder
                .comment("The number of hostile mobs a single Rozital Pyramid must consume before disappearing.")
                .defineInRange("pyramidKillQuota", 20, 1, 1000); // now quota is 20

        builder.pop();

        SERVER_SPEC = builder.build();
    }

    public static float getTerminalPunchDamage() {
        return TERMINAL_PUNCH_DAMAGE.get().floatValue();
    }

    public static long getServerBreakIntervalTicks() {
        return minutesToTicks(SERVER_BREAK_INTERVAL_MINUTES.get(), -1L);
    }

    public static long getRecentlyFixedProtectionTicks() {
        return minutesToTicks(RECENTLY_FIXED_PROTECTION_MINUTES.get(), 0L);
    }

    public static boolean debugSignalBreaks() {
        return DEBUG_SIGNAL_BREAKS.get();
    }

    public static int getCommonWeight() { return COMMON_WEIGHT.get(); }

    public static int getRareWeight() { return RARE_WEIGHT.get(); }

    public static int getRarerWeight() { return RARER_WEIGHT.get(); }

    public static int getVeryRareWeight() { return VERY_RARE_WEIGHT.get(); }

    public static int getPyramidKillQuota() {
        return PYRAMID_KILL_QUOTA.get();
    }

    private static long minutesToTicks(double minutes, long disabledValue) {
        if (minutes <= 0.0D) {
            return disabledValue;
        }

        return Math.max(1L, Math.round(minutes * TICKS_PER_MINUTE));
    }
}