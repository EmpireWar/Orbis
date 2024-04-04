package org.empirewar.orbis.query;

import com.google.common.base.Preconditions;

import java.util.Objects;

final class QueryResultBuilder<R, Q extends RegionQuery<?>> implements RegionQuery.Result.Builder<R, Q> {

    private R result;
    private Q query;

    QueryResultBuilder() {

    }

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
