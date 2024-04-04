package org.empirewar.orbis.query;

import com.google.common.base.Preconditions;
import org.empirewar.orbis.flag.RegionFlag;
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
