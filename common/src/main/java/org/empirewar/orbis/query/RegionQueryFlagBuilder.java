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
package org.empirewar.orbis.query;

import com.google.common.base.Preconditions;

import org.empirewar.orbis.flag.RegionFlag;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

non-sealed class RegionQueryFlagBuilder<FR> implements RegionQuery.Flag.Builder<FR> {

    private RegionFlag<FR> flag;
    private UUID player;

    RegionQueryFlagBuilder() {}

    @Override
    public RegionQuery.Flag<FR> build() {
        Preconditions.checkState(this.flag != null, "Flag cannot be empty");
        return new RegionQuery.Flag<>() {
            @Override
            public RegionFlag<FR> flag() {
                return flag;
            }

            @Override
            public Optional<UUID> player() {
                return Optional.ofNullable(player);
            }
        };
    }

    @Override
    public RegionQuery.Flag.Builder<FR> flag(RegionFlag<FR> flag) {
        Objects.requireNonNull(flag, "Flag cannot be null");
        this.flag = flag;
        return this;
    }

    @Override
    public RegionQuery.Flag.Builder<FR> player(UUID player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.player = player;
        return this;
    }
}
