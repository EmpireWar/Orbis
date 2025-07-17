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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.empirewar.orbis.bukkit.OrbisBukkitPlatform;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;

public abstract class InteractEntityListener implements Listener {

    protected final OrbisBukkitPlatform<?> orbis;

    public InteractEntityListener(OrbisBukkitPlatform<?> orbis) {
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
