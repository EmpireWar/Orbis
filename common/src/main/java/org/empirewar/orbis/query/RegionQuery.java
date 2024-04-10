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

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3d;

import java.util.Optional;
import java.util.Set;
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
        RegionFlag<FR> flag();

        // OrbisSession session();

        // TODO Optional<Player> to check flags against a specific player

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

            Builder<FR> flag(RegionFlag<FR> flag);

            // Builder player(); TODO

            Flag<FR> build();
        }

        static <FR> Builder<FR> builder() {
            return new RegionQueryFlagBuilder<>();
        }

        static <FR> Builder<FR> builder(RegionFlag<FR> flag) {
            return new RegionQueryFlagBuilder<FR>().flag(flag);
        }
    }

    /**
     * Represents a query to find regions that are within a position.
     *
     * @see Area#contains(Vector3d)
     */
    non-sealed interface Position extends RegionQuery<Set<Region>> {

        /**
         * Gets the position of this query.
         *
         * @return the position
         */
        Vector3d position();

        @Override
        default Result.Builder<Result, Set<Region>, Position> resultBuilder() {
            return RegionQuery.Result.builder((q, r) -> new Result() {
                @Override
                public Position query() {
                    return q;
                }

                @Override
                public Set<Region> result() {
                    return r;
                }
            });
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
            Result query(Position position);

            default Result query(Position.Builder position) {
                return query(position.build());
            }
        }

        interface Result extends RegionQuery.Result<Set<Region>, Position>, Flag.Queryable {

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
        }

        sealed interface Builder permits RegionQueryPositionBuilder {

            Builder position(Vector3d position);

            default Builder position(double x, double y, double z) {
                return position(new Vector3d(x, y, z));
            }

            Position build();
        }

        static Builder builder() {
            return new RegionQueryPositionBuilder();
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
