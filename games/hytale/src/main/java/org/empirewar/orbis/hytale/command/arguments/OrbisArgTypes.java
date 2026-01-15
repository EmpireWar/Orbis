/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale.command.arguments;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;

import net.kyori.adventure.text.Component;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.command.parser.registry.RegistryMapper;
import org.empirewar.orbis.hytale.util.TextUtil;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.registry.OrbisRegistry;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jspecify.annotations.NonNull;

public final class OrbisArgTypes {

    public static final SingleArgumentType<Region> REGION = registryArgument(
            "<region>",
            "Region identifier",
            "spawn_region",
            OrbisRegistries.REGIONS,
            RegistryMapper.IDENTITY);

    public static final SingleArgumentType<RegionisedWorld> REGIONISED_WORLD =
            new SingleArgumentType<>("<world>", "Regionised world", "overworld") {

                @Override
                public RegionisedWorld parse(String input, ParseResult result) {
                    return OrbisAPI.get().getRegionisedWorlds().stream()
                            .filter(world -> world.worldId()
                                    .orElseThrow()
                                    .asString()
                                    .equalsIgnoreCase(input))
                            .findFirst()
                            .orElseGet(() -> {
                                result.fail(TextUtil.componentToHytaleMessage(Component.text(
                                        "Unknown world '" + input + "'", OrbisText.SECONDARY_RED)));
                                return null;
                            });
                }

                @Override
                public void suggest(
                        @NonNull CommandSender sender,
                        @NonNull String textAlreadyEntered,
                        int numParametersTyped,
                        @NonNull SuggestionResult suggestions) {
                    OrbisAPI.get().getRegionisedWorlds().stream()
                            .map(world -> world.worldId().orElseThrow().asString())
                            .forEach(suggestions::suggest);
                }
            };

    private static <T, K> SingleArgumentType<T> registryArgument(
            String name,
            String description,
            String example,
            OrbisRegistry<T, K> registry,
            RegistryMapper<String, K> mapper) {

        return new SingleArgumentType<>(name, description, example) {

            @Override
            public T parse(String input, ParseResult result) {
                return registry.get(mapper.map(input)).orElseGet(() -> {
                    result.fail(TextUtil.componentToHytaleMessage(Component.text(
                            "Could not find value '" + input + "' in registry '"
                                    + registry.key().asString() + "'",
                            OrbisText.SECONDARY_RED)));
                    return null;
                });
            }

            @Override
            public void suggest(
                    @NonNull CommandSender sender,
                    @NonNull String textAlreadyEntered,
                    int numParametersTyped,
                    @NonNull SuggestionResult result) {
                registry.getKeys().stream().map(mapper::reverse).forEach(result::suggest);
            }
        };
    }
}
