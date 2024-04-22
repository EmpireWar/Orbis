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
package org.empirewar.orbis.sponge.listener;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.vehicle.Vehicle;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.List;

public final class InteractEntityListener {

    private final Orbis orbis;

    public InteractEntityListener(Orbis orbis) {
        this.orbis = orbis;
    }

    @Listener
    public void onAttackDirect(InteractEntityEvent.Primary event) {
        final Entity attacked = event.entity();
        final RegionisedWorld world =
                orbis.getRegionisedWorld(attacked.serverLocation().world().uniqueId());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> query =
                world.query(RegionQuery.Position.builder()
                        .position(
                                attacked.position().x(),
                                attacked.position().y(),
                                attacked.position().z()));
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

        if (!(attacked instanceof Living)) return;

        // Check PvP flag for players
        if (attacked instanceof ServerPlayer) {
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

        if (!damageable.contains(attacked.type().key(RegistryTypes.ENTITY_TYPE))) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onFallDamage(DamageEntityEvent event) {
        final Entity entity = event.entity();
        if (event.source() instanceof DamageSource damageSource) {
            final DamageType type = damageSource.type();
            if (type != DamageTypes.FALL.get()) return;
            if (shouldPreventEntityAction(entity, DefaultFlags.FALL_DAMAGE)) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onMobDirectDamage(DamageEntityEvent event, @First Living damager) {
        final Entity entity = event.entity();
        if (!(entity instanceof ServerPlayer)) return;

        if (!(damager instanceof ServerPlayer)) {
            if (shouldPreventEntityAction(entity, DefaultFlags.CAN_TAKE_MOB_DAMAGE_SOURCES)) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onDrop(ChangeInventoryEvent.Drop event, @Root ServerPlayer player) {
        if (shouldPreventEntityAction(player, DefaultFlags.CAN_DROP_ITEM)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPickup(ChangeInventoryEvent.Pickup event, @Root ServerPlayer player) {
        if (shouldPreventEntityAction(player, DefaultFlags.CAN_PICKUP_ITEM)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onRotate(InteractEntityEvent.Secondary event) {
        if (!(event.entity() instanceof ItemFrame itemFrame)) return;
        final boolean rotate = !itemFrame.item().get().isEmpty();
        if (rotate) {
            if (shouldPreventEntityAction(itemFrame, DefaultFlags.ITEM_FRAME_ROTATE)) {
                event.setCancelled(true);
            }
        } else {
            if (shouldPreventEntityAction(itemFrame, DefaultFlags.ITEM_FRAME_ITEM_PLACE)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean shouldPreventEntityAction(Entity entity, RegionFlag<Boolean> flag) {
        return !orbis.getRegionisedWorld(entity.serverLocation().world().uniqueId())
                .query(RegionQuery.Position.builder()
                        .position(
                                entity.position().x(),
                                entity.position().y(),
                                entity.position().z()))
                .query(RegionQuery.Flag.builder(flag).player(entity.uniqueId()))
                .result()
                .orElse(true);
    }
}
