package net.pixeldreamstudios.tms.spell;

import net.minecraft.util.Identifier;
import net.pixeldreamstudios.tms.TooManySpells;
import net.spell_engine.api.datagen.SpellBuilder;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.Sound;
import net.spell_engine.fx.SpellEngineSounds;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;

public class SoulslikeWeaponrySpells {

    public record Entry(Identifier id, Spell spell) { }
    public static final List<Entry> entries = new ArrayList<>();

    public static final Entry SUMMON_FREYR_SWORD = add(createSummonFreyrSword());
    public static final Entry FLAME_PILLAR_PROC = add(createFlamePillarProc());

    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static Entry createSummonFreyrSword() {
        var id = Identifier.of(TooManySpells.MOD_ID, "summon_freyr_sword");

        Spell spell = new Spell();
        spell.school = SpellSchools.SOUL;
        spell.range = 20.0F;
        spell.tier = 2;
        spell.type = Spell.Type.ACTIVE;

        spell.learn = new Spell.Learn();

        spell.active = new Spell.Active();
        spell.active.cast = new Spell.Active.Cast();
        spell.active.cast.duration = 0.5F;
        spell.active.cast.animation = "spell_engine:two_handed_channeling";

        SpellBuilder.Casting.channel(spell, 4, 20);
        spell.active.cast.animation = "spell_engine:two_handed_channeling";
        spell.active.cast.sound = new Sound(SpellEngineSounds.GENERIC_ARCANE_CASTING.id(), 0);

        spell.release = new Spell.Release();
        spell.release.animation = "spell_engine:two_handed_release";
        spell.release.sound = new Sound("minecraft:entity.evoker.cast_spell", 1.0F, 1.2F, 0.1F);

        spell.target = new Spell.Target();
        spell.target.type = Spell.Target.Type.CASTER;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.CUSTOM;
        spell.deliver.custom = new Spell.Delivery.Custom();
        spell.deliver.custom.handler = TooManySpells.MOD_ID + ":summon_freyr_sword";

        Spell.Impact impact = new Spell.Impact();
        impact.school = SpellSchools.SOUL;
        impact.action = new Spell.Impact.Action();
        impact.action.type = Spell.Impact.Action.Type.SPAWN;

        impact.action.damage = new Spell.Impact.Action.Damage();
        impact.action.damage.spell_power_coefficient = 0.065F;

        List<Spell.Impact.Action.Spawn> spawns = new ArrayList<>();
        Spell.Impact.Action.Spawn spawn = new Spell.Impact.Action.Spawn();
        spawn.entity_type_id = "soulsweapons:freyr_sword_entity";
        spawn.time_to_live_seconds = 30;
        spawn.placement = new Spell.EntityPlacement();
        spawn.placement.location_offset_by_look = 2.0F;
        spawn.placement.apply_yaw = true;
        spawns.add(spawn);
        impact.action.spawns = spawns;

        impact.particles = new ParticleBatch[1];
        impact.particles[0] = new ParticleBatch(
                "minecraft:soul_fire_flame",
                ParticleBatch.Shape.SPHERE,
                ParticleBatch.Origin.CENTER,
                20,
                0.1F,
                0.2F
        );

        impact.sound = new Sound("minecraft:block.beacon.activate", 0.5F, 1.5F, 0.1F);

        spell.impacts = List.of(impact);

        spell.cost = new Spell.Cost();
        spell.cost.cooldown = new Spell.Cost.Cooldown();
        spell.cost.cooldown.duration = 1.0F;
        spell.cost.cooldown.proportional = true;
        spell.cost.durability = 1;

        spell.cost.item = new Spell.Cost.Item();
        spell.cost.item.id = "runes:soul_stone";

        return new Entry(id, spell);
    }

    private static Entry createFlamePillarProc() {
        var id = Identifier.of(TooManySpells.MOD_ID, "flame_pillar_proc");

        Spell spell = new Spell();
        spell.school = SpellSchools.FIRE;
        spell.range = 6.0F;
        spell.tier = 1;
        spell.type = Spell.Type.PASSIVE;
        spell.group = "secondary";

        spell.passive = new Spell.Passive();

        Spell.Trigger trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.stage = Spell.Trigger.Stage.POST;
        trigger.chance = 1.0F;

        spell.passive.triggers = List.of(trigger);

        spell.target = new Spell.Target();
        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.CUSTOM;
        spell.deliver.custom = new Spell.Delivery.Custom();
        spell.deliver.custom.handler = TooManySpells.MOD_ID + ":flame_pillar_proc";

        spell.learn = new Spell.Learn();

        spell.release = new Spell.Release();
        spell.release.sound = new Sound("minecraft:entity.blaze.shoot", 1.0F, 0.8F, 0.1F);

        Spell.Impact impact = new Spell.Impact();
        impact.school = SpellSchools.FIRE;
        impact.action = new Spell.Impact.Action();
        impact.action.type = Spell.Impact.Action.Type.DAMAGE;

        impact.action.damage = new Spell.Impact.Action.Damage();
        impact.action.damage.spell_power_coefficient = 0.5F;
        impact.action.damage.knockback = 2.0F;

        impact.particles = new ParticleBatch[1];
        impact.particles[0] = new ParticleBatch(
                "minecraft:flame",
                ParticleBatch.Shape.PILLAR,
                ParticleBatch.Origin.FEET,
                30,
                0.3F,
                2.0F
        );

        impact.sound = new Sound("minecraft:entity.blaze.shoot", 1.0F, 0.8F, 0.1F);

        spell.impacts = List.of(impact);

        return new Entry(id, spell);
    }
}