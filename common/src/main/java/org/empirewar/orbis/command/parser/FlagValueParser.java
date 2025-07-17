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
package org.empirewar.orbis.command.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;

import io.leangen.geantyref.TypeToken;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.command.caption.OrbisCaptionKeys;
import org.empirewar.orbis.flag.RegistryRegionFlag;
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
        final RegistryRegionFlag<?> flag = commandContext.get("flag");
        // spotless:off
        // Take in the string as a JSON representation, parse it for primitives, then pass it into the codec
        // TODO: probably wise to protect against bad input, although this should only ever run with admins
        // spotless:on
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(FlagValueParseResult.class, new FlagValueAdapter<>(flag))
                .create();

        try {
            final FlagValueParseResult parsed = gson.fromJson(input, FlagValueParseResult.class);

            final DataResult<?> get = parsed.result();
            if (get.isError()) {
                return ArgumentParseResult.failureFuture(new FlagValueParserException(
                        input, get.error().orElseThrow().message(), commandContext));
            }

            return ArgumentParseResult.successFuture(
                    new FlagValue<>(get.result().orElseThrow()));
        } catch (JsonSyntaxException e) {
            return ArgumentParseResult.failureFuture(
                    new FlagValueParserException(input, e.getMessage(), commandContext));
        }
    }

    @Override
    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>>
            suggestionsFuture(@NonNull CommandContext<C> context, @NonNull CommandInput input) {
        final RegistryRegionFlag<?> flag = context.get("flag");
        // Try to find a valid parser for this. Very cursed but works epic.
        final ParserRegistry<C> parserRegistry = (ParserRegistry<C>) manager.parserRegistry();
        final Optional<? extends ArgumentParser<C, ?>> parser = parserRegistry.createParser(
                TypeToken.get(flag.defaultValueType()), ParserParameters.empty());
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
