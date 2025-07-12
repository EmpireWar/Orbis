/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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
package org.empirewar.orbis.fabric.command;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;

import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.command.parser.RegistryValueParser;
import org.empirewar.orbis.fabric.OrbisFabric;
import org.empirewar.orbis.fabric.session.FabricConsoleSession;
import org.empirewar.orbis.fabric.session.FabricPlayerSession;
import org.empirewar.orbis.player.OrbisSession;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.setting.ManagerSetting;

public final class FabricCommands {

    private final FabricServerCommandManager<OrbisSession> manager;

    public FabricCommands(OrbisFabric mod) {
        this.manager = new FabricServerCommandManager<>(
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.create(
                        sender -> {
                            if (sender.getPlayer() instanceof ServerPlayer player) {
                                return new FabricPlayerSession(player, sender);
                            }
                            return new FabricConsoleSession(sender);
                        },
                        session -> {
                            if (session instanceof FabricPlayerSession playerSession) {
                                return playerSession.getCause();
                            }

                            return ((FabricConsoleSession) session).cause();
                        }));

        manager.appendSuggestionMapper(suggestion -> {
            if (!(suggestion instanceof ComponentTooltipSuggestion componentTooltipSuggestion))
                return suggestion;

            return TooltipSuggestion.suggestion(
                    suggestion.suggestion(),
                    NonWrappingComponentSerializer.INSTANCE.serialize(
                            componentTooltipSuggestion.tooltip()));
        });

        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true);

        this.mapBrigadierArguments();
        this.registerCommands(mod);
    }

    private void mapBrigadierArguments() {
        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegionFlagParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegionisedWorldParser<OrbisSession>>() {},
                        configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<FlagValueParser<OrbisSession>>() {}, configurer -> configurer
                                .to(parser -> MessageArgument.message())
                                .cloudSuggestions());

        manager.brigadierManager()
                .registerMapping(
                        new TypeToken<RegistryValueParser<OrbisSession, ?, ?>>() {},
                        configurer -> configurer
                                .to(parser -> ResourceLocationArgument.id())
                                .cloudSuggestions());
    }

    private void registerCommands(OrbisFabric mod) {
        new CommonCommands(manager);
    }
}
