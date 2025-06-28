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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.command.caption.OrbisCaptionKeys;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Optional;

public final class RegionFlagParser<C>
        implements ArgumentParser<C, RegistryRegionFlag<?>>, BlockingSuggestionProvider<C> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull RegistryRegionFlag<?>> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput) {
        final String input = commandInput.peekString();
        final Optional<RegistryRegionFlag<?>> regionFlag =
                OrbisRegistries.FLAGS.get(Key.key(input));
        return regionFlag
                .<ArgumentParseResult<RegistryRegionFlag<?>>>map(f -> {
                    commandInput.readString();
                    return ArgumentParseResult.success(f);
                })
                .orElseGet(() -> ArgumentParseResult.failure(
                        new RegionFlagParserException(input, commandContext)));
    }

    @Override
    public @NonNull Iterable<? extends @NonNull Suggestion> suggestions(
            @NonNull CommandContext<C> context, @NonNull CommandInput input) {
        return OrbisRegistries.FLAGS.getAll().stream()
                .map(flag -> {
                    final Optional<String> description = flag.description();
                    if (description.isPresent()) {
                        return ComponentTooltipSuggestion.suggestion(
                                flag.key().asString(),
                                Component.text(description.get(), NamedTextColor.WHITE));
                    } else {
                        return Suggestion.suggestion(flag.key().asString());
                    }
                })
                .toList();
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
