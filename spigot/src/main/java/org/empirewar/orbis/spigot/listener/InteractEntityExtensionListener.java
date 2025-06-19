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
package org.empirewar.orbis.spigot.listener;

import net.kyori.adventure.key.Key;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.empirewar.orbis.bukkit.OrbisBukkit;
import org.empirewar.orbis.bukkit.listener.InteractEntityListener;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;

import java.util.List;

public class InteractEntityExtensionListener extends InteractEntityListener {

    public InteractEntityExtensionListener(OrbisBukkit orbis) {
        super(orbis);
    }

    @EventHandler
    public void onAttackDirect(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        final Entity attacked = event.getEntity();
        final Location location = attacked.getLocation();
        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(attacked.getWorld()));
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> query =
                world.query(RegionQuery.Position.builder()
                        .position(location.getX(), location.getY(), location.getZ()));
        if (attacked instanceof Vehicle) {
            if (!query.query(RegionQuery.Flag.builder(DefaultFlags.CAN_DESTROY_VEHICLE))
                    .result()
                    .orElse(true)) {
                event.setCancelled(true);
            }
            return;
        } else if (attacked instanceof Painting) {
            if (!query.query(RegionQuery.Flag.builder(DefaultFlags.CAN_DESTROY_PAINTING))
                    .result()
                    .orElse(true)) {
                event.setCancelled(true);
            }
            return;
        } else if (attacked instanceof ItemFrame) {
            if (!query.query(RegionQuery.Flag.builder(DefaultFlags.CAN_DESTROY_ITEM_FRAME))
                    .result()
                    .orElse(true)) {
                event.setCancelled(true);
            }
            return;
        }

        if (!attacked.getType().isAlive()) return;

        // Check PvP flag for players
        if (attacked instanceof Player) {
            if (shouldPreventEntityAction(attacked, DefaultFlags.CAN_PVP)) {
                event.setCancelled(true);
            }
            return;
        }

        final List<Key> damageable = query.query(
                        RegionQuery.Flag.builder(DefaultFlags.DAMAGEABLE_ENTITIES))
                .result()
                .orElse(null);
        if (damageable == null) return;

        if (!damageable.contains(orbis.adventureKey(attacked.getType()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRotate(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) return;
        // I'm not sure this is correct because Spigot doesn't have a proper event for this
        // Oh well, who's using Spigot in 2024 anyway? Unless you want 10000ms lag spikes on
        // teleports.
        if (shouldPreventEntityAction(itemFrame, DefaultFlags.ITEM_FRAME_ROTATE)
                || shouldPreventEntityAction(itemFrame, DefaultFlags.ITEM_FRAME_ITEM_PLACE)) {
            event.setCancelled(true);
        }
    }
}
