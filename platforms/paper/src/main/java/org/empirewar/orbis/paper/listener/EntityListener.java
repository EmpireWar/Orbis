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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.paper.OrbisPaperPlatform;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3d;

import java.util.List;

public class EntityListener implements Listener {

    protected final OrbisPaperPlatform<?> orbis;

    public EntityListener(OrbisPaperPlatform<?> orbis) {
        this.orbis = orbis;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(player.getWorld()));
        final Location location = player.getLocation();
        final Vector3d pos = new Vector3d(location.getX(), location.getY(), location.getZ());

        final RegionQuery.Flag.Builder<Boolean> builder =
                RegionQuery.Flag.builder(MinecraftFlags.INVULNERABILITY);
        builder.player(player.getUniqueId());
        final boolean canAct = world.query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(false);

        if (canAct) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && shouldPreventEntityAction(entity, MinecraftFlags.FALL_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobDirectDamage(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        final Entity damager = event.getDamager();
        if (!(entity instanceof Player)) return;

        if (!(damager instanceof Player)) {
            if (shouldPreventEntityAction(entity, MinecraftFlags.CAN_TAKE_MOB_DAMAGE_SOURCES)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (shouldPreventEntityAction(player, MinecraftFlags.CAN_DROP_ITEM)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (shouldPreventEntityAction(event.getEntity(), MinecraftFlags.CAN_PICKUP_ITEM)) {
            event.setCancelled(true);
        }
    }

    protected boolean shouldPreventEntityAction(Entity entity, RegistryRegionFlag<Boolean> flag) {
        return shouldPreventEntityAction(entity, entity, flag);
    }

    protected boolean shouldPreventEntityAction(
            Entity entity, @Nullable Entity player, RegistryRegionFlag<Boolean> flag) {
        final RegionisedWorld world =
                orbis.getRegionisedWorld(orbis.adventureKey(entity.getWorld()));
        if (world == null) return false;

        RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) {
            builder.player(player.getUniqueId());
        }

        final Location location = entity.getLocation();
        return !world.query(RegionQuery.Position.builder()
                        .position(location.getX(), location.getY(), location.getZ()))
                .query(builder)
                .result()
                .orElse(true);
    }

    @EventHandler
    public void onAttackDirect(PrePlayerAttackEntityEvent event) {
        if (!event.willAttack()) return;

        final Entity attacked = event.getAttacked();
        final Player attacker = event.getPlayer();

        final RegionisedWorld world =
                orbis.getRegionisedWorld(attacked.getWorld().key());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> positionQuery = world.query(
                RegionQuery.Position.at(attacked.getX(), attacked.getY(), attacked.getZ()));
        final RegistryRegionFlag<Boolean> flag =
                switch (attacked) {
                    case Vehicle ignored -> MinecraftFlags.CAN_DESTROY_VEHICLE;
                    case Painting ignored -> MinecraftFlags.CAN_DESTROY_PAINTING;
                    case ItemFrame ignored -> MinecraftFlags.CAN_DESTROY_ITEM_FRAME;
                    default -> null;
                };

        if (flag != null) {
            final RegionQuery.Flag.Builder<Boolean> flagQueryBuilder =
                    RegionQuery.Flag.builder(flag).player(attacker.getUniqueId());
            if (!positionQuery.query(flagQueryBuilder).result().orElse(true)) {
                event.setCancelled(true);
            }
            return;
        }

        if (!attacked.getType().isAlive()) return;

        // Check PvP flag for players via AttackEntityEvent instead if cancel-pvp-hit-sounds = false
        if (attacked instanceof Player) {
            if (orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)
                    && shouldPreventEntityAction(attacked, attacker, MinecraftFlags.CAN_PVP)) {
                event.setCancelled(true);
            }
            return;
        }

        final RegionQuery.Flag.Builder<List<Key>> flagQueryBuilder = RegionQuery.Flag.builder(
                        MinecraftFlags.DAMAGEABLE_ENTITIES)
                .player(attacker.getUniqueId());
        final List<Key> damageable =
                positionQuery.query(flagQueryBuilder).result().orElse(null);
        if (damageable == null) return;

        if (!damageable.contains(attacked.getType().key())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)) return;

        final Entity attacked = event.getEntity();
        if (attacked instanceof Player
                && event.getDamager() instanceof Player
                && shouldPreventEntityAction(
                        attacked, event.getDamager(), MinecraftFlags.CAN_PVP)) {
            event.setCancelled(true);
        }
    }

    // I added this event to Paper back in 2021, now it helps me again :)
    @EventHandler
    public void onRotate(PlayerItemFrameChangeEvent event) {
        if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE) {
            if (shouldPreventEntityAction(
                    event.getItemFrame(), event.getPlayer(), MinecraftFlags.ITEM_FRAME_ROTATE)) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE) {
            if (shouldPreventEntityAction(
                    event.getItemFrame(),
                    event.getPlayer(),
                    MinecraftFlags.ITEM_FRAME_ITEM_PLACE)) {
                event.setCancelled(true);
            }
        }
    }
}
