package net.pixeldreamstudios.tms.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.network.payload.SummonSyncPayload;

import java.util.*;

public class SummonTracker {

    private static final Map<UUID, List<SummonData>> PLAYER_SUMMONS = new HashMap<>();
    private static final Map<UUID, SummonData> ENTITY_LOOKUP = new HashMap<>();
    private static final Set<UUID> CLIENT_SPELL_SUMMONS = new HashSet<>();
    private static final int WARNING_TICKS = 100;
    private static final int FLASH_INTERVAL = 20;

    public static class SummonData {
        public final UUID entityUuid;
        public final UUID ownerUuid;
        public final long spawnTick;
        public final int lifetimeTicks;
        public final boolean allowInteraction;
        public final String summonType;
        private final Entity entityRef;
        private boolean hasShownWarning = false;
        public final int summonIndex;

        public SummonData(UUID entityUuid, UUID ownerUuid, long spawnTick, int lifetimeTicks, boolean allowInteraction, String summonType, Entity entityRef, int summonIndex) {
            this.entityUuid = entityUuid;
            this.ownerUuid = ownerUuid;
            this.spawnTick = spawnTick;
            this.lifetimeTicks = lifetimeTicks;
            this.allowInteraction = allowInteraction;
            this.summonType = summonType;
            this.entityRef = entityRef;
            this.summonIndex = summonIndex;
        }

        public Entity getEntity() {
            return entityRef;
        }

        public boolean isExpired(long currentTick) {
            if (lifetimeTicks <= 0) return false;
            return (currentTick - spawnTick) >= lifetimeTicks;
        }

        public int getRemainingTicks(long currentTick) {
            if (lifetimeTicks <= 0) return Integer.MAX_VALUE;
            return (int) (lifetimeTicks - (currentTick - spawnTick));
        }

        public boolean shouldShowWarning(long currentTick) {
            return getRemainingTicks(currentTick) <= WARNING_TICKS;
        }

        public boolean shouldFlash(long currentTick) {
            int remainingTicks = getRemainingTicks(currentTick);
            return remainingTicks <= WARNING_TICKS && (remainingTicks % FLASH_INTERVAL) < 10;
        }
    }

    public static void registerSummon(PlayerEntity owner, Entity entity, long spawnTick, int lifetimeTicks, boolean allowInteraction, String summonType) {
        UUID ownerUuid = owner.getUuid();
        UUID entityUuid = entity.getUuid();

        List<SummonData> playerSummons = PLAYER_SUMMONS.computeIfAbsent(ownerUuid, k -> new ArrayList<>());
        int summonIndex = (int) playerSummons.stream().filter(s -> s.summonType.equals(summonType)).count();

        SummonData data = new SummonData(entityUuid, ownerUuid, spawnTick, lifetimeTicks, allowInteraction, summonType, entity, summonIndex);

        playerSummons.add(data);
        ENTITY_LOOKUP.put(entityUuid, data);

        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            syncToClients(serverWorld, entityUuid, true);
        }

