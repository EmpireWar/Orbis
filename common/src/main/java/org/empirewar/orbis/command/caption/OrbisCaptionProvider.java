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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

/**
 * Caption registry that uses bi-functions to produce messages
 *
 * @param <C> Command sender type
 */
public final class OrbisCaptionProvider<C> extends DelegatingCaptionProvider<C> {

    /**
     * Default caption for {@link OrbisCaptionKeys#ARGUMENT_PARSE_FAILURE_REGION_NOT_FOUND}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_REGION_NOT_FOUND =
            "Could not find region '<input>'";

    /**
     * Default caption for {@link OrbisCaptionKeys#ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND =
            "Could not find flag '<input>'";

    /**
     * Default caption for {@link OrbisCaptionKeys#ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND =
            "Could not find world '<input>'";

    /**
     * Default caption for {@link OrbisCaptionKeys#ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID =
            "Invalid flag value '<input>': <error>";

    /**
     * Default caption for {@link OrbisCaptionKeys#ARGUMENT_PARSE_FAILURE_AREA_TYPE_NOT_FOUND}.
     */
    public static final String ARGUMENT_PARSE_FAILURE_AREA_TYPE_NOT_FOUND =
            "Could not find area type '<input>'";

    private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
            .putCaption(
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGION_NOT_FOUND,
                    ARGUMENT_PARSE_FAILURE_REGION_NOT_FOUND)
            .putCaption(
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND,
                    ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND)
            .putCaption(
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND,
                    ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND)
            .putCaption(
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID,
                    ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID)
            .putCaption(
                    OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_AREA_TYPE_NOT_FOUND,
                    ARGUMENT_PARSE_FAILURE_AREA_TYPE_NOT_FOUND)
            .build();

    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
