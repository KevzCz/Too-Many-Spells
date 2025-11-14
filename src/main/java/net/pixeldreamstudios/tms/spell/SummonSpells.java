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

public class SummonSpells {

    public record Entry(Identifier id, Spell spell) { }
    public static final List<Entry> entries = new ArrayList<>();
    public static final Entry SUMMON_EVOKER_FANGS = add(createSummonEvokerFangs());

    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static Entry createSummonEvokerFangs() {
        var id = Identifier.of(TooManySpells.MOD_ID, "summon_evoker_fangs");

        Spell spell = new Spell();
        spell.school = SpellSchools.SOUL;
        spell.range = 30.0F;
        spell.tier = 2;
        spell.type = Spell.Type.ACTIVE;

        spell.learn = new Spell.Learn();

        spell.active = new Spell.Active();
        spell.active.cast = new Spell.Active.Cast();
        spell.active.cast.duration = 0.75F;
        spell.active.cast.animation = "spell_engine:two_handed_channeling";

        SpellBuilder.Casting.channel(spell, 6, 30);
        spell.active.cast.sound = new Sound(SpellEngineSounds.GENERIC_ARCANE_CASTING.id(), 0);

        spell.release = new Spell.Release();
        spell.release.animation = "spell_engine:two_handed_release";
        spell.release.sound = new Sound("minecraft:entity.evoker.cast_spell", 1.0F, 1.0F, 0.1F);

        spell.target = new Spell.Target();
        spell.target.type = Spell.Target.Type.AIM;
        spell.target.aim = new Spell.Target.Aim();
        spell.target.aim.required = true;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.CUSTOM;
        spell.deliver.custom = new Spell.Delivery.Custom();
        spell.deliver.custom.handler = TooManySpells.MOD_ID + ":summon_evoker_fangs";

        Spell.Impact impact = new Spell.Impact();
        impact.school = SpellSchools.SOUL;
        impact.action = new Spell.Impact.Action();
        impact.action.type = Spell.Impact.Action.Type.DAMAGE;

        impact.action.damage = new Spell.Impact.Action.Damage();
        impact.action.damage.spell_power_coefficient = 0.5F;

        impact.particles = new ParticleBatch[1];
        impact.particles[0] = new ParticleBatch(
                "minecraft:soul_fire_flame",
                ParticleBatch.Shape.PILLAR,
                ParticleBatch.Origin.FEET,
                20,
                0.3F,
                1.5F
        );

        impact.sound = new Sound("minecraft:entity.evoker_fangs.attack", 1.0F, 1.0F, 0.1F);

        spell.impacts = List.of(impact);

        spell.cost = new Spell.Cost();
        spell.cost.cooldown = new Spell.Cost.Cooldown();
        spell.cost.cooldown.duration = 8.0F;
        spell.cost.cooldown.proportional = true;
        spell.cost.durability = 1;

        spell.cost.item = new Spell.Cost.Item();
        spell.cost.item.id = "runes:soul_stone";

        return new Entry(id, spell);
    }
}