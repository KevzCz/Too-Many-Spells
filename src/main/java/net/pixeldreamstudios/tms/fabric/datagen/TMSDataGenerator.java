package net.pixeldreamstudios.tms.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.pixeldreamstudios.tms.spell.SoulslikeWeaponrySpells;
import net.pixeldreamstudios.tms.spell.SummonSpells;
import net.spell_engine.api.datagen.SpellGenerator;

import java.util.concurrent.CompletableFuture;

public class TMSDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(SpellGen::new);
    }

    public static class SpellGen extends SpellGenerator {
        public SpellGen(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateSpells(Builder builder) {
            for (var entry : SoulslikeWeaponrySpells.entries) {
                builder.add(entry.id(), entry.spell());
            }
            for (var entry : SummonSpells.entries) {
                builder.add(entry.id(), entry.spell());
            }
        }
    }
}