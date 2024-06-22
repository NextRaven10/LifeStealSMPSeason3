package com.github.tatercertified.lifesteal.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerManager.class)
public interface SaveHandlerAccessor {
    @Accessor()
    public PlayerSaveHandler getSaveHandler();
}
