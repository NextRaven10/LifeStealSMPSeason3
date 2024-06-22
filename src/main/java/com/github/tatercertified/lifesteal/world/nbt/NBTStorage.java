package com.github.tatercertified.lifesteal.world.nbt;

import com.github.tatercertified.lifesteal.Loader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class NBTStorage extends PersistentState {

    public NbtList deadPlayers = new NbtList();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.put("deadPlayers", deadPlayers);
        return nbt;
    }

    public static NBTStorage createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NBTStorage state = new NBTStorage();
        state.deadPlayers = nbt.getList("deadPlayers", NbtElement.COMPOUND_TYPE);
        return state;
    }

    private static final Type<NBTStorage> type = new Type<>(
            NBTStorage::new,
            NBTStorage::createFromNbt,
            null
    );

    public static NBTStorage getServerState(MinecraftServer server) {
        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.
        NBTStorage state = persistentStateManager.getOrCreate(type, Loader.MOD_ID);

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }
}
