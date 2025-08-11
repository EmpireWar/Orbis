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
package org.empirewar.orbis.modded.command;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import org.empirewar.orbis.command.CommonCommands;
import org.empirewar.orbis.command.parser.FlagValueParser;
import org.empirewar.orbis.command.parser.RegionFlagParser;
import org.empirewar.orbis.command.parser.RegionisedWorldParser;
import org.empirewar.orbis.command.parser.registry.RegistryValueParser;
import org.empirewar.orbis.player.OrbisSession;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.setting.ManagerSetting;

public class ModdedCommands<
        M extends CommandManager<OrbisSession> & BrigadierManagerHolder<OrbisSession, ?>> {

    protected final M manager;

    public ModdedCommands(M manager) {
        this.manager = manager;

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
        new CommonCommands(manager);
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
}
