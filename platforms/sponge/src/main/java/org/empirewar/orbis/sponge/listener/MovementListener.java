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

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.sponge.api.RegionEnterEvent;
import org.empirewar.orbis.sponge.api.RegionLeaveEvent;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;

import java.util.Set;

public final class MovementListener {

    private final Orbis orbis;

    public MovementListener(Orbis orbis) {
        this.orbis = orbis;
    }

    @Listener
    public void onMove(MoveEntityEvent event) {
        if (!(event.entity() instanceof ServerPlayer player)) return;
        final Vector3d to = event.destinationPosition();
        final Vector3d from = event.originalPosition();
        final RegionisedWorld world = orbis.getRegionisedWorld(player.world().key());

        if (event.context().get(EventContextKeys.MOVEMENT_TYPE).orElse(null)
                == MovementTypes.ENTITY_TELEPORT.get()) {
            final boolean canMove = world.query(
                            RegionQuery.Position.builder().position(to.x(), to.y(), to.z()))
                    .query(RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER)
                            .player(player.uniqueId()))
                    .result()
                    .orElse(true);
            if (!canMove) {
                event.setCancelled(true);
            }
            return;
        }

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> toQuery =
                world.query(RegionQuery.Position.builder().position(to.x(), to.y(), to.z()));
        final boolean canMove = toQuery.query(
                        RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER).player(player.uniqueId()))
                .result()
                .orElse(true);

        if (!canMove) {
            event.setCancelled(true);
            return;
        }

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> fromQuery =
                world.query(RegionQuery.Position.builder().position(from.x(), from.y(), from.z()));
        final Set<Region> toRegions = toQuery.result();
        final Set<Region> fromRegions = fromQuery.result();
        for (Region possiblyEntered : toRegions) {
            if (fromRegions.contains(possiblyEntered)) continue;
            Sponge.eventManager()
                    .post(new RegionEnterEvent(
                            player,
                            ServerLocation.of(player.world(), to),
                            world,
                            possiblyEntered,
                            event.cause()));
        }

        for (Region possiblyLeft : fromRegions) {
            if (toRegions.contains(possiblyLeft)) continue;
            Sponge.eventManager()
                    .post(new RegionLeaveEvent(
                            player,
                            ServerLocation.of(player.world(), to),
                            world,
                            possiblyLeft,
                            event.cause()));
        }
    }

    @Listener
    public void onFoodLevelChange(ChangeDataHolderEvent.ValueChange event) {
        if (!(event.targetHolder() instanceof ServerPlayer entity)) return;
        if (event.endResult().successfulData().stream()
                .noneMatch(immutable -> immutable.key() == Keys.FOOD_LEVEL)) return;
        if (event.cause().containsType(PluginContainer.class)) return;
        final RegionisedWorld world = orbis.getRegionisedWorld(entity.world().key());
        final boolean drain = world.query(RegionQuery.Position.builder()
                        .position(
                                entity.position().x(),
                                entity.position().y(),
                                entity.position().z()))
                .query(RegionQuery.Flag.builder(DefaultFlags.DRAIN_HUNGER)
                        .player(entity.uniqueId()))
                .result()
                .orElse(true);
        if (drain) return;
        event.setCancelled(true);
    }
}
