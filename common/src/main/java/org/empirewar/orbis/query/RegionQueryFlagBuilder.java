package org.empirewar.orbis.query;

import com.google.common.base.Preconditions;
import org.empirewar.orbis.flag.RegionFlag;

import java.util.Objects;

non-sealed class RegionQueryFlagBuilder implements RegionQuery.Flag.Builder {

    private RegionFlag flag;

    RegionQueryFlagBuilder() {

    }

    @Override
    public RegionQuery.Flag build() {
        Preconditions.checkState(this.flag != null, "Flag cannot be empty");
        return () -> flag;
    }

    @Override
    public RegionQuery.Flag.Builder flag(RegionFlag flag) {
        Objects.requireNonNull(flag, "Flag cannot be null");
        this.flag = flag;
        return this;
    }
}
