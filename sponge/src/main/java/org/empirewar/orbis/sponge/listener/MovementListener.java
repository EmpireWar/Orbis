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
        final RegionisedWorld world = orbis.getRegionisedWorld(player.world().uniqueId());

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
        final RegionisedWorld world = orbis.getRegionisedWorld(entity.world().uniqueId());
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
