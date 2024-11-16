package cn.ksmcbrigade.wai.utils;

import net.minecraft.world.entity.item.FallingBlockEntity;

public interface BlockInfo {
    void set(FallingBlockEntity entity);
    FallingBlockEntity get();
}
