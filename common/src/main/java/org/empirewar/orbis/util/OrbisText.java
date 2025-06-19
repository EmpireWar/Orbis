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
package org.empirewar.orbis.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public final class OrbisText {

    public static final TextColor MAIN = TextColor.fromHexString("#3FA489");
    public static final TextColor SECONDARY_RED = TextColor.color(244, 61, 61);
    public static final TextColor SECONDARY_ORANGE = TextColor.fromHexString("#F5AF2F");
    public static final TextColor EREBOR_GREEN = TextColor.color(4, 219, 100);

    public static final Component PREFIX = Component.text("[\uD83C\uDF0D] ", MAIN);
}
