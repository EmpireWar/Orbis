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

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.query.RegionQuery;

public record InteractEntityListener(Orbis orbis) implements Listener {

    @EventHandler
    public void onAttackDirect(PrePlayerAttackEntityEvent event) {
        if (!event.willAttack()) return;

        final Entity attacked = event.getAttacked();
        if (attacked instanceof Player) {
            event.setCancelled(shouldPreventEntityAction(attacked, DefaultFlags.CAN_PVP));
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            final Entity entity = event.getEntity();
            event.setCancelled(shouldPreventEntityAction(entity, DefaultFlags.FALL_DAMAGE));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        event.setCancelled(shouldPreventEntityAction(player, DefaultFlags.CAN_DROP_ITEMS));
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        event.setCancelled(
                shouldPreventEntityAction(event.getPlayer(), DefaultFlags.CAN_PICKUP_ITEMS));
    }

    private boolean shouldPreventEntityAction(Entity entity, RegionFlag<Boolean> flag) {
        return !orbis.getRegionisedWorld(entity.getWorld().getUID())
                .query(RegionQuery.Position.builder()
                        .position(entity.getX(), entity.getY(), entity.getZ()))
                .query(RegionQuery.Flag.builder(flag))
                .result()
                .orElse(true);
    }
}
