package cn.ksmcbrigade.wai.utils;

import cn.ksmcbrigade.wai.client.BPClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record NetworkMessage(int player, ResourceLocation block) {
    public static void encode(NetworkMessage msg, FriendlyByteBuf buf){
        buf.writeInt(msg.player);
        buf.writeResourceLocation(msg.block);
    }

    public static NetworkMessage decode(FriendlyByteBuf buf){
        return new NetworkMessage(buf.readInt(),buf.readResourceLocation());
    }

    public static void handle(NetworkMessage msg, Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()-> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->BPClient.handle(msg, context.get())));
        context.get().setPacketHandled(true);
    }
}
