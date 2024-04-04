package org.empirewar.orbis.query;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.region.Region;
import org.joml.Vector3d;

import java.util.Set;

/**
 * Represents some kind of query on a region.
 */
public sealed interface RegionQuery<R> permits RegionQuery.Position, RegionQuery.Flag {

    /**
     * Gets the result builder for this query.
     * @return the result builder
     */
    Result.Builder<R, ? extends RegionQuery<R>> resultBuilder();

    // TODO add query for players - RegionQuery<Set<Player>>

    non-sealed interface Flag extends RegionQuery<Boolean> {

        /**
         * Gets the flag of this query.
         * @return the flag
         */
        RegionFlag flag();

        // TODO Optional<Player> to check flags against a specific player

        @Override
        default Result.Builder<Boolean, Flag> resultBuilder() {
            return RegionQuery.Result.builder();
        }

        interface Queryable {

            Result<Boolean, Flag> query(Flag flag);
        }

        sealed interface Builder permits RegionQueryFlagBuilder {

            Builder flag(RegionFlag flag);

//            Builder player(); TODO

            Flag build();
        }

        static Builder builder() {
            return new RegionQueryFlagBuilder();
        }
    }

    /**
     * Represents a query to find regions that are within a position.
     * @see Area#contains(Vector3d)
     */
    non-sealed interface Position extends RegionQuery<Set<Region>> {

        /**
         * Gets the position of this query.
         * @return the position
         */
        Vector3d position();

        @Override
        default Result.Builder<Set<Region>, Position> resultBuilder() {
            return RegionQuery.Result.builder();
        }

        interface Queryable {

            /**
             * Queries for a set of regions for a specific {@link Position} query.
             * @param position the position query
             * @return the query {@link Result}
             */
            Result<Set<Region>, Position> query(Position position);
        }

        sealed interface Builder permits RegionQueryPositionBuilder {

            Builder position(Vector3d position);

            Position build();
        }

        static Builder builder() {
            return new RegionQueryPositionBuilder();
        }
    }

    /**
     * Represents the result of a {@link RegionQuery}.
     */
    interface Result<R, Q extends RegionQuery<?>> {

        /**
         * Gets the query used to generate this result.
         * @return the query causing this result
         */
        Q query();

        /**
         * The resulting object of the {@link #query()}.
         * @return the result
         */
        R result();

        interface Builder<R, Q extends RegionQuery<?>> {

            Builder<R, Q> query(Q query);

            Builder<R, Q> result(R result);

            RegionQuery.Result<R, Q> build();
        }

        static <R, Q extends RegionQuery<?>> Builder<R, Q> builder() {
            return new QueryResultBuilder<>();
        }
    }
}
