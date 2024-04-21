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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.paper.api.event.RegionEnterEvent;
import org.empirewar.orbis.paper.api.event.RegionLeaveEvent;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;

import java.util.Optional;

public record RegionEnterLeaveListener(Orbis orbis) implements Listener {

    @EventHandler
    public void onEnter(RegionEnterEvent event) {
        final Player player = event.getPlayer();
        final Location location = event.getLocation();
        final RegionisedWorld world = event.getWorld();
        this.applyTimeChanges(player, world, location.x(), location.y(), location.getZ());
    }

    @EventHandler
    public void onLeave(RegionLeaveEvent event) {
        final Player player = event.getPlayer();
        final Location location = event.getLocation();
        final RegionisedWorld world = event.getWorld();
        this.applyTimeChanges(player, world, location.x(), location.y(), location.getZ());
    }

    private void applyTimeChanges(
            Player player, RegionisedWorld world, double x, double y, double z) {
        final Optional<Long> timeResult = world.query(
                        RegionQuery.Position.builder().position(x, y, z).build())
                .query(RegionQuery.Flag.builder(DefaultFlags.TIME))
                .result();
        timeResult.ifPresentOrElse(
                time -> player.setPlayerTime(time, false), player::resetPlayerTime);
    }
}
