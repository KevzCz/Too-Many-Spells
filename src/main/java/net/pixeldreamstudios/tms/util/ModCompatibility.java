package net.pixeldreamstudios.tms.util;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompatibility {
    public static final String SOULSLIKE_WEAPONRY = "soulsweapons";
    public static final String SIMPLY_SWORDS = "simplyswords";
    public static final String SIMPLY_MORE = "simplymore";
    public static boolean isSoulslikeWeaponryLoaded() {
        return FabricLoader.getInstance().isModLoaded(SOULSLIKE_WEAPONRY);
    }
    public static boolean isSimplySwordsLoaded() {
        return FabricLoader.getInstance().isModLoaded(SIMPLY_SWORDS);
    }
    public static boolean isSimplyMoreLoaded() {
        return FabricLoader.getInstance().isModLoaded(SIMPLY_MORE);
    }
}