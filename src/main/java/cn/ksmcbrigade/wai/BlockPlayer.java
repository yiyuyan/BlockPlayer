package cn.ksmcbrigade.wai;

import cn.ksmcbrigade.wai.utils.BlockInfo;
import cn.ksmcbrigade.wai.utils.NetworkGetMessage;
import cn.ksmcbrigade.wai.utils.NetworkMessage;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Objects;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BlockPlayer.MODID)
@Mod.EventBusSubscriber(modid = BlockPlayer.MODID)
public class BlockPlayer {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "wai";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID,"sync"),()->"1",(a)->true,(b)->true);
    public static SimpleChannel client_channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID,"client_sync"),()->"1",(a)->true,(b)->true);

    public BlockPlayer() {
        channel.registerMessage(0, NetworkMessage.class,NetworkMessage::encode,NetworkMessage::decode,NetworkMessage::handle);
        client_channel.registerMessage(0, NetworkGetMessage.class,NetworkGetMessage::encode,NetworkGetMessage::decode,(msg,context)->{
            context.get().enqueueWork(()->{
                DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER,()->()->{
                    LOGGER.info("Client sync.");
                    if(context.get().getSender()==null) return;
                    context.get().getSender().serverLevel().players().stream().filter(player -> ((BlockInfo)player).get()!=null).forEach(player -> {
                        channel.sendTo(new NetworkMessage(player.getId(),ForgeRegistries.BLOCKS.getKey(((BlockInfo)player).get().getBlockState().getBlock())), Objects.requireNonNull(context.get().getSender()).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    });
                });
                context.get().setPacketHandled(true);
            });
        });
        LOGGER.info("BlockPlayer mod loaded.");
    }

    @SubscribeEvent
    public static void command(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("to-block").then(Commands.argument("block", BlockStateArgument.block(event.getBuildContext())).executes(commandContext -> {
            if(!commandContext.getSource().isPlayer()) return 1;
            FallingBlockEntity entity = EntityType.FALLING_BLOCK.create(commandContext.getSource().getLevel());
            assert entity != null;
            BlockState blockState = BlockStateArgument.getBlock(commandContext,"block").getState().getBlock().defaultBlockState();
            entity.blockState = blockState;
            ((BlockInfo) Objects.requireNonNull(commandContext.getSource().getPlayer())).set(entity);
            channel.send(PacketDistributor.ALL.noArg(),new NetworkMessage(commandContext.getSource().getPlayer().getId(),ForgeRegistries.BLOCKS.getKey(blockState.getBlock())));
            return 0;
        })));
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        BlockInfo info = (BlockInfo) event.player;
        if(info.get()!=null){
            info.get().setPos(event.player.getPosition(0));
            info.get().setStartPos(event.player.getOnPos());
        }
    }
}
