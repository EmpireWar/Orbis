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
package org.empirewar.orbis.sponge.api;

import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.server.ServerLocation;

public class RegionLeaveEvent extends AbstractEvent {

    private final ServerPlayer player;
    private final ServerLocation location;
    private final RegionisedWorld world;
    private final Region region;
    private final Cause cause;

    public RegionLeaveEvent(
            ServerPlayer player,
            ServerLocation location,
            RegionisedWorld world,
            Region region,
            Cause cause) {
        this.player = player;
        this.location = location;
        this.world = world;
        this.region = region;
        this.cause = cause;
    }

    @Override
    public Cause cause() {
        return cause;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLocation getLocation() {
        return location;
    }

    public RegionisedWorld getWorld() {
        return world;
    }

    public Region getRegion() {
        return region;
    }
}
