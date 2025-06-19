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
package org.empirewar.orbis.paper.listener;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import net.kyori.adventure.key.Key;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
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
    public void onAttackDirect(PrePlayerAttackEntityEvent event) {
        if (!event.willAttack()) return;

        final Entity attacked = event.getAttacked();
        final RegionisedWorld world =
                orbis.getRegionisedWorld(attacked.getWorld().key());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> query =
                world.query(RegionQuery.Position.builder()
                        .position(attacked.getX(), attacked.getY(), attacked.getZ()));
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
            if (!orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)
                    && shouldPreventEntityAction(attacked, DefaultFlags.CAN_PVP)) {
                event.setCancelled(true);
            }
            return;
        }

        final List<Key> damageable = query.query(
                        RegionQuery.Flag.builder(DefaultFlags.DAMAGEABLE_ENTITIES))
                .result()
                .orElse(null);
        if (damageable == null) return;

        if (!damageable.contains(attacked.getType().key())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(PrePlayerAttackEntityEvent event) {
        if (!orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)) return;

        final Entity attacked = event.getAttacked();
        if (shouldPreventEntityAction(attacked, DefaultFlags.CAN_PVP)) {
            event.setCancelled(true);
        }
    }

    // I added this event to Paper back in 2021, now it helps me again :)
    @EventHandler
    public void onRotate(PlayerItemFrameChangeEvent event) {
        if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE) {
            if (shouldPreventEntityAction(event.getItemFrame(), DefaultFlags.ITEM_FRAME_ROTATE)) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE) {
            if (shouldPreventEntityAction(
                    event.getItemFrame(), DefaultFlags.ITEM_FRAME_ITEM_PLACE)) {
                event.setCancelled(true);
            }
        }
    }
}
