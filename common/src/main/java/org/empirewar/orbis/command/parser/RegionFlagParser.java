/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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

import net.kyori.adventure.key.Key;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.command.caption.OrbisCaptionKeys;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.registry.Registries;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.Optional;

public class RegionFlagParser<C>
        implements ArgumentParser<C, RegionFlag<?>>, BlockingSuggestionProvider.Strings<C> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull RegionFlag<?>> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput) {
        final String input = commandInput.peekString();
        System.out.println("asd");
        final Optional<RegionFlag<?>> regionFlag = Registries.FLAGS.get(Key.key(input));
        System.out.println("op: " + regionFlag);
        return regionFlag
                .<ArgumentParseResult<RegionFlag<?>>>map(f -> {
                    commandInput.readString();
                    return ArgumentParseResult.success(f);
                })
                .orElseGet(() -> ArgumentParseResult.failure(
                        new RegionFlagParserException(input, commandContext)));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> commandContext, @NonNull CommandInput input) {
        return Registries.FLAGS.getAll().stream().map(f -> f.key().asString()).toList();
    }

    public static final class RegionFlagParserException extends ParserException {

        public RegionFlagParserException(
                final @NonNull String input, final @NonNull CommandContext<?> context) {
            super(
                    RegionFlagParser.class,
                    context,
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND,
                    CaptionVariable.of("input", input));
        }
    }
}
