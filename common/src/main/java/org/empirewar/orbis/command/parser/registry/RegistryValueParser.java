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
package org.empirewar.orbis.command.parser.registry;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.empirewar.orbis.command.caption.OrbisCaptionKeys;
import org.empirewar.orbis.registry.OrbisRegistry;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.Optional;
import java.util.stream.Collectors;

public final class RegistryValueParser<C, R, K>
        implements ArgumentParser<C, R>, BlockingSuggestionProvider.Strings<C> {

    private final OrbisRegistry<R, K> registry;
    private final RegistryMapper<String, K> keyMapper;

    public RegistryValueParser(OrbisRegistry<R, K> registry, RegistryMapper<String, K> keyMapper) {
        this.registry = registry;
        this.keyMapper = keyMapper;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull R> parse(
            @NonNull CommandContext<@NonNull C> commandContext,
            @NonNull CommandInput commandInput) {
        final String input = commandInput.peekString();

        final Optional<R> registered = registry.get(keyMapper.map(commandInput.peekString()));
        if (registered.isPresent()) {
            commandInput.readString();
            return ArgumentParseResult.success(registered.get());
        }

        return ArgumentParseResult.failure(
                new RegistryValueParserException(input, registry, commandContext));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull CommandContext<C> commandContext, @NonNull CommandInput input) {
        return registry.getKeys().stream().map(keyMapper::reverse).collect(Collectors.toSet());
    }

    public static final class RegistryValueParserException extends ParserException {

        private final String input;

        public RegistryValueParserException(
                final @NonNull String input,
                final @NonNull OrbisRegistry<?, ?> registry,
                final @NonNull CommandContext<?> context) {
            super(
                    RegistryValueParser.class,
                    context,
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_VALUE_NOT_FOUND,
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("registry", registry.key().asString()));
            this.input = input;
        }

        public @NonNull String getInput() {
            return this.input;
        }
    }
}
