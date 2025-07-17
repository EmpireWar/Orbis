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
package org.empirewar.orbis.sponge.api;

import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.server.ServerLocation;

public class RegionEnterEvent extends AbstractEvent {

    private final ServerPlayer player;
    private final ServerLocation location;
    private final RegionisedWorld world;
    private final Region region;
    private final Cause cause;

    public RegionEnterEvent(
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
