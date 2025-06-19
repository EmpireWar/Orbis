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
package org.empirewar.orbis.member;

/**
 * Represents a member group that a {@link org.empirewar.orbis.flag.GroupedMutableRegionFlag} can affect.
 */
public enum FlagMemberGroup {
    /**
     * Represents a player that is not a member of a region.
     */
    NONMEMBER("Anyone that is not a direct member of the region."),
    /**
     * Represents a player that is a member of a region.
     */
    MEMBER("Represents anyone that is a member of the region.");

    private final String description;

    FlagMemberGroup(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
