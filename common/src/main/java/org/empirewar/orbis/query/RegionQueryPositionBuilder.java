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

import org.joml.Vector3d;

import java.util.Objects;

non-sealed class RegionQueryPositionBuilder implements RegionQuery.Position.Builder {

    private Vector3d position;

    RegionQueryPositionBuilder() {
    }

    @Override
    public RegionQuery.Position build() {
        Preconditions.checkState(this.position != null, "Position cannot be empty");
        return () -> position;
    }

    @Override
    public RegionQuery.Position.Builder position(Vector3d position) {
        Objects.requireNonNull(position, "Position cannot be null");
        this.position = position;
        return this;
    }
}
