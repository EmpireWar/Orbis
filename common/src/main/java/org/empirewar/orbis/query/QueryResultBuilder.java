/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2024 Empire War
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
