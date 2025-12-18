/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.paper.task;

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

public class PaperRegionVisualiserTask extends RegionVisualiserTaskBase {

    public PaperRegionVisualiserTask(OrbisPlatform platform) {
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
