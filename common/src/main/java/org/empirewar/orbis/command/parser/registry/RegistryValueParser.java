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
