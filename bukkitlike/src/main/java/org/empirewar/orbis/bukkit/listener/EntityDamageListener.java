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
package org.empirewar.orbis.bukkit.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.empirewar.orbis.bukkit.OrbisBukkit;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3d;

public record EntityDamageListener(OrbisBukkit orbis) implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(player.getWorld()));
        final Location location = player.getLocation();
        final Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        final RegionQuery.Flag.Builder<Boolean> builder =
                RegionQuery.Flag.builder(DefaultFlags.INVULNERABILITY);
        builder.player(player.getUniqueId());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(false);

        if (!canAct) {
            event.setCancelled(true);
        }
    }
}
