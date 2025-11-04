package net.pixeldreamstudios.tms.util;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompatibility {
    public static final String SOULSLIKE_WEAPONRY = "soulsweapons";

    public static boolean isSoulslikeWeaponryLoaded() {
        return FabricLoader.getInstance().isModLoaded(SOULSLIKE_WEAPONRY);
    }

}