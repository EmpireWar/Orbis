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
package org.empirewar.orbis.region;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a region within a world.
 * <p>
 * Regions hold an {@link Area} to define the locations affected by the region.
 * They also hold a set of {@link MutableRegionFlag}s.
 * <p>
 * Multiple regions spanning the same area will be prioritised based off the {@link #priority()} parameter.
 * When querying a location for a flag, the region with the highest priority that has that flag should return the flag value.
 * <p>
 * Regions that have the same priority and conflicting flags have undefined behaviour.
 * In these cases, admins should be warned about possible issues.
 */
public class Region implements RegionQuery.Flag.Queryable, Comparable<Region> {

    public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(Region::name),
                    MutableRegionFlag.CODEC
                            .listOf()
                            .fieldOf("flags")
                            .forGetter(r -> r.flags.values().stream().toList()),
                    Area.CODEC.fieldOf("area").forGetter(Region::area),
                    Codec.INT.fieldOf("priority").forGetter(Region::priority))
            .apply(instance, Region::new));

    private final String name;
    private final Map<Key, MutableRegionFlag<?>> flags;
    private final Area area;

    private int priority;

    public Region(String name) {
        this.name = name;
        this.flags = new HashMap<>();
        this.area = new CuboidArea();
        this.priority = 1;
    }

    private Region(String name, List<MutableRegionFlag<?>> flags, Area area, int priority) {
        this.name = name;
        this.flags = new HashMap<>();
        flags.forEach(mu -> this.flags.put(mu.key(), mu));
        this.area = area;
        this.priority = priority;
    }

    /**
     * Gets the name of this region.
     * <p>
     * A region name should never include spaces and is considered an identifier.
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the area of this region.
     * @return the area
     */
    public Area area() {
        return area;
    }

    /**
     * Gets the priority of this region.
     * <p>
     * A priority is always positive (including zero) and a higher priority gives precedence over other regions when checking flags.
     * @return the priority
     */
    public int priority() {
        return priority;
    }

    /**
     * @see #priority()
     * @param priority the priority
     */
    public void priority(int priority) {
        Preconditions.checkState(priority >= 0, "Priority must be positive");
        this.priority = priority;
    }

    /**
     * Adds a flag to this region.
     * The flag is transformed into a <i>mutable</i> flag.
     * @see RegionFlag#asMutable()
     * @param flag the flag to add
     */
    public void addFlag(RegionFlag<?> flag) {
        flags.put(flag.key(), flag.asMutable());
    }

    /**
     * Removes a flag from this region.
     * @param flag the flag to remove
     * @return true if flag existed and was removed
     */
    public boolean removeFlag(RegionFlag<?> flag) {
        return flags.remove(flag.key()) == null;
    }

    /**
     * Sets the value of a flag.
     * @param flag the flag
     * @param value the value
     * @param <T> value type
     */
    public <T> void setFlag(RegionFlag<T> flag, T value) {
        final MutableRegionFlag<T> mu = (MutableRegionFlag<T>) flags.get(flag.key());
        mu.setValue(value);
    }

    @Override
    public <FR> RegionQuery.Result<Optional<FR>, RegionQuery.Flag<FR>> query(
            RegionQuery.Flag<FR> flag) {
        final Stream<MutableRegionFlag<?>> stream = flags.values().stream();
        final Optional<MutableRegionFlag<?>> foundFlag =
                stream.filter(mu -> mu.equals(flag.flag())).findAny();
        Optional<FR> foundValue;
        foundValue = foundFlag.flatMap(
                mutableRegionFlag -> Optional.of((FR) mutableRegionFlag.getValue()));
        return flag.resultBuilder().query(flag).result(foundValue).build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("flags", flags)
                .add("area", area)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region region)) return false;
        return Objects.equals(name(), region.name());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name());
    }

    @Override
    public int compareTo(@NotNull Region o) {
        return Integer.compare(this.priority, o.priority());
    }
}
