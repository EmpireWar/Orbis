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
package org.empirewar.orbis.command.caption;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

/**
 * Caption registry that uses bi-functions to produce messages
 *
 * @param <C> Command sender type
 */
public final class OrbisCaptionProvider<C> extends DelegatingCaptionProvider<C> {

    private static CaptionProvider<?> PROVIDER;

    public static void registerTranslations() {
         PROVIDER = CaptionProvider.constantProvider()
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND
                                                .key())))
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND.key())))
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID
                                                .key())))
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_VALUE_NOT_FOUND,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_VALUE_NOT_FOUND
                                                .key())))
                .build();
    }

    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