        TooManySpells.LOGGER.info("✓ Registered {} spell summon: {} [Index: {}]", summonType, entityUuid, summonIndex);
        TooManySpells.LOGGER.info("  - Owner: {} ({})", owner.getName().getString(), ownerUuid);
        TooManySpells.LOGGER.info("  - Lifetime: {} ticks", lifetimeTicks);
        TooManySpells.LOGGER.info("  - Total summons for player: {}", playerSummons.size());
    }

    public static void unregisterSummon(UUID entityUuid) {
        SummonData data = ENTITY_LOOKUP.remove(entityUuid);
        if (data != null) {
            List<SummonData> playerSummons = PLAYER_SUMMONS.get(data.ownerUuid);
            if (playerSummons != null) {
                playerSummons.removeIf(s -> s.entityUuid.equals(entityUuid));
                if (playerSummons.isEmpty()) {
                    PLAYER_SUMMONS.remove(data.ownerUuid);
                } else {
                    reindexSummons(playerSummons, data.summonType);
                }
            }
        }
    }

    private static void reindexSummons(List<SummonData> summons, String summonType) {
        List<SummonData> typedSummons = summons.stream()
                .filter(s -> s.summonType.equals(summonType))
                .sorted(Comparator.comparingLong(s -> s.spawnTick))
                .toList();

        for (int i = 0; i < typedSummons.size(); i++) {
            SummonData oldData = typedSummons.get(i);
            SummonData newData = new SummonData(
                    oldData.entityUuid,
                    oldData.ownerUuid,
                    oldData.spawnTick,
                    oldData.lifetimeTicks,
                    oldData.allowInteraction,
                    oldData.summonType,
                    oldData.entityRef,
                    i
            );
            summons.remove(oldData);
            summons.add(newData);
            ENTITY_LOOKUP.put(oldData.entityUuid, newData);
        }
    }

    public static void unregisterSummon(ServerWorld world, UUID entityUuid) {
        unregisterSummon(entityUuid);
        syncToClients(world, entityUuid, false);
    }

    private static void syncToClients(ServerWorld world, UUID entityUuid, boolean isRegistering) {
        SummonSyncPayload payload = new SummonSyncPayload(entityUuid, isRegistering);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static boolean isSpellSummon(UUID entityUuid) {
        return ENTITY_LOOKUP.containsKey(entityUuid);
    }

    public static SummonData getSummonData(UUID entityUuid) {
        return ENTITY_LOOKUP.get(entityUuid);
    }

    public static boolean canInteract(UUID entityUuid) {
        SummonData data = ENTITY_LOOKUP.get(entityUuid);
        return data != null && data.allowInteraction;
    }

    public static int getPlayerSummonCount(UUID playerUuid) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        return summons != null ? summons.size() : 0;
    }

    public static int getPlayerSummonCountByType(UUID playerUuid, String summonType) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return 0;
        return (int) summons.stream().filter(s -> s.summonType.equals(summonType)).count();
    }

    public static List<UUID> getPlayerSummons(UUID playerUuid) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return Collections.emptyList();
        return summons.stream().map(s -> s.entityUuid).toList();
    }

    public static List<UUID> getPlayerSummonsByType(UUID playerUuid, String summonType) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return Collections.emptyList();
        return summons.stream()
                .filter(s -> s.summonType.equals(summonType))
                .map(s -> s.entityUuid)
                .toList();
    }

    public static UUID getOldestSummonByType(UUID playerUuid, String summonType) {
        List<SummonData> summons = PLAYER_SUMMONS.get(playerUuid);
        if (summons == null) return null;

        return summons.stream()
                .filter(s -> s.summonType.equals(summonType))
                .min(Comparator.comparingLong(s -> s.spawnTick))
                .map(s -> s.entityUuid)
                .orElse(null);
    }

    public static void tick(ServerWorld world) {
        long currentTick = world.getTime();
        List<UUID> playersToRemove = new ArrayList<>();

        PLAYER_SUMMONS.forEach((ownerUuid, summons) -> {
            summons.removeIf(data -> {
                Entity entity = data.getEntity();

                if (entity == null || entity.isRemoved()) {
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity != null && entity.getWorld() instanceof ServerWorld sw) {
                        syncToClients(sw, data.entityUuid, false);
                    } else {
                        syncToClients(world, data.entityUuid, false);
                    }
                    return true;
                }

                if (!entity.isAlive()) {
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncToClients(sw, data.entityUuid, false);
                    }
                    return true;
                }

                PlayerEntity owner = entity.getWorld().getPlayerByUuid(data.ownerUuid);

                if (owner == null || !owner.isAlive()) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        spawnDespawnParticles(sw, entity);
                    }
                    entity.discard();
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncToClients(sw, data.entityUuid, false);
                    }
                    return true;
                }

                if (data.shouldShowWarning(currentTick) && !data.hasShownWarning) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        spawnWarningParticles(sw, entity);
                    }
                    data.hasShownWarning = true;
                }

                if (data.shouldFlash(currentTick)) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        spawnFlashParticles(sw, entity);
                    }
                }

                if (data.isExpired(currentTick)) {
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        spawnDespawnParticles(sw, entity);
                    }
                    entity.discard();
                    ENTITY_LOOKUP.remove(data.entityUuid);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        syncToClients(sw, data.entityUuid, false);
                    }
                    return true;
                }

                return false;
            });

            if (summons.isEmpty()) {
                playersToRemove.add(ownerUuid);
            }
        });

        playersToRemove.forEach(PLAYER_SUMMONS::remove);
    }

    private static void spawnWarningParticles(ServerWorld world, Entity entity) {
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double offsetX = Math.cos(angle) * 0.5;
            double offsetZ = Math.sin(angle) * 0.5;

            world.spawnParticles(
                    ParticleTypes.FLAME,
                    entity.getX() + offsetX,
                    entity.getY() + 0.5,
                    entity.getZ() + offsetZ,
                    1,
                    0, 0.1, 0,
                    0.01
            );
        }
    }

    private static void spawnFlashParticles(ServerWorld world, Entity entity) {
        world.spawnParticles(
                ParticleTypes.END_ROD,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                3,
                0.3, 0.5, 0.3,
                0.02
        );
    }

    private static void spawnDespawnParticles(ServerWorld world, Entity entity) {
        world.spawnParticles(
                ParticleTypes.POOF,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                30,
                0.5, 0.8, 0.5,
                0.1
        );

        for (int i = 0; i < 15; i++) {
            double angle = (Math.PI * 2 * i) / 15;
            double offsetX = Math.cos(angle) * 0.3;
            double offsetZ = Math.sin(angle) * 0.3;

            world.spawnParticles(
                    ParticleTypes.SOUL,
                    entity.getX() + offsetX,
                    entity.getY() + 0.5,
                    entity.getZ() + offsetZ,
                    1,
                    0, 0.5, 0,
                    0.05
            );
        }

        world.spawnParticles(
                ParticleTypes.ENCHANT,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                20,
                0.5, 0.8, 0.5,
                0.5
        );
    }

    public static void cleanupPlayer(UUID playerUuid) {
        List<SummonData> summons = PLAYER_SUMMONS.remove(playerUuid);
        if (summons != null) {
            for (SummonData data : summons) {
                ENTITY_LOOKUP.remove(data.entityUuid);
            }
        }
    }

    public static void clientRegisterSummon(UUID entityUuid) {
        CLIENT_SPELL_SUMMONS.add(entityUuid);
        }

    public static void clientUnregisterSummon(UUID entityUuid) {
        CLIENT_SPELL_SUMMONS.remove(entityUuid);
    }

    public static boolean clientIsSpellSummon(UUID entityUuid) {
        return CLIENT_SPELL_SUMMONS.contains(entityUuid);
    }

    public static void clientClearAll() {
        CLIENT_SPELL_SUMMONS.clear();
    }
}