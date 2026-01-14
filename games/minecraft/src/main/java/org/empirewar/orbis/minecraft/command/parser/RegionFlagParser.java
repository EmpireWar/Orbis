/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2024 Empire War
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
package org.empirewar.orbis.minecraft.command.parser;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.minecraft.command.caption.OrbisCaptionKeys;
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
