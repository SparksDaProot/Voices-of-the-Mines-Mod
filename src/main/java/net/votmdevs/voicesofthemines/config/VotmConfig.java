package net.votmdevs.voicesofthemines.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class VotmConfig {

    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.DoubleValue SERVER_BREAK_INTERVAL_MINUTES;
    public static final ForgeConfigSpec.DoubleValue RECENTLY_FIXED_PROTECTION_MINUTES;
    public static final ForgeConfigSpec.BooleanValue DEBUG_SIGNAL_BREAKS;
    public static final ForgeConfigSpec.DoubleValue TERMINAL_PUNCH_DAMAGE;

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