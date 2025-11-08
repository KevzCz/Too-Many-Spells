package net.pixeldreamstudios.tms.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.pixeldreamstudios.tms.TooManySpells;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class TMSSoulsweaponsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TMSSoulsweaponsConfig INSTANCE;
    private static final String CONFIG_FILE_NAME = "too_many_spells/soulsweapons/tms-soulsweapons.json";

    public MjolnirConfig mjolnir = new MjolnirConfig();
    public NightfallConfig nightfall = new NightfallConfig();
    public ForlornScytheConfig forlorn_scythe = new ForlornScytheConfig();
    public LeviathanAxeConfig leviathan_axe = new LeviathanAxeConfig();
    public LichBaneConfig lich_bane = new LichBaneConfig();
    public MasterSwordConfig master_sword = new MasterSwordConfig();
    public DarkMoonGreatswordConfig dark_moon_greatsword = new DarkMoonGreatswordConfig();
    public MoonlightGreatswordConfig moonlight_greatsword = new MoonlightGreatswordConfig();
    public BluemoonGreatswordConfig bluemoon_greatsword = new BluemoonGreatswordConfig();
    public PureMoonlightGreatswordConfig pure_moonlight_greatsword = new PureMoonlightGreatswordConfig();
    public MoonlightShortswordConfig moonlight_shortsword = new MoonlightShortswordConfig();
    public BluemoonShortswordConfig bluemoon_shortsword = new BluemoonShortswordConfig();
    public CometSpearConfig comet_spear = new CometSpearConfig();
    public UmbralTrespassConfig umbral_trespass = new UmbralTrespassConfig();
    public WitherSkullConfig wither_skull = new WitherSkullConfig();
    public LightningConfig lightning = new LightningConfig();
    public ExcaliburConfig excalibur = new ExcaliburConfig();
    public SoulReaperConfig soul_reaper = new SoulReaperConfig();
    public FrostmourneConfig frostmourne = new FrostmourneConfig();
    public NightsEdgeConfig nights_edge = new NightsEdgeConfig();
    public HolyMoonlightGreatswordConfig holy_moonlight_greatsword = new HolyMoonlightGreatswordConfig();
    public RimeSpectreConfig rime_spectre = new RimeSpectreConfig();
    public SoulmassConfig soulmass = new SoulmassConfig();
    public EvokerFangsConfig evoker_fangs = new EvokerFangsConfig();
    public SupernovaConfig supernova = new SupernovaConfig();
    public MoonveilConfig moonveil = new MoonveilConfig();
    public DragonbaneConfig dragonbane = new DragonbaneConfig();
    public DragonStaffConfig dragon_staff = new DragonStaffConfig();
    public DarkinBladeConfig darkin_blade = new DarkinBladeConfig();
    public WhirligigSawbladeConfig whirligig_sawblade = new WhirligigSawbladeConfig();
    public TonitrusConfig tonitrus = new TonitrusConfig();
    public GhostGlaiveConfig ghost_glaive = new GhostGlaiveConfig();
    public FreyrSwordConfig freyr_sword = new FreyrSwordConfig();
    public DawnbreakerConfig dawnbreaker = new DawnbreakerConfig();
    public EmpoweredDawnbreakerConfig empowered_dawnbreaker = new EmpoweredDawnbreakerConfig();
    public DraupnirSpearConfig draupnir_spear = new DraupnirSpearConfig();
    public DragonslayerSwordBerserkConfig dragonslayer_sword_berserk = new DragonslayerSwordBerserkConfig();
    public BloodthirsterConfig bloodthirster = new BloodthirsterConfig();
    public BladeDanceConfig blade_dance = new BladeDanceConfig();
    public BloodlustConfig bloodlust = new BloodlustConfig();
    public DarkmoonLongbowConfig darkmoon_longbow = new DarkmoonLongbowConfig();
    public GaleforceConfig galeforce = new GaleforceConfig();

    public static TMSSoulsweaponsConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TMSSoulsweaponsConfig();
        }
        return INSTANCE;
    }

    public static void load(Path configDir) {
        File configFile = configDir.resolve(CONFIG_FILE_NAME).toFile();
        File configFolder = configFile.getParentFile();

        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                TMSSoulsweaponsConfig loaded = GSON.fromJson(reader, TMSSoulsweaponsConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                } else {
                    INSTANCE = new TMSSoulsweaponsConfig();
                }
                TooManySpells.LOGGER.info("Loaded TMS-Soulsweapons configuration from {}", configFile.getAbsolutePath());
            } catch (IOException e) {
                TooManySpells.LOGGER.error("Failed to load config file, using defaults", e);
                INSTANCE = new TMSSoulsweaponsConfig();
            }
        } else {
            INSTANCE = new TMSSoulsweaponsConfig();
            save(configDir);
        }
    }

    public static void save(Path configDir) {
        File configFile = configDir.resolve(CONFIG_FILE_NAME).toFile();
        File configFolder = configFile.getParentFile();

        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(INSTANCE, writer);
            TooManySpells.LOGGER.info("Saved TMS-Soulsweapons configuration to {}", configFile.getAbsolutePath());
        } catch (IOException e) {
            TooManySpells.LOGGER.error("Failed to save config file", e);
        }
    }

    private TMSSoulsweaponsConfig() {
    }

    public static class MjolnirConfig {
        public double lightningSpellPowerBonus = 4.0;
        public ProjectileConfig projectile = new ProjectileConfig();

        public static class ProjectileConfig {
            public float attackDamageBaseline = 13.0F;
        }
    }

    public static class NightfallConfig {
        public double soulSpellPowerBonus = 3.0;
        public double petInheritanceBonus = 0.1;
        public boolean useKevslibraryPetInheritanceAttribute = false;
        public float attackDamageBaseline = 11.0F;
        public float soulBaseline = 20.0F;
        public float attackDamageWeight = 0.5F;
        public float soulWeight = 0.5F;
    }

    public static class ForlornScytheConfig {
        public double soulSpellPowerBonus = 4.0;
    }

    public static class LeviathanAxeConfig {
        public double frostSpellPowerBonus = 4.0;
        public float attackDamageBaseline = 10.0F;
        public float frostBaseline = 20.0F;
        public float frostPerAmplifier = 10.0F;
        public float attackDamageWeight = 0.5F;
        public float frostWeight = 0.5F;
        public IceExplosionConfig ice_explosion = new IceExplosionConfig();

        public static class IceExplosionConfig {
            public float frostBaseline = 200.0F;
        }
    }

    public static class LichBaneConfig {
        public double fireSpellPowerBonus = 4.0;
        public float fireBaseline = 20.0F;
    }

    public static class MasterSwordConfig {
        public double maxHealthBonusPercentage = 0.20;
        public float attackDamageBaseline = 8.0F;
        public float maxHealthBaseline = 40.0F;
        public float attackDamageWeight = 0.5F;
        public float maxHealthWeight = 0.5F;
    }

    public static class DarkMoonGreatswordConfig {
        public double frostSpellPowerBonus = 3.0;
        public float frostBaseline = 20.0F;
        public float frostPerAmplifier = 10.0F;
    }

    public static class MoonlightGreatswordConfig {
        public float attackDamageBaseline = 9.0F;
    }

    public static class BluemoonGreatswordConfig {
        public float attackDamageBaseline = 8.0F;
    }

    public static class PureMoonlightGreatswordConfig {
        public float attackDamageBaseline = 11.0F;
    }

    public static class MoonlightShortswordConfig {
        public float attackDamageBaseline = 8.0F;
    }

    public static class BluemoonShortswordConfig {
        public float attackDamageBaseline = 7.0F;
    }

    public static class CometSpearConfig {
        public float attackDamageBaseline = 8.0F;
    }

    public static class UmbralTrespassConfig {
        public float darkinScytheBaseline = 12.0F;
        public float shadowAssassinScytheBaseline = 13.0F;
    }

    public static class WitherSkullConfig {
        public float soulBaseline = 20.0F;
    }

    public static class LightningConfig {
        public double damagePerSpellPower = 0.5;
        public float playerDamageMultiplier = 0.9F;
        public boolean damagePassiveEntities = false;
        public boolean damageTamedEntities = false;
        public boolean damageChanneler = false;
    }

    public static class ExcaliburConfig {
        public double arcaneSpellPowerBonus = 2.0;
        public double soulSpellPowerBonus = 2.0;
        public float arcaneBaseline = 20.0F;
        public float soulBaseline = 20.0F;
        public float arcaneWeight = 0.5F;
        public float soulWeight = 0.5F;
    }

    public static class SoulReaperConfig {
        public double soulSpellPowerBonus = 2.0;
        public double petInheritanceBonus = 0.15;
        public boolean useKevslibraryPetInheritanceAttribute = false;
    }

    public static class FrostmourneConfig {
        public double frostSpellPowerBonus = 3.0;
        public double petInheritanceBonus = 0.075;
        public boolean useKevslibraryPetInheritanceAttribute = false;
        public float frostBaseline = 20.0F;
        public float frostPerAmplifier = 10.0F;
    }

    public static class NightsEdgeConfig {
        public double arcaneSpellPowerBonus = 4.0;
        public float attackDamageBaseline = 10.0F;
        public float arcaneBaseline = 20.0F;
        public float attackDamageWeight = 0.5F;
        public float arcaneWeight = 0.5F;
    }

    public static class HolyMoonlightGreatswordConfig {
        public float attackDamageBaseline = 10.0F;
    }

    public static class RimeSpectreConfig {
        public float soulBaseline = 20.0F;
        public float frostBaseline = 20.0F;
        public float soulWeight = 0.25F;
        public float frostWeight = 0.75F;
    }

    public static class SoulmassConfig {
        public float soulBaseline = 20.0F;
    }

    public static class EvokerFangsConfig {
        public float soulBaseline = 10.0F;
    }

    public static class SupernovaConfig {
        public double fireSpellPowerBonus = 3.0;
        public float fireBaseline = 20.0F;
        public float flamePillarScaling = 0.75F;
        public float moltenMetalScaling = 0.1F;
    }

    public static class MoonveilConfig {
        public float attackDamageBaseline = 11.0F;
    }

    public static class DragonbaneConfig {
        public double lightningSpellPowerBonus = 4.0;
        public float lightningBaseline = 20.0F;
    }

    public static class DragonStaffConfig {
        public double arcaneSpellPowerBonus = 3.0;
        public float arcaneBaseline = 20.0F;
        public float healCapMultiplier = 1.25F;
        public float auraAmplifierPer10Arcane = 1.0F;
    }

    public static class DarkinBladeConfig {
        public float attackDamageBaseline = 11.0F;
        public float healMinScale = 0.75F;
        public float healMaxScale = 1.25F;
    }

    public static class WhirligigSawbladeConfig {
        public float attackDamageBaseline = 11.0F;
    }

    public static class TonitrusConfig {
        public double lightningSpellPowerBonus = 4.0;
        public float lightningBaseline = 40.0F;
        public float amplifierPerLightning = 20.0F;
    }

    public static class GhostGlaiveConfig {
        public double arcaneSpellPowerBonus = 4.0;
        public float attackDamageBaseline = 10.0F;
        public float arcaneBaseline = 20.0F;
        public float attackDamageWeight = 0.5F;
        public float arcaneWeight = 0.5F;
    }

    public static class FreyrSwordConfig {
        public double soulSpellPowerBonus = 4.0;
        public float attackDamageBaseline = 7.0F;
        public float soulBaseline = 20.0F;
        public float attackDamageWeight = 0.75F;
        public float soulWeight = 2.5F;
        public double petInheritanceBonus = 0.2;
        public boolean useKevslibraryPetInheritanceAttribute = false;
    }

    public static class DawnbreakerConfig {
        public double fireSpellPowerBonus = 2.0;
        public float fireBaseline = 20.0F;
    }

    public static class EmpoweredDawnbreakerConfig {
        public double fireSpellPowerBonus = 4.0;
        public float fireBaseline = 20.0F;
    }

    public static class DraupnirSpearConfig {
        public float attackDamageBaseline = 8.0F;
    }

    public static class DragonslayerSwordBerserkConfig {
        public float attackDamageBaseline = 12.0F;
    }

    public static class BloodthirsterConfig {
        public float attackDamageBaseline = 8.0F;
        public float healMinScale = 0.75F;
        public float healMaxScale = 1.50F;
    }

    public static class BladeDanceConfig {
        public float attackDamageBaseline = 8.0F;
        public float attackSpeedBaseline = 1.3F;
        public float attackDamageWeight = 0.75F;
        public float attackSpeedWeight = 0.75F;
    }

    public static class BloodlustConfig {
        public float attackDamageBaseline = 7.0F;
        public float selfDamageCapHearts = 12.0F;
        public float selfDamageCapHealthPercent = 0.5F;
    }

    public static class DarkmoonLongbowConfig {
        public double arcaneSpellPowerBonus = 4.0;
        public float rangedDamageBaseline = 9.0F;
        public float arcaneBaseline = 20.0F;
        public float rangedDamageWeight = 0.05F;
        public float arcaneWeight = 0.075F;
    }

    public static class GaleforceConfig {
        public float rangedDamageBaseline = 9.0F;
    }
}