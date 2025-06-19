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
package org.empirewar.orbis.command.parser;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.command.caption.OrbisCaptionKeys;
import org.empirewar.orbis.world.RegionisedWorld;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public final class RegionisedWorldParser<C>
        implements ArgumentParser<C, RegionisedWorld>, BlockingSuggestionProvider.Strings<C> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull RegionisedWorld> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput) {
        final String input = commandInput.peekString();

        for (RegionisedWorld regionisedWorld : OrbisAPI.get().getRegionisedWorlds()) {
            if (regionisedWorld.worldName().orElseThrow().equalsIgnoreCase(input)) {
                commandInput.readString();
                return ArgumentParseResult.success(regionisedWorld);
            }
        }

        return ArgumentParseResult.failure(
                new RegionisedWorldParserException(input, commandContext));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> commandContext, @NonNull CommandInput input) {
        return OrbisAPI.get().getRegionisedWorlds().stream()
                .map(rw -> rw.worldName().orElseThrow())
                .toList();
    }

    public static final class RegionisedWorldParserException extends ParserException {

        private final String input;

        public RegionisedWorldParserException(
                final @NonNull String input, final @NonNull CommandContext<?> context) {
            super(
                    RegionisedWorldParser.class,
                    context,
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND,
                    CaptionVariable.of("input", input));
            this.input = input;
        }

        public @NonNull String getInput() {
            return this.input;
        }
    }
}
