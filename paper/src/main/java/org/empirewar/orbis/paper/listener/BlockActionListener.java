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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.paper.OrbisPaper;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3d;

public record BlockActionListener(OrbisPaper plugin) implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (shouldPreventBlockAction(block, DefaultFlags.CAN_BREAK)) {
            System.out.println("cannot break!");
            event.getPlayer()
                    .sendMessage(Component.text("Hey!", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.space())
                            .append(Component.text(
                                    "Sorry, but you can't break that here.", NamedTextColor.GRAY)));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();
        if (shouldPreventBlockAction(block, DefaultFlags.CAN_PLACE)) {
            event.setCancelled(true);
            event.setBuild(false);
        }
    }

    private boolean shouldPreventBlockAction(Block block, RegionFlag<Boolean> flag) {
        final Vector3d pos = new Vector3d(block.getX(), block.getY(), block.getZ());
        final RegionisedWorld world = plugin.getRegionisedWorld(block.getWorld().getUID());
        System.out.println("regions at pos: "
                + world.query(RegionQuery.Position.builder().position(pos)).result());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(RegionQuery.Flag.builder(flag))
                .result()
                .orElse(true);
        return !canAct;
    }
}
