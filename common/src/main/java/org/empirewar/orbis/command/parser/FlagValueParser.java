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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

import io.leangen.geantyref.TypeToken;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.command.caption.OrbisCaptionKeys;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.flag.value.FlagValue;
import org.empirewar.orbis.flag.value.FlagValueParseResult;
import org.empirewar.orbis.serialization.FlagValueAdapter;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public record FlagValueParser<C>(CommandManager<?> manager)
        implements ArgumentParser.FutureArgumentParser<C, FlagValue<?>>, SuggestionProvider<C> {

    @Override
    public @NonNull CompletableFuture<@NonNull ArgumentParseResult<FlagValue<?>>> parseFuture(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput) {
        final String input = this.parseGreedy(commandInput).parsedValue().orElseThrow();
        final RegionFlag<?> flag = commandContext.get("flag");
        // spotless:off
        // Take in the string as a JSON representation, parse it for primitives, then pass it into the codec
        // TODO: probably wise to protect against bad input, although this should only ever run with admins
        // spotless:on
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(FlagValueParseResult.class, new FlagValueAdapter<>(flag))
                .create();

        try {
            final FlagValueParseResult parsed = gson.fromJson(input, FlagValueParseResult.class);

            final Either<?, ? extends DataResult.PartialResult<?>> get = parsed.result();
            if (get.left().isEmpty()) {
                return ArgumentParseResult.failureFuture(new FlagValueParserException(
                        input, get.right().orElseThrow().message(), commandContext));
            }

            return ArgumentParseResult.successFuture(new FlagValue<>(get.left().get()));
        } catch (JsonSyntaxException e) {
            return ArgumentParseResult.failureFuture(
                    new FlagValueParserException(input, e.getMessage(), commandContext));
        }
    }

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>>
            suggestionsFuture(@NonNull CommandContext<C> context, @NonNull CommandInput input) {
        final RegionFlag<?> flag = context.get("flag");
        // Try to find a valid parser for this. Very cursed but works epic.
        final Object defaultValue = flag.getDefaultValue();
        final ParserRegistry<C> parserRegistry = (ParserRegistry<C>) manager.parserRegistry();
        final Optional<? extends ArgumentParser<C, ?>> parser = parserRegistry.createParser(
                TypeToken.get(defaultValue.getClass()), ParserParameters.empty());
        if (parser.isPresent()) {
            return parser.get().suggestionProvider().suggestionsFuture(context, input);
        }
        return CompletableFuture.completedFuture(List.of());
    }

    private static final Pattern FLAG_PATTERN =
            Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");

    private @NonNull ArgumentParseResult<String> parseGreedy(
            final @NonNull CommandInput commandInput) {
        final int size = commandInput.remainingTokens();
        final StringJoiner stringJoiner = new StringJoiner(" ");

        for (int i = 0; i < size; i++) {
            final String string = commandInput.peekString();

            if (string.isEmpty()) {
                break;
            }

            // The pattern requires a leading space.
            if (FLAG_PATTERN.matcher(string).matches()) {
                break;
            }

            stringJoiner.add(
                    commandInput.readStringSkipWhitespace(false /* preserveSingleSpace */));
        }

        return ArgumentParseResult.success(stringJoiner.toString());
    }

    public static final class FlagValueParserException extends ParserException {

        public FlagValueParserException(
                final @NonNull String input,
                String error,
                final @NonNull CommandContext<?> context) {
            super(
                    FlagValueParser.class,
                    context,
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID,
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("error", error));
        }
    }
}
