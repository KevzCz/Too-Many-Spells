package net.pixeldreamstudios.tms.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;
import net.pixeldreamstudios.tms.TooManySpells;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record SummonSyncPayload(UUID entityUuid, boolean isRegistering) implements CustomPayload {

    public static final Id<SummonSyncPayload> ID = new Id<>(Identifier.of(TooManySpells.MOD_ID, "summon_sync"));

    public static final PacketCodec<RegistryByteBuf, SummonSyncPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, SummonSyncPayload::entityUuid,
            PacketCodecs.BOOL, SummonSyncPayload::isRegistering,
            SummonSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}