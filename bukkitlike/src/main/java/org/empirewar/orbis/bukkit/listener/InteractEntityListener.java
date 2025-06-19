/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.empirewar.orbis.bukkit.OrbisBukkit;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;

public abstract class InteractEntityListener implements Listener {

    protected final OrbisBukkit orbis;

    public InteractEntityListener(OrbisBukkit orbis) {
        this.orbis = orbis;
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && shouldPreventEntityAction(entity, DefaultFlags.FALL_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobDirectDamage(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        final Entity damager = event.getDamager();
        if (!(entity instanceof Player)) return;

        if (!(damager instanceof Player)) {
            if (shouldPreventEntityAction(entity, DefaultFlags.CAN_TAKE_MOB_DAMAGE_SOURCES)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (shouldPreventEntityAction(player, DefaultFlags.CAN_DROP_ITEM)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (shouldPreventEntityAction(event.getEntity(), DefaultFlags.CAN_PICKUP_ITEM)) {
            event.setCancelled(true);
        }
    }

    protected boolean shouldPreventEntityAction(Entity entity, RegistryRegionFlag<Boolean> flag) {
        final Location location = entity.getLocation();
        return !orbis.getRegionisedWorld(orbis.adventureKey(entity.getWorld()))
                .query(RegionQuery.Position.builder()
                        .position(location.getX(), location.getY(), location.getZ()))
                .query(RegionQuery.Flag.builder(flag).player(entity.getUniqueId()))
                .result()
                .orElse(true);
    }
}
