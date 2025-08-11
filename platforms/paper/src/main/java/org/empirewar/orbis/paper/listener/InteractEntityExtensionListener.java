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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.empirewar.orbis.bukkit.OrbisBukkitPlatform;
import org.empirewar.orbis.bukkit.listener.InteractEntityListener;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;

import java.util.List;

public class InteractEntityExtensionListener extends InteractEntityListener {

    public InteractEntityExtensionListener(OrbisBukkitPlatform<?> orbis) {
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

        // Check PvP flag for players via AttackEntityEvent instead if cancel-pvp-hit-sounds = false
        if (attacked instanceof Player) {
            if (orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)
                    && shouldPreventEntityAction(attacked, event.getPlayer(), DefaultFlags.CAN_PVP)) {
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
    public void onAttack(EntityDamageByEntityEvent event) {
        if (orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)) return;

        final Entity attacked = event.getEntity();
        if (shouldPreventEntityAction(attacked, event.getDamager(), DefaultFlags.CAN_PVP)) {
            event.setCancelled(true);
        }
    }

    // I added this event to Paper back in 2021, now it helps me again :)
    @EventHandler
    public void onRotate(PlayerItemFrameChangeEvent event) {
        if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE) {
            if (shouldPreventEntityAction(event.getItemFrame(), event.getPlayer(), DefaultFlags.ITEM_FRAME_ROTATE)) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE) {
            if (shouldPreventEntityAction(
                    event.getItemFrame(), event.getPlayer(), DefaultFlags.ITEM_FRAME_ITEM_PLACE)) {
                event.setCancelled(true);
            }
        }
    }
}
