/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2024 Empire War
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.empirewar.orbis.bukkit.listener;

import net.kyori.adventure.key.Key;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.empirewar.orbis.bukkit.OrbisBukkitPlatform;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.List;

public record BlockActionListener(OrbisBukkitPlatform<?> orbis) implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (shouldPreventBlockAction(block, event.getPlayer(), DefaultFlags.CAN_BREAK)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();
        if (shouldPreventBlockAction(block, event.getPlayer(), DefaultFlags.CAN_PLACE)) {
            event.setCancelled(true);
            event.setBuild(false);
        }
    }

    @EventHandler
    public void onAttemptAccess(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof BlockInventoryHolder blockHolder) {
            final Block block = blockHolder.getBlock();
            event.setCancelled(
                    shouldPreventBlockAction(block, DefaultFlags.BLOCK_INVENTORY_ACCESS));
        }
    }

    @EventHandler
    public void onRedstoneUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL)
            return;

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        if (block != null
                && (block.getType() == Material.FARMLAND
                        || block.getType() == Material.TURTLE_EGG)) {
            if (event.getAction() == Action.PHYSICAL
                    && shouldPreventBlockAction(block, player, DefaultFlags.BLOCK_TRAMPLE)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
            }
            return;
        }

        if (shouldPreventBlockAction(block, player, DefaultFlags.TRIGGER_REDSTONE)) {
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent event) {
        final Block block = event.getBlock();
        if (Tag.CORALS.isTagged(block.getType())) {
            event.setCancelled(shouldPreventBlockAction(block, DefaultFlags.CORAL_DECAY));
        }
    }

    @EventHandler
    public void onDecay(LeavesDecayEvent event) {
        if (shouldPreventBlockAction(event.getBlock(), DefaultFlags.LEAF_DECAY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGrow(BlockGrowEvent event) {
        final Block block = event.getBlock();
        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(block.getWorld()));
        final List<Key> growable = world.query(
                        RegionQuery.Position.at(block.getX(), block.getY(), block.getZ()))
                .query(RegionQuery.Flag.builder(DefaultFlags.GROWABLE_BLOCKS))
                .result()
                .orElse(null);
        if (growable == null) return;

        if (!growable.contains(orbis.adventureKey(block.getType()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        final Block block = event.getBlock();
        if (Tag.FIRE.isTagged(block.getType())
                && shouldPreventBlockAction(block, DefaultFlags.FIRE_SPREAD)) {
            event.setCancelled(true);
            return;
        }

        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(block.getWorld()));
        final List<Key> growable = world.query(
                        RegionQuery.Position.at(block.getX(), block.getY(), block.getZ()))
                .query(RegionQuery.Flag.builder(DefaultFlags.GROWABLE_BLOCKS))
                .result()
                .orElse(null);
        if (growable == null) return;

        if (!growable.contains(orbis.adventureKey(block.getType()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPush(BlockPistonExtendEvent event) {
        this.handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler
    public void onPull(BlockPistonExtendEvent event) {
        this.handlePistonEvent(event, event.getBlocks());
    }

    private void handlePistonEvent(BlockPistonEvent event, Collection<Block> blocks) {
        for (Block block : blocks) {
            if (shouldPreventBlockAction(block, DefaultFlags.ACTIVATE_PISTONS)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean shouldPreventBlockAction(
            @Nullable Block block, RegistryRegionFlag<Boolean> flag) {
        return shouldPreventBlockAction(block, null, flag);
    }

    // spotless:off
    private boolean shouldPreventBlockAction(@Nullable Block block, @Nullable Player player, RegistryRegionFlag<Boolean> flag) {
        // spotless:on
        if (block == null) return false;
        final Vector3d pos = new Vector3d(block.getX(), block.getY(), block.getZ());
        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(block.getWorld()));
        final RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) builder.player(player.getUniqueId());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(true);
        return !canAct;
    }
}
