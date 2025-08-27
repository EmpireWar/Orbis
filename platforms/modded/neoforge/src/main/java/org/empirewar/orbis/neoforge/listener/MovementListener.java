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
package org.empirewar.orbis.neoforge.listener;

import net.kyori.adventure.key.Keyed;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.neoforge.access.ServerPlayerDuck;
import org.empirewar.orbis.neoforge.api.event.RegionEnterEvent;
import org.empirewar.orbis.neoforge.api.event.RegionLeaveEvent;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;

import java.util.Set;

public final class MovementListener {

    private final Orbis orbis;

    public MovementListener(Orbis orbis) {
        this.orbis = orbis;
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        final RegionisedWorld world =
                orbis.getRegionisedWorld(((Keyed) player.level().dimension()).key());
        final boolean canMove = world.query(RegionQuery.Position.builder()
                        .position(event.getTargetX(), event.getTargetY(), event.getTargetZ()))
                .query(RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER).player(player.getUUID()))
                .result()
                .orElse(true);
        if (!canMove) {
            event.setCanceled(true);
        } else if (!event.isCanceled()) {
            ((ServerPlayerDuck) player).orbis$setLastTickPosition(event.getTarget());
        }
    }

    @SubscribeEvent
    public void onMove(PlayerTickEvent.Post event) {
        final Player player = event.getEntity();
        final Vec3 to = player.position();
        final ServerPlayerDuck duck = (ServerPlayerDuck) player;
        final Vec3 from = duck.orbis$getLastTickPosition() == null
                ? player.oldPosition()
                : duck.orbis$getLastTickPosition();
        final RegionisedWorld world =
                orbis.getRegionisedWorld(((Keyed) player.level().dimension()).key());

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> toQuery =
                world.query(RegionQuery.Position.builder().position(to.x(), to.y(), to.z()));
        final boolean canMove = toQuery.query(
                        RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER).player(player.getUUID()))
                .result()
                .orElse(true);

        if (!canMove) {
            player.teleportTo(from.x, from.y, from.z);
            return;
        }

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> fromQuery =
                world.query(RegionQuery.Position.builder().position(from.x(), from.y(), from.z()));
        final Set<Region> toRegions = toQuery.result();
        final Set<Region> fromRegions = fromQuery.result();
        for (Region possiblyEntered : toRegions) {
            if (fromRegions.contains(possiblyEntered)) continue;
            NeoForge.EVENT_BUS.post(
                    new RegionEnterEvent(player, player.level(), to, world, possiblyEntered));
        }

        for (Region possiblyLeft : fromRegions) {
            if (toRegions.contains(possiblyLeft)) continue;
            NeoForge.EVENT_BUS.post(
                    new RegionLeaveEvent(player, player.level(), to, world, possiblyLeft));
        }
    }
}
