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
package org.empirewar.orbis.command.caption;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * {@link Caption} instances for messages in Orbis
 */
public final class OrbisCaptionKeys {

    private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();

    //	/**
    //	 * Variables: None
    //	 */

    //	 /**
    //	 * Variables: {input}, {min}, {max}
    //	 */

    /**
     * Variables: {input}
     */
    public static final Caption ARGUMENT_PARSE_FAILURE_REGION_NOT_FOUND =
            of("argument.parse.failure.region_not_found");

    public static final Caption ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND =
            of("argument.parse.failure.region_flag_not_found");

    public static final Caption ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND =
            of("argument.parse.failure.world_not_found");

    public static final Caption ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID =
            of("argument.parse.failure.flag_value_invalid");

    private OrbisCaptionKeys() {}

    private static @NonNull Caption of(final @NonNull String key) {
        final Caption caption = Caption.of(key);
        RECOGNIZED_CAPTIONS.add(caption);
        return caption;
    }

    /**
     * Get an immutable collection containing all standard caption keys
     *
     * @return Immutable collection of keys
     */
    public static @NonNull Collection<@NonNull Caption> getStandardCaptionKeys() {
        return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
    }
}
