package com.magneticcells.network;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.gui.MagneticCellMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncContents(int energy, int maxEnergy) implements CustomPacketPayload {

    public static final Type<PacketSyncContents> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(MagneticCellsMod.MODID, "sync_contents"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncContents> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, PacketSyncContents::energy,
            ByteBufCodecs.INT, PacketSyncContents::maxEnergy,
            PacketSyncContents::new
        );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacketSyncContents pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof MagneticCellMenu menu) {
                menu.clientEnergy    = pkt.energy;
                menu.clientMaxEnergy = pkt.maxEnergy;
            }
        });
    }
}
