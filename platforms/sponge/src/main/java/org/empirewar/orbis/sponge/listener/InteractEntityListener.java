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
package org.empirewar.orbis.sponge.listener;

import net.kyori.adventure.key.Key;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.vehicle.Vehicle;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;

public final class InteractEntityListener {

    private final Orbis orbis;

    public InteractEntityListener(Orbis orbis) {
        this.orbis = orbis;
    }

    @Listener(order = Order.EARLY)
    public void onAttackDirect(InteractEntityEvent.Primary event) {
        final Entity attacked = event.entity();
        final ServerPlayer player = event.cause().first(ServerPlayer.class).orElse(null);

        final RegionisedWorld world =
                orbis.getRegionisedWorld(attacked.serverLocation().world().key());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> positionQuery =
                world.query(RegionQuery.Position.at(
                        attacked.position().x(),
                        attacked.position().y(),
                        attacked.position().z()));
        final RegistryRegionFlag<Boolean> flag =
                switch (attacked) {
                    case Vehicle ignored -> DefaultFlags.CAN_DESTROY_VEHICLE;
                    case Painting ignored -> DefaultFlags.CAN_DESTROY_PAINTING;
                    case ItemFrame ignored -> DefaultFlags.CAN_DESTROY_ITEM_FRAME;
                    default -> null;
                };

        if (flag != null) {
            final RegionQuery.Flag.Builder<Boolean> flagQueryBuilder =
                    RegionQuery.Flag.builder(flag);
            if (player != null) flagQueryBuilder.player(player.uniqueId());
            if (!positionQuery.query(flagQueryBuilder).result().orElse(true)) {
                event.setCancelled(true);
            }
            return;
        }

        if (!(attacked instanceof Living)) return;

        // Check PvP flag for players via AttackEntityEvent instead if cancel-pvp-hit-sounds = false
        if (attacked instanceof ServerPlayer) {
            if (orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)
                    && shouldPreventEntityAction(attacked, player, DefaultFlags.CAN_PVP)) {
                event.setCancelled(true);
            }
            return;
        }

        final RegionQuery.Flag.Builder<List<Key>> flagQueryBuilder =
                RegionQuery.Flag.builder(DefaultFlags.DAMAGEABLE_ENTITIES);
        if (player != null) flagQueryBuilder.player(player.uniqueId());
        final List<Key> damageable =
                positionQuery.query(flagQueryBuilder).result().orElse(null);
        if (damageable == null) return;

        if (!damageable.contains(attacked.type().key(RegistryTypes.ENTITY_TYPE))) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
    public void onAttack(AttackEntityEvent.Post event, @First ServerPlayer attacker) {
        if (orbis.config().node("cancel-pvp-hit-sounds").getBoolean(true)) return;

        final Entity attacked = event.entity();
        if (shouldPreventEntityAction(attacked, attacker, DefaultFlags.CAN_PVP)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
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

    @Listener(order = Order.EARLY)
    public void onMobDirectDamage(DamageEntityEvent event, @First Living damager) {
        final Entity entity = event.entity();
        if (!(entity instanceof ServerPlayer)) return;

        if (!(damager instanceof ServerPlayer)) {
            if (shouldPreventEntityAction(entity, DefaultFlags.CAN_TAKE_MOB_DAMAGE_SOURCES)) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onDrop(ChangeInventoryEvent.Drop event, @First ServerPlayer player) {
        if (shouldPreventEntityAction(player, DefaultFlags.CAN_DROP_ITEM)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
    public void onPickup(ChangeInventoryEvent.Pickup event, @Root ServerPlayer player) {
        if (shouldPreventEntityAction(player, DefaultFlags.CAN_PICKUP_ITEM)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
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

    private boolean shouldPreventEntityAction(Entity entity, RegistryRegionFlag<Boolean> flag) {
        return shouldPreventEntityAction(entity, entity, flag);
    }

    private boolean shouldPreventEntityAction(
            Entity entity, @Nullable Entity player, RegistryRegionFlag<Boolean> flag) {
        final RegionisedWorld world =
                orbis.getRegionisedWorld(entity.serverLocation().world().key());
        if (world == null) return false;

        RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) {
            builder.player(player.uniqueId());
        }

        final Vector3d location = entity.position();
        return !world.query(RegionQuery.Position.builder()
                        .position(location.x(), location.y(), location.z()))
                .query(builder)
                .result()
                .orElse(true);
    }
}
