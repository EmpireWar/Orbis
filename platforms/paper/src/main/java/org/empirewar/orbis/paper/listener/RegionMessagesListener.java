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
package org.empirewar.orbis.paper.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.paper.OrbisPaperPlatform;
import org.empirewar.orbis.paper.api.event.RegionEnterEvent;
import org.empirewar.orbis.paper.api.event.RegionLeaveEvent;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;

public record RegionMessagesListener(OrbisPaperPlatform orbis) implements Listener {

    @EventHandler
    public void onEnter(RegionEnterEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        region.query(RegionQuery.Flag.builder(DefaultFlags.ENTRY_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));
    }

    @EventHandler
    public void onLeave(RegionLeaveEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        region.query(RegionQuery.Flag.builder(DefaultFlags.EXIT_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));
    }
}
