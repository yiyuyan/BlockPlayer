package cn.ksmcbrigade.wai.utils;

import net.minecraft.network.FriendlyByteBuf;

public record NetworkGetMessage() {
    public static void encode(NetworkGetMessage msg, FriendlyByteBuf buf){
    }

    public static NetworkGetMessage decode(FriendlyByteBuf buf){
        return new NetworkGetMessage();
    }
}
