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

public record RegionTimeListener(Orbis orbis) implements Listener {

    @EventHandler
    public void onEnter(RegionEnterEvent event) {
        final Player player = event.getPlayer();
        final Location location = event.getLocation();
        final RegionisedWorld world = event.getWorld();
        this.applyTimeChanges(player, world, location.getX(), location.getY(), location.getZ());
    }

    @EventHandler
    public void onLeave(RegionLeaveEvent event) {
        final Player player = event.getPlayer();
        final Location location = event.getLocation();
        final RegionisedWorld world = event.getWorld();
        this.applyTimeChanges(player, world, location.getX(), location.getY(), location.getZ());
    }

    private void applyTimeChanges(
            Player player, RegionisedWorld world, double x, double y, double z) {
        final Optional<Long> timeResult = world.query(
                        RegionQuery.Position.at(x, y, z).build())
                .query(RegionQuery.Flag.builder(DefaultFlags.TIME).player(player.getUniqueId()))
                .result();
        timeResult.ifPresentOrElse(
                time -> player.setPlayerTime(time, false), player::resetPlayerTime);
    }
}
