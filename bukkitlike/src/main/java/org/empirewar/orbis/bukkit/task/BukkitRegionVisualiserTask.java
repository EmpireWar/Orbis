/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.task.RegionVisualiserTaskBase;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.UUID;

public class BukkitRegionVisualiserTask extends RegionVisualiserTaskBase {

    public BukkitRegionVisualiserTask(OrbisPlatform platform) {
        super(platform);
    }

    @Override
    protected Vector3dc getPlayerPosition(UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        final Location location = player.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    @Override
    protected void showGreenParticle(UUID uuid, Vector3dc point) {
        final Player player = Bukkit.getPlayer(uuid);
        Location loc = new Location(player.getWorld(), point.x(), point.y(), point.z());
        player.spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);
    }

    @Override
    protected void showOrangeParticle(UUID uuid, Vector3dc point) {
        final Player player = Bukkit.getPlayer(uuid);
        Location loc = new Location(player.getWorld(), point.x(), point.y(), point.z());
        // Create an orange color (RGB: 255, 165, 0)
        Particle.DustOptions dustOptions =
                new Particle.DustOptions(Color.fromRGB(255, 165, 0), 2.0F);
        player.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dustOptions);
    }
}
