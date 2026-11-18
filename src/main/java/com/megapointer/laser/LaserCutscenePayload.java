package com.megapointer.laser;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
public record LaserCutscenePayload(int mode,BlockPos target,int duration) implements CustomPayload {
    public static final CustomPayload.Id<LaserCutscenePayload> ID=
        new CustomPayload.Id<>(Identifier.of(MegaLaser90.MOD_ID,"cutscene"));
    public static final PacketCodec<RegistryByteBuf,LaserCutscenePayload> CODEC=PacketCodec.tuple(
        PacketCodecs.INTEGER,LaserCutscenePayload::mode,
        BlockPos.PACKET_CODEC,LaserCutscenePayload::target,
        PacketCodecs.INTEGER,LaserCutscenePayload::duration,
        LaserCutscenePayload::new);
    @Override public CustomPayload.Id<? extends CustomPayload> getId(){return ID;}
}
