package cn.ksmcbrigade.wai.client;

import cn.ksmcbrigade.wai.BlockPlayer;
import cn.ksmcbrigade.wai.utils.BlockInfo;
import cn.ksmcbrigade.wai.utils.NetworkGetMessage;
import cn.ksmcbrigade.wai.utils.NetworkMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = BlockPlayer.MODID,value = Dist.CLIENT)
public class BPClient {

    @SubscribeEvent
    public static void join(ClientPlayerNetworkEvent.LoggingIn event){
        if(Minecraft.getInstance().player!=null && ((BlockInfo)Minecraft.getInstance().player).get()!=null){
            ((BlockInfo)Minecraft.getInstance().player).set(null);
        }
        if(Minecraft.getInstance().getConnection()==null) return;

        BlockPlayer.client_channel.sendToServer(new NetworkGetMessage());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void render(RenderPlayerEvent.Pre event){
            if(event.getEntity()==null || ((BlockInfo)event.getEntity()).get()==null){
                return;
            }
            event.getPoseStack().pushPose();
            FallingBlockEntity entity = ((BlockInfo)event.getEntity()).get();
            EntityRenderer<? super Entity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            renderer.render(entity,
                    0,
                    event.getPartialTick(),
                    event.getPoseStack(),
                    event.getMultiBufferSource(),
                    event.getPackedLight());
            event.getPoseStack().popPose();
            event.setCanceled(true);
    }

    public static void handle(NetworkMessage msg, NetworkEvent.Context context){
            if(Minecraft.getInstance().level==null) return;
            Player P = null;
            for (Player entitiesOfClass : Minecraft.getInstance().level.players()) {
                if(entitiesOfClass.getId()==msg.player()){
                    P = entitiesOfClass;
                    break;
                }
            }
            if(P!=null){
                if(msg.empty()){
                    ((BlockInfo)P).set(null);
                    return;
                }
                Block block = ForgeRegistries.BLOCKS.getValue(msg.block());
                if(block==null){
                    return;
                }
                FallingBlockEntity fallingBlockEntity = EntityType.FALLING_BLOCK.create(Minecraft.getInstance().level);
                if (fallingBlockEntity != null) {
                    fallingBlockEntity.blockState = block.defaultBlockState();
                }
                ((BlockInfo)P).set(fallingBlockEntity);
            }
    }
}
