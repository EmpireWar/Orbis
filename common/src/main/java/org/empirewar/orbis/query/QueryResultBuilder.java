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
import java.util.function.BiFunction;

final class QueryResultBuilder<RQR extends RegionQuery.Result<R, Q>, R, Q extends RegionQuery<R>>
        implements RegionQuery.Result.Builder<RQR, R, Q> {

    private final BiFunction<Q, R, RQR> transformer;
    private R result;
    private Q query;

    QueryResultBuilder(BiFunction<Q, R, RQR> transformer) {
        this.transformer = transformer;
    }

    @Override
    public QueryResultBuilder<RQR, R, Q> query(Q query) {
        Objects.requireNonNull(query, "Query cannot be null");
        this.query = query;
        return this;
    }

    @Override
    public QueryResultBuilder<RQR, R, Q> result(R result) {
        Objects.requireNonNull(result, "Result cannot be null");
        this.result = result;
        return this;
    }

    @Override
    public RQR build() {
        Preconditions.checkState(this.query != null, "Query cannot be empty");
        Preconditions.checkState(this.result != null, "Result cannot be empty");
        return transformer.apply(query, result);
    }
}
