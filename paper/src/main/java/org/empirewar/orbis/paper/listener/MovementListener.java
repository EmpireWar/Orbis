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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.paper.api.event.RegionEnterEvent;
import org.empirewar.orbis.paper.api.event.RegionLeaveEvent;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;

import java.util.Set;

public record MovementListener(Orbis orbis) implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;
        final Player player = event.getPlayer();
        final Location to = event.getTo();
        final Location from = event.getFrom();
        final RegionisedWorld world = orbis.getRegionisedWorld(to.getWorld().getUID());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> toQuery =
                world.query(RegionQuery.Position.builder().position(to.x(), to.y(), to.z()));
        final boolean canMove = toQuery.query(RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER)
                        .player(player.getUniqueId()))
                .result()
                .orElse(true);

        if (!canMove) {
            event.setTo(new Location(
                    from.getWorld(), from.x(), from.y(), from.z(), to.getYaw(), to.getPitch()));
            return;
        }

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> fromQuery =
                world.query(RegionQuery.Position.builder().position(from.x(), from.y(), from.z()));
        final Set<Region> toRegions = toQuery.result();
        final Set<Region> fromRegions = fromQuery.result();
        for (Region possiblyEntered : toRegions) {
            if (fromRegions.contains(possiblyEntered)) continue;
            Bukkit.getPluginManager()
                    .callEvent(new RegionEnterEvent(player, to, world, possiblyEntered));
        }

        for (Region possiblyLeft : fromRegions) {
            if (toRegions.contains(possiblyLeft)) continue;
            Bukkit.getPluginManager()
                    .callEvent(new RegionLeaveEvent(player, to, world, possiblyLeft));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();
        final RegionisedWorld world = orbis.getRegionisedWorld(to.getWorld().getUID());
        final boolean canMove = world.query(
                        RegionQuery.Position.builder().position(to.x(), to.y(), to.z()))
                .query(RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER)
                        .player(player.getUniqueId()))
                .result()
                .orElse(true);
        if (!canMove) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        final HumanEntity entity = event.getEntity();
        final RegionisedWorld world = orbis.getRegionisedWorld(entity.getWorld().getUID());
        final boolean drain = world.query(RegionQuery.Position.builder()
                        .position(entity.getX(), entity.getY(), entity.getZ()))
                .query(RegionQuery.Flag.builder(DefaultFlags.DRAIN_HUNGER)
                        .player(entity.getUniqueId()))
                .result()
                .orElse(true);
        if (drain) return;
        event.setCancelled(true);
    }
}
