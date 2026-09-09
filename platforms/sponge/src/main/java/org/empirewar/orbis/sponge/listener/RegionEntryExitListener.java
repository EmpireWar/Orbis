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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

public final class RegionEntryExitListener {

    private final OrbisSponge orbis;

    public RegionEntryExitListener(OrbisSponge orbis) {
        this.orbis = orbis;
    }

    @Listener
    public void onEnter(RegionEnterEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));

        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_PLAYER_COMMANDS))
                .result()
                .ifPresent(commands -> commands.forEach(cmd -> processCommand(cmd, player, false)));
        region.query(RegionQuery.Flag.builder(MinecraftFlags.ENTRY_CONSOLE_COMMANDS))
                .result()
                .ifPresent(commands -> commands.forEach(cmd -> processCommand(cmd, player, true)));
    }

    @Listener
    public void onLeave(RegionLeaveEvent event) {
        final Player player = event.getPlayer();
        final Region region = event.getRegion();
        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_MESSAGE))
                .result()
                .ifPresent(message -> player.sendMessage(orbis.miniMessage().deserialize(message)));

        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_PLAYER_COMMANDS))
                .result()
                .ifPresent(commands -> commands.forEach(cmd -> processCommand(cmd, player, false)));
        region.query(RegionQuery.Flag.builder(MinecraftFlags.EXIT_CONSOLE_COMMANDS))
                .result()
                .ifPresent(commands -> commands.forEach(cmd -> processCommand(cmd, player, true)));
    }

    private void processCommand(String cmd, Player player, boolean console) {
        cmd = cmd.replace("%player%", player.name())
                .replace("%uuid%", player.uniqueId().toString());

        try {
            if (console) {
                Sponge.server().commandManager().process(cmd);
            } else {
                final PermissionService permissionService =
                        Sponge.server().serviceProvider().permissionService();
                final Subject subject = permissionService
                        .userSubjects()
                        .subject(player.uniqueId().toString())
                        .map(s -> (Subject) s)
                        .orElse(permissionService.defaults());
                Sponge.server().commandManager().process(subject, player, cmd);
            }
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }
}
