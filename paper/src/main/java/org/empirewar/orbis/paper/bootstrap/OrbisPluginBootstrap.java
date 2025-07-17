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
        new BukkitCommands<>(manager);
    }
}
