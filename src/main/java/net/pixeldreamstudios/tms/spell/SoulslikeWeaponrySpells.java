package net.pixeldreamstudios.tms.spell;

import net.minecraft.util.Identifier;
import net.pixeldreamstudios.tms.TooManySpells;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.Sound;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.List;

public class SoulslikeWeaponrySpells {

    public record Entry(Identifier id, Spell spell) { }
    public static final List<Entry> entries = new ArrayList<>();

    public static final Entry SUMMON_FREYR_SWORD = add(createSummonFreyrSword());

    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static Entry createSummonFreyrSword() {
        var id = Identifier.of(TooManySpells.MOD_ID, "summon_freyr_sword");

        Spell spell = new Spell();
        spell.school = SpellSchools.ARCANE;
        spell.range = 20.0F;
        spell.tier = 2;
        spell.type = Spell.Type.ACTIVE;

        spell.active = new Spell.Active();
        spell.active.cast.duration = 1.5F;
        spell.active.cast.animation = "casting";

        spell.release.sound = new Sound("minecraft:entity.evoker.cast_spell", 1.0F, 1.2F, 0.1F);

        spell.target.type = Spell.Target.Type.CASTER;

        spell.deliver.type = Spell.Delivery.Type.CUSTOM;
        spell.deliver.custom = new Spell.Delivery.Custom();
        spell.deliver.custom.handler = TooManySpells.MOD_ID + ":summon_freyr_sword";

        Spell.Impact impact = new Spell.Impact();
        impact.action = new Spell.Impact.Action();
        impact.action.type = Spell.Impact.Action.Type.SPAWN;

        List<Spell.Impact.Action.Spawn> spawns = new ArrayList<>();
        Spell.Impact.Action.Spawn spawn = new Spell.Impact.Action.Spawn();
        spawn.entity_type_id = "soulsweapons:freyr_sword_entity";
        spawn.time_to_live_seconds = 30;
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

        spell.cost.cooldown.duration = 20.0F;
        spell.cost.durability = 5;

        return new Entry(id, spell);
    }
}