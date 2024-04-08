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

import java.util.Objects;

final class QueryResultBuilder<R, Q extends RegionQuery<R>>
        implements RegionQuery.Result.Builder<R, Q> {

    private R result;
    private Q query;

    QueryResultBuilder() {}

    @Override
    public QueryResultBuilder<R, Q> query(Q query) {
        Objects.requireNonNull(query, "Query cannot be null");
        this.query = query;
        return this;
    }

    @Override
    public QueryResultBuilder<R, Q> result(R result) {
        Objects.requireNonNull(result, "Result cannot be null");
        this.result = result;
        return this;
    }

    @Override
    public RegionQuery.Result<R, Q> build() {
        Preconditions.checkState(this.query != null, "Query cannot be empty");
        Preconditions.checkState(this.result != null, "Result cannot be empty");

        return new RegionQuery.Result<>() {
            @Override
            public Q query() {
                return query;
            }

            @Override
            public R result() {
                return result;
            }
        };
    }
}
