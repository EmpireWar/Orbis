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
package org.empirewar.orbis.region;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.GroupedMutableRegionFlag;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.registry.RegistryResolvable;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a region within a world.
 * <p>
 * Regions hold an {@link Area} to define the locations affected by the region.
 * They also hold a set of {@link MutableRegionFlag}s.
 * <p>
 * Regions may have region <i>parents</i> defined, where they will inherit the flags of those parents
 * if it does not have an overriding flag set.
 * <p>
 * There is also a set of {@link Member}s.
 * Members may be influenced differently by a {@link org.empirewar.orbis.flag.GroupedMutableRegionFlag}.
 * By default, flags affect all members in a region.
 * <p>
 * Multiple regions spanning the same area will be prioritised based off the {@link #priority()} parameter.
 * When querying a location for a flag, the region with the highest priority that has that flag should return the flag value.
 * <p>
 * Regions that have the same priority and conflicting flags have undefined behaviour.
 * In these cases, admins should be warned about possible issues.
 */
public sealed class Region
        implements RegionQuery.Flag.Queryable, Comparable<Region>, RegistryResolvable<String>
        permits GlobalRegion {

    public static final MapCodec<Region> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codec.STRING.fieldOf("name").forGetter(Region::name),
                            Codec.STRING
                                    .listOf()
                                    .fieldOf("parents")
                                    .forGetter(r -> r.parents().stream()
                                            .map(Region::name)
                                            .toList()),
                            Member.CODEC
                                    .listOf()
                                    .fieldOf("members")
                                    .forGetter(r -> r.members().stream().toList()),
                            MutableRegionFlag.TYPE_CODEC
                                    .listOf()
                                    .fieldOf("flags")
                                    .forGetter(r -> r.flags.values().stream().toList()),
                            Area.CODEC.fieldOf("area").forGetter(Region::area),
                            Codec.INT.fieldOf("priority").forGetter(Region::priority))
                    .apply(instance, Region::new));

    private final String name;
    private final Set<Region> parents;
    private final Set<Member> members;
    protected final Map<Key, MutableRegionFlag<?>> flags;
    private final Area area;

    private int priority;

    public Region(String name, Area area) {
        this.name = name;
        this.parents = new HashSet<>();
        this.members = new HashSet<>();
        this.flags = new HashMap<>();
        this.area = area;
        this.priority = 2;
    }

    private Region(
            String name,
            List<String> parents,
            List<Member> members,
            List<MutableRegionFlag<?>> flags,
            Area area,
            int priority) {
        this.name = name;
        this.parents = new HashSet<>();
        parents.forEach(parentName -> {
            OrbisAPI.get()
                    .logger()
                    .info("Region {} waiting for region parent {}", name, parentName);
            OrbisRegistries.REGIONS.resolve(parentName, this::addParent);
        });
        this.members = new HashSet<>(members);
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
     * Gets the parents of this region.
     * @return the parents
     */
    public Set<Region> parents() {
        return Set.copyOf(parents);
    }

    /**
     * Adds a parent to this region.
     * @param region the parent to add
     */
    public void addParent(Region region) {
        if (region.equals(this)) {
            throw new IllegalArgumentException("Cannot add parent of self!");
        }

        if (region.parents.contains(this)) {
            throw new IllegalArgumentException("Cannot have a parent loop!");
        }

        if (parents.stream().anyMatch(p -> p.parents.contains(region))) {
            throw new IllegalArgumentException("Cannot have a parent loop!");
        }

        parents.add(region);
    }

    /**
     * Removes a parent from this region.
     * @param region the parent to remove
     */
    public void removeParent(Region region) {
        parents.remove(region);
    }

    /**
     * Gets the members of this region.
     * @return the members
     */
    public Set<Member> members() {
        return Set.copyOf(members);
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public void removeMember(Member member) {
        members.remove(member);
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
     * @see RegistryRegionFlag#asMutable()
     * @param flag the flag to add
     * @return the transformed flag instance
     */
    public <T> MutableRegionFlag<T> addFlag(RegistryRegionFlag<T> flag) {
        final MutableRegionFlag<T> mutable = flag.asMutable();
        flags.put(flag.key(), mutable);
        return mutable;
    }

    /**
     * Adds a grouped flag to this region with the initial set of groups.
     * @param flag the flag to add
     * @param groups the groups to add to the flag
     * @return the transformed flag instance
     */
    public <T> GroupedMutableRegionFlag<T> addGroupedFlag(
            RegistryRegionFlag<T> flag, Set<FlagMemberGroup> groups) {
        final GroupedMutableRegionFlag<T> grouped = flag.asGrouped();
        flags.put(flag.key(), grouped);
        groups.forEach(grouped::addGroup);
        return grouped;
    }

    /**
     * Removes a flag from this region.
     * @param flag the flag to remove
     * @return true if flag existed and was removed
     */
    public boolean removeFlag(RegistryRegionFlag<?> flag) {
        return flags.remove(flag.key()) == null;
    }

    public boolean hasFlag(RegistryRegionFlag<?> flag) {
        return flags.containsKey(flag.key());
    }

    public <T> Optional<MutableRegionFlag<T>> getFlag(RegistryRegionFlag<T> flag) {
        return Optional.ofNullable((MutableRegionFlag<T>) flags.get(flag.key()));
    }

    /**
     * Sets the value of a flag.
     * @throws IllegalArgumentException if the flag is not present
     * @param flag the flag
     * @param value the value
     * @param <T> value type
     */
    public <T> void setFlag(RegistryRegionFlag<T> flag, T value) {
        MutableRegionFlag<T> mu = getFlag(flag)
                .orElseThrow(() -> new IllegalArgumentException("Region '" + name
                        + "' does not have a flag by key '" + flag.key().asString() + "'!"));
        mu.setValue(value);
    }

    @Override
    public <FR> RegionQuery.Result<Optional<FR>, RegionQuery.Flag<FR>> query(
            RegionQuery.Flag<FR> flag) {
        final Optional<MutableRegionFlag<FR>> foundFlag = getFlag(flag.flag());
        final Optional<UUID> player = flag.player();
        final Member playerMember = player.flatMap(uuid -> members.stream()
                        .filter(member -> member.checkMember(uuid))
                        .findAny())
                .orElse(null);

        Optional<FR> foundValue;
        foundValue = foundFlag
                .flatMap(mutableRegionFlag -> {
                    // TODO add test case
                    if (player.isPresent()
                            && mutableRegionFlag instanceof GroupedMutableRegionFlag<?> group) {
                        if (playerMember == null
                                && !group.groups().contains(FlagMemberGroup.NONMEMBER)) {
                            return Optional.empty();
                        } else if (playerMember != null
                                && !group.groups().contains(FlagMemberGroup.MEMBER)) {
                            return Optional.empty();
                        }
                    }

                    return Optional.of(mutableRegionFlag.getValue());
                })
                .or(() -> parents.stream()
                        .sorted(Comparator.reverseOrder())
                        .map(r -> r.query(flag))
                        .map(RegionQuery.Result::result)
                        .findAny()
                        .orElse(Optional.empty()));
        return flag.resultBuilder().query(flag).result(foundValue).build();
    }

    /**
     * Gets whether this region is a "global region".
     * <p>
     * A global region is a region that encompasses a world.
     * @return true if global
     */
    public boolean isGlobal() {
        return this instanceof GlobalRegion;
    }

    public RegionType<?> getType() {
        return RegionType.NORMAL;
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

    @Override
    public String key() {
        return name;
    }
}
