package com.github.tatercertified.lifesteal.world.nbt;

import com.github.tatercertified.lifesteal.Loader;
import net.minecraft.datafixer.DataFixTypes;
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

    public static NBTStorage createFromNbt(NbtCompound nbt) {
        NBTStorage state = new NBTStorage();
        state.deadPlayers = nbt.getList("deadPlayers", NbtElement.COMPOUND_TYPE);
        return state;
    }

    private static PersistentState.Type<NBTStorage> getPersistentStateType() {
        return new PersistentState.Type<>(NBTStorage::new, NBTStorage::createFromNbt, DataFixTypes.LEVEL);
    }

    public static NBTStorage getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                NBTStorage.getPersistentStateType(),
                Loader.MOD_ID);
    }
}
