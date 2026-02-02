/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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

import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.sponge.OrbisSponge;
import org.empirewar.orbis.sponge.api.RegionEnterEvent;
import org.empirewar.orbis.sponge.api.RegionLeaveEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

public final class RegionMessagesListener {

    private final OrbisSponge orbis;

    public RegionMessagesListener(OrbisSponge orbis) {
        this.orbis = orbis;
    }

    @Listener
    public void onEnter(RegionEnterEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));
    }

    @Listener
    public void onLeave(RegionLeaveEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));
    }
}
