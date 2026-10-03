package com.fosder.pocketdimension.util;

import net.minecraftforge.common.ForgeConfigSpec;

public final class PocketClientConfig {
    private static final String DEFAULT_TRANSLATION_API_URL =
            "https://distributions.crowdin.net/95af9b78fd3175bd3b60699o28i/content/es-EC/translation_credits.json";

    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue DIMENSIONAL_FOG;
    public static final ForgeConfigSpec.BooleanValue DETAILED_MACHINE_INFO;
    public static final ForgeConfigSpec.BooleanValue GUI_TOOLTIPS;
    public static final ForgeConfigSpec.ConfigValue<String> COMMUNITY_TRANSLATION_API_URL;
    public static final ForgeConfigSpec.IntValue COMMUNITY_TRANSLATION_API_TIMEOUT_MS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("visual");
        DIMENSIONAL_FOG = builder
                .comment("Use the Pocket Dimension and Nether corruption fog effect.")
                .define("dimensionalFog", true);
        GUI_TOOLTIPS = builder
                .comment("Show extra tooltips in Pocket Dimension interfaces.")
                .define("guiTooltips", true);
        builder.pop();

        builder.push("information");
        DETAILED_MACHINE_INFO = builder
                .comment("Show detailed drain and activity information in the stabilizer screen.")
                .define("detailedMachineInfo", true);
        builder.pop();

        builder.push("community");
        COMMUNITY_TRANSLATION_API_URL = builder
                .comment("Optional Crowdin CDN HTTPS URL returning translation credits as JSON. Leave empty to use local credits.")
                .define("translationApiUrl", DEFAULT_TRANSLATION_API_URL);
        COMMUNITY_TRANSLATION_API_TIMEOUT_MS = builder
                .comment("Connection and read timeout for the community translation API in milliseconds.")
                .defineInRange("translationApiTimeoutMs", 3500, 1000, 10000);
        builder.pop();

        CLIENT_SPEC = builder.build();
    }

    private PocketClientConfig() {
    }

    public static void resetToDefaults() {
        DIMENSIONAL_FOG.set(true);
        DETAILED_MACHINE_INFO.set(true);
        GUI_TOOLTIPS.set(true);
        COMMUNITY_TRANSLATION_API_URL.set(DEFAULT_TRANSLATION_API_URL);
        COMMUNITY_TRANSLATION_API_TIMEOUT_MS.set(3500);
    }
}
