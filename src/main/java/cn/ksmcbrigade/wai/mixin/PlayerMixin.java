package cn.ksmcbrigade.wai.mixin;

import cn.ksmcbrigade.wai.utils.BlockInfo;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements BlockInfo {

    @Unique
    private FallingBlockEntity entity = null;

    @Override
    public void set(FallingBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public FallingBlockEntity get() {
        return this.entity;
    }
}
