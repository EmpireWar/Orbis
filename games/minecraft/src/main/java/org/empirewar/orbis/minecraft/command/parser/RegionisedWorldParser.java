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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.minecraft.command.caption.OrbisCaptionKeys;
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
            if (regionisedWorld.worldId().orElseThrow().asString().equalsIgnoreCase(input)) {
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
                .map(rw -> rw.worldId().orElseThrow().asString())
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
