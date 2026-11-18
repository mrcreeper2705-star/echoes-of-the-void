package dev.voidechoes.storm;

import dev.voidechoes.VoidEchoes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Server -> client: a Void Pillar appears at (x, z). */
public record PillarPayload(double x, double z) implements CustomPayload {
    public static final CustomPayload.Id<PillarPayload> ID = new CustomPayload.Id<>(VoidEchoes.id("void_pillar"));
    public static final PacketCodec<RegistryByteBuf, PillarPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, PillarPayload::x, PacketCodecs.DOUBLE, PillarPayload::z, PillarPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
