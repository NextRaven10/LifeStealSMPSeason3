package com.github.tatercertified.lifesteal.item;

import com.github.tatercertified.lifesteal.Loader;
import com.github.tatercertified.lifesteal.util.ModelledPolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.Objects;

import static com.github.tatercertified.lifesteal.Loader.revival_block;

public class HeartItem extends ModelledPolymerItem {

    public HeartItem(Settings settings, PolymerModelData customModelData) {
        super(settings, customModelData);
    }

    Block block_from_config = Registries.BLOCK.get(Identifier.tryParse(revival_block));


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) {
            user.getStackInHand(hand).decrement(1);
            updateValueOf(user, world.getGameRules().getInt(Loader.HEARTBONUS), true);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (block_from_config == null) {
            return super.useOnBlock(context);
        }
        PlayerEntity player =  context.getPlayer();
        BlockPos pos = new BlockPos.Mutable(context.getBlockPos().getX(), context.getBlockPos().getY(), context.getBlockPos().getZ());
        String playername = context.getStack().getName().getString();
        if (player.isSneaking() && context.getWorld().getBlockState(pos).getBlock() == block_from_config) {
            if (!Objects.equals(playername, "Heart") && isAltar(context.getWorld(), pos)) {

                MinecraftServer server = context.getWorld().getServer();

                if (playername.equalsIgnoreCase(player.getDisplayName().getString())) {

                    if(!player.getWorld().getGameRules().getBoolean(Loader.GIFTHEARTS)) {
                        player.sendMessage(Text.of("Heart Gifting is Disabled"), true);
                        return super.useOnBlock(context);
                    }

                    if (player.getHealth() > 2) {
                        updateValueOf(player, -2, true);
                        context.getStack().increment(1);
                        player.sendMessage(Text.of("Traded 1 Heart"));
                    } else {
                        player.sendMessage(Text.of("Your Health is Too Low"), true);
                    }

                } else {
                    ServerPlayerEntity revived = server.getPlayerManager().getPlayer(playername);

                    if (revived != null && revived.isSpectator()) {
                        revive(revived, context);
                    } else if (server.getUserCache().findByName(playername).isPresent() && revived == null) {
                        revived = server.getPlayerManager().createPlayer(server.getUserCache().findByName(playername).get());
                        NbtCompound data = server.getPlayerManager().loadPlayerData(revived);
                        if (data != null && data.getInt("playerGameType") == 3) {
                            reviveOffline(revived, context, data);
                        } else {
                            if (data != null && data.getInt("playerGameType") != 3) {
                                player.sendMessage(Text.of(playername + " is still alive"), true);
                            } else {
                                player.sendMessage(Text.of(playername + " does not exist"), true);
                            }
                        }
                    } else {
                        if (revived != null && !revived.isSpectator()) {
                            player.sendMessage(Text.of(playername + " is still alive"), true);
                        } else {
                            player.sendMessage(Text.of(playername + " does not exist"), true);
                        }
                    }
                }
            }
        }
        return super.useOnBlock(context);
    }

    private static void revive(ServerPlayerEntity player, ItemUsageContext context) {
        player.teleport(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5);
        player.changeGameMode(GameMode.SURVIVAL);
        player.sendMessage(Text.of(context.getPlayer().getDisplayName().getString() + " has revived you!"));
        player.getWorld().playSound(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1, 1, true);
        updateValueOf(player, 1, true);
        context.getStack().decrement(1);
        context.getPlayer().sendMessage(Text.of("You revived " + player.getDisplayName().getString()), true);
    }

    private static void reviveOffline(ServerPlayerEntity player, ItemUsageContext context, NbtCompound data) {
        player.setPosition(context.getBlockPos().getX() + 0.5, context.getBlockPos().getY() + 1, context.getBlockPos().getZ() + 0.5);
        data.putInt("playerGameType", 0);
        player.setGameMode(data);
        updateValueOf(player, 1, false);
        data.putString("reviver", context.getPlayer().getName().getString());
        savePlayerData(player);
        if (context.getWorld().getGameRules().getBoolean(Loader.BANWHENMINHEALTH)) {
            context.getPlayer().getServer().getPlayerManager().getUserBanList().remove(player.getGameProfile());
        }
        context.getStack().decrement(1);
        context.getPlayer().sendMessage(Text.of("You revived " + player.getDisplayName().getString()), true);
    }


    //Thanks to PotatoBoy for helping me out!
    public static void savePlayerData(ServerPlayerEntity player) {
        File playerDataDir = player.getServer().getSavePath(WorldSavePath.PLAYERDATA).toFile();
        try {
            NbtCompound compoundTag = player.writeNbt(new NbtCompound());
            File file = File.createTempFile(player.getUuidAsString() + "-", ".dat", playerDataDir);
            NbtIo.writeCompressed(compoundTag, file);
            File file2 = new File(playerDataDir, player.getUuidAsString() + ".dat");
            File file3 = new File(playerDataDir, player.getUuidAsString() + ".dat_old");
            Util.backupAndReplace(file2, file, file3);
        } catch (Exception var6) {
            LogManager.getLogger().warn("Failed to save player data for {}", player.getName().getString());
        }
    }

    public static void updateValueOf(PlayerEntity of, float by, boolean online) {
        EntityAttributeInstance health = of.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        assert health != null;
        float oldHealth = (float) health.getValue();
        float newHealth = oldHealth + by;
        int maxHealth = of.getWorld().getGameRules().getInt(Loader.MAXPLAYERHEALTH);

        if(maxHealth > 0 && newHealth > maxHealth) {
            newHealth = (float) maxHealth;
        }
        health.setBaseValue(newHealth);
        if (online) {
            of.sendMessage(Text.of("Your max health is now " + newHealth), true);
            if (oldHealth == (float) maxHealth) {
                of.giveItemStack(new ItemStack(ModItems.HEART, 1));
                of.getInventory().updateItems();
                of.sendMessage(Text.of("You are already at the maximum amount of health!"), true);
            } else {
                of.setHealth(of.getHealth() + by);
                if (of.getHealth() > maxHealth) of.setHealth(maxHealth);
            }
        }
    }

    public static boolean isAltar(World world, BlockPos pos) {
        BlockState north = world.getBlockState(pos.north());
        BlockState east = world.getBlockState(pos.east());
        BlockState south = world.getBlockState(pos.south());
        BlockState west = world.getBlockState(pos.west());

        return north.isIn(BlockTags.CANDLES) && north.get(CandleBlock.LIT)
                && east.isIn(BlockTags.CANDLES) && east.get(CandleBlock.LIT)
                && south.isIn(BlockTags.CANDLES) && south.get(CandleBlock.LIT)
                && west.isIn(BlockTags.CANDLES) && west.get(CandleBlock.LIT);
    }
}
