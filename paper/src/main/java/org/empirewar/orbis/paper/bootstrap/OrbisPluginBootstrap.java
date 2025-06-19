/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 EmpireWar
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
package org.empirewar.orbis.paper.bootstrap;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.empirewar.orbis.bukkit.command.BukkitCommands;
import org.empirewar.orbis.paper.session.PaperConsoleSession;
import org.empirewar.orbis.paper.session.PaperPlayerSession;
import org.empirewar.orbis.player.OrbisSession;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.paper.PaperCommandManager;

public class OrbisPluginBootstrap implements PluginBootstrap {

    private static BukkitCommands<PaperCommandManager<OrbisSession>> commands;

    @Override
    public void bootstrap(BootstrapContext bootstrapContext) {
        SenderMapper<CommandSourceStack, OrbisSession> mapper = SenderMapper.create(
                source -> {
                    if (source.getExecutor() instanceof Player player) {
                        return new PaperPlayerSession(player, source);
                    }
                    return new PaperConsoleSession(Bukkit.getConsoleSender(), source);
                },
                session -> {
                    if (session instanceof PaperPlayerSession paper) {
                        return paper.source();
                    } else {
                        return ((PaperConsoleSession) session).source();
                    }
                });

        PaperCommandManager<OrbisSession> manager = PaperCommandManager.builder(mapper)
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildBootstrapped(bootstrapContext);
        // Thanks to @metabrix and @TonytheMacaroni
        // https://discord.com/channels/766366162388123678/1170254709722984460/1252264381585035355
        manager.appendSuggestionMapper(suggestion -> {
            if (!(suggestion instanceof ComponentTooltipSuggestion componentTooltipSuggestion))
                return suggestion;

            return TooltipSuggestion.suggestion(
                    suggestion.suggestion(),
                    MessageComponentSerializer.message()
                            .serializeOrNull(componentTooltipSuggestion.tooltip()));
        });
        commands = new BukkitCommands<>(manager);
    }
}
