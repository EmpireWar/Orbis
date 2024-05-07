/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
 * Copyright (C) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.empirewar.orbis.paper.listener;

import net.kyori.adventure.key.Key;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;

public record BlockActionListener(Orbis orbis) implements Listener {

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
        if (block instanceof Farmland) {
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
        final RegionisedWorld world = orbis.getRegionisedWorld(block.getWorld().key());
        final List<Key> growable = world.query(RegionQuery.Position.builder()
                        .position(block.getX(), block.getY(), block.getZ()))
                .query(RegionQuery.Flag.builder(DefaultFlags.GROWABLE_BLOCKS))
                .result()
                .orElse(null);
        if (growable == null) return;

        if (!growable.contains(block.getType().key())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event) {
        final Block block = event.getBlock();
        if (Tag.FIRE.isTagged(block.getType())
                && shouldPreventBlockAction(block, DefaultFlags.FIRE_SPREAD)) {
            event.setCancelled(true);
        }
    }

    private boolean shouldPreventBlockAction(@Nullable Block block, RegionFlag<Boolean> flag) {
        return shouldPreventBlockAction(block, null, flag);
    }

    // spotless:off
    private boolean shouldPreventBlockAction(@Nullable Block block, @Nullable Player player, RegionFlag<Boolean> flag) {
        // spotless:on
        if (block == null) return false;
        final Vector3d pos = new Vector3d(block.getX(), block.getY(), block.getZ());
        final RegionisedWorld world = orbis.getRegionisedWorld(block.getWorld().key());
        final RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) builder.player(player.getUniqueId());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(true);
        return !canAct;
    }
}
