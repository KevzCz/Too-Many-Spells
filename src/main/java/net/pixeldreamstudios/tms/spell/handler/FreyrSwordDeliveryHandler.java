package net.pixeldreamstudios.tms.spell.handler;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.summonerlib.api.SummonBuilder;
import net.pixeldreamstudios.summonerlib.manager.SummonLifecycleManager;
import net.pixeldreamstudios.summonerlib.util.SummonAttributeApplicator;
import net.pixeldreamstudios.summonerlib.util.SummonLimitHandler;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FreyrSwordDeliveryHandler implements SpellHandlers.CustomDelivery {

    private static final boolean ALLOW_INTERACTION = false;

    @Override
    public boolean onSpellDelivery(
            World world,
            RegistryEntry<Spell> spellEntry,
            PlayerEntity caster,
            List<SpellHelper.DeliveryTarget> targets,
            SpellHelper.ImpactContext context,
            @Nullable Vec3d customLocation) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }

        Spell spell = spellEntry.value();

        if (spell.impacts == null || spell.impacts.isEmpty()) {
            return false;
        }

        SummonLimitHandler.handleSummonLimit(
                caster,
                ExtendedFreyrSwordData.SUMMON_TYPE,
                1
        );

        for (Spell.Impact impact : spell.impacts) {
            if (impact.action != null && impact.action.spawns != null) {
                for (Spell.Impact.Action.Spawn spawnData : impact.action.spawns) {
                    spawnFreyrSword(serverWorld, caster, spawnData, impact);
                }
            }
        }

        return true;
    }

    private void spawnFreyrSword(ServerWorld world, PlayerEntity player, Spell.Impact.Action.Spawn spawnData, Spell.Impact impact) {
        Identifier itemId = Identifier.of("soulsweapons", "freyr_sword");
        var item = Registries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item);

        FreyrSwordEntity freyrSword = new FreyrSwordEntity(world, player, stack);

        Vec3d spawnPos = calculateSpawnPosition(player, spawnData.placement);
        freyrSword.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        freyrSword.setYaw(player.getYaw());
        freyrSword.setStationaryPos(FreyrSwordEntity.NULLISH_POS);

        if (freyrSword.getAttributeInstance(EntityAttributes.GENERIC_SCALE) != null) {
            freyrSword.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(0.75);
        }

        float coefficient = 0.065F;
        if (impact.action != null && impact.action.damage != null) {
            coefficient = impact.action.damage.spell_power_coefficient;
        }

        SummonAttributeApplicator.AttributeConfig config = new SummonAttributeApplicator.AttributeConfig(
                player,
                freyrSword,
                coefficient,
                SpellSchools.SOUL
        );

        SummonAttributeApplicator.applyAllAttributes(config);

        int lifetime = SummonAttributeApplicator.calculateLifetime(player, coefficient, SpellSchools.SOUL);

        FreyrSwordEntity spawned = SummonBuilder.create(player, freyrSword, world)
                .withType(ExtendedFreyrSwordData.SUMMON_TYPE)
                .withLifetime(lifetime)
                .allowInteraction(ALLOW_INTERACTION)
                .slotCost(1)
                .group("freyr_swords")
                .onSpawn(sword -> {
                    ExtendedFreyrSwordData.addSpellSummonUuid(player, sword.getUuid());
                    SummonLifecycleManager.spawnSummonParticles(world, sword);
                })
                .build();

        if (spawned != null) {
            player.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.0F);
        }
    }

    private Vec3d calculateSpawnPosition(PlayerEntity player, Spell.EntityPlacement placement) {
        Vec3d playerPos = player.getPos();
        Vec3d lookVec = player.getRotationVector();

        double x = playerPos.x + lookVec.x * placement.location_offset_by_look;
        double y = playerPos.y;
        double z = playerPos.z + lookVec.z * placement.location_offset_by_look;

        x += placement.location_offset_x;
        y += placement.location_offset_y;
        z += placement.location_offset_z;

        return new Vec3d(x, y, z);
    }
}