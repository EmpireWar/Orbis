/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.paper.OrbisPaperPlatform;
import org.empirewar.orbis.paper.api.event.RegionEnterEvent;
import org.empirewar.orbis.paper.api.event.RegionLeaveEvent;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;

public record RegionEntryExitListener(OrbisPaperPlatform<?> orbis) implements Listener {

    @EventHandler
    public void onEnter(RegionEnterEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        this.applyTimeChanges(player, event.getWorld(), event.getLocation());
        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));

        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_PLAYER_COMMANDS))
                .result()
                .ifPresent(commands ->
                        commands.forEach(cmd -> this.processCommand(cmd, player, false)));
        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_CONSOLE_COMMANDS))
                .result()
                .ifPresent(commands ->
                        commands.forEach(cmd -> this.processCommand(cmd, player, true)));
    }

    @EventHandler
    public void onLeave(RegionLeaveEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        this.applyTimeChanges(player, event.getWorld(), event.getLocation());
        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));

        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_PLAYER_COMMANDS))
                .result()
                .ifPresent(commands ->
                        commands.forEach(cmd -> this.processCommand(cmd, player, false)));
        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_CONSOLE_COMMANDS))
                .result()
                .ifPresent(commands ->
                        commands.forEach(cmd -> this.processCommand(cmd, player, true)));
    }

    private void processCommand(String cmd, Player player, boolean console) {
        cmd = cmd.replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());

        if (console) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else {
            player.performCommand(cmd);
        }
    }

    private void applyTimeChanges(Player player, RegionisedWorld world, Location location) {
        final var timeResult = world.query(
                        RegionQuery.Position.at(location.getX(), location.getY(), location.getZ())
                                .build())
                .query(RegionQuery.Flag.builder(MinecraftFlags.TIME).player(player.getUniqueId()))
                .result();
        timeResult.ifPresentOrElse(
                time -> player.setPlayerTime(time, false), player::resetPlayerTime);
    }
}
