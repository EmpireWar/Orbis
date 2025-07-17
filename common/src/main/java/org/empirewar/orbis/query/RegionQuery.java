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

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Represents some kind of query on a region.
 */
public sealed interface RegionQuery<R> permits RegionQuery.Position, RegionQuery.Flag {

    /**
     * Gets the result builder for this query.
     *
     * @return the result builder
     */
    Result.Builder<? extends Result<R, ?>, R, ? extends RegionQuery<R>> resultBuilder();

    // TODO add query for players - RegionQuery<Set<Player>>

    /**
     * Represents a query to find a flag in a region, with optional values to filter the result.
     * @param <FR> the flag type
     */
    non-sealed interface Flag<FR> extends RegionQuery<Optional<FR>> {

        /**
         * Gets the flag of this query.
         *
         * @return the flag
         */
        RegistryRegionFlag<FR> flag();

        Optional<UUID> player();

        @Override
        default Result.Builder<Result<Optional<FR>, Flag<FR>>, Optional<FR>, Flag<FR>>
                resultBuilder() {
            return RegionQuery.Result.builder();
        }

        interface Queryable {

            <FR> Result<Optional<FR>, Flag<FR>> query(Flag<FR> flag);

            default <FR> Result<Optional<FR>, Flag<FR>> query(Builder<FR> flag) {
                return query(flag.build());
            }
        }

        sealed interface Builder<FR> permits RegionQueryFlagBuilder {

            Builder<FR> flag(RegistryRegionFlag<FR> flag);

            Builder<FR> player(UUID player);

            Flag<FR> build();
        }

        static <FR> Builder<FR> builder() {
            return new RegionQueryFlagBuilder<>();
        }

        static <FR> Builder<FR> builder(RegistryRegionFlag<FR> flag) {
            return new RegionQueryFlagBuilder<FR>().flag(flag);
        }
    }

    /**
     * Represents a query to find regions that are within a position.
     *
     * @see Area#contains(Vector3dc)
     */
    non-sealed interface Position extends RegionQuery<Set<Region>> {

        /**
         * Gets the position of this query.
         *
         * @return the position
         */
        Vector3dc position();

        @Override
        default FilterableRegionResult.Builder<
                        FilterableRegionResult<Position>, Set<Region>, Position>
                resultBuilder() {
            return FilterableRegionResult.builder();
        }

        interface Queryable {

            /**
             * Queries for a set of regions for a specific {@link Position} query.
             * <p>
             * The returned {@link Set<Region>} result is ordered with the region of the highest priority first.
             * @param position the position query
             * @return the query {@link Result}
             * @see Region#priority()
             */
            FilterableRegionResult<Position> query(Position position);

            default FilterableRegionResult<Position> query(Position.Builder position) {
                return query(position.build());
            }
        }

        sealed interface Builder permits RegionQueryPositionBuilder {

            Builder position(Vector3dc position);

            default Builder position(double x, double y, double z) {
                return position(new Vector3d(x, y, z));
            }

            Position build();
        }

        static Builder builder() {
            return new RegionQueryPositionBuilder();
        }

        static Builder at(Vector3dc position) {
            return builder().position(position);
        }

        static Builder at(double x, double y, double z) {
            return builder().position(x, y, z);
        }
    }

    /**
     * Represents a result that can be chained to filter a set of regions.
     * @param <Q> the query type
     */
    interface FilterableRegionResult<Q extends RegionQuery<Set<Region>>>
            extends RegionQuery.Result<Set<Region>, Q>, Flag.Queryable {

        @Override
        default <FR> RegionQuery.Result<Optional<FR>, Flag<FR>> query(Flag<FR> flag) {
            final Set<Region> regions = result();
            for (Region region : regions) {

                final RegionQuery.Result<Optional<FR>, Flag<FR>> query = region.query(flag);
                if (query.result().isPresent()) {
                    return query;
                }
            }
            return RegionQuery.Result.<Optional<FR>, Flag<FR>>builder()
                    .query(flag)
                    .result(Optional.empty())
                    .build();
        }

        static <Q extends RegionQuery<Set<Region>>>
                Builder<FilterableRegionResult<Q>, Set<Region>, Q> builder() {
            BiFunction<Q, Set<Region>, FilterableRegionResult<Q>> function =
                    (query, result) -> new FilterableRegionResult<>() {
                        @Override
                        public Q query() {
                            return query;
                        }

                        @Override
                        public Set<Region> result() {
                            return result;
                        }
                    };
            return new QueryResultBuilder<>(function);
        }
    }

    /**
     * Represents the result of a {@link RegionQuery}.
     */
    interface Result<R, Q extends RegionQuery<R>> {

        /**
         * Gets the query used to generate this result.
         *
         * @return the query causing this result
         */
        Q query();

        /**
         * The resulting object of the {@link #query()}.
         *
         * @return the result
         */
        R result();

        interface Builder<RQR extends RegionQuery.Result<R, Q>, R, Q extends RegionQuery<R>> {

            Builder<RQR, R, Q> query(Q query);

            Builder<RQR, R, Q> result(R result);

            RQR build();
        }

        static <RQR extends RegionQuery.Result<R, Q>, R, Q extends RegionQuery<R>>
                Builder<RQR, R, Q> builder(BiFunction<Q, R, RQR> transformer) {
            return new QueryResultBuilder<>(transformer);
        }

        static <R, Q extends RegionQuery<R>> Builder<RegionQuery.Result<R, Q>, R, Q> builder() {
            BiFunction<Q, R, RegionQuery.Result<R, Q>> function =
                    (query, result) -> new Result<>() {
                        @Override
                        public Q query() {
                            return query;
                        }

                        @Override
                        public R result() {
                            return result;
                        }
                    };
            return new QueryResultBuilder<>(function);
        }
    }
}
