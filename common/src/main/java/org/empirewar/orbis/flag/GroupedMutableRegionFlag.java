/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.registry.Registries;
import org.empirewar.orbis.util.EnumCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents a flag that affects a specific set of {@link FlagMemberGroup}s.
 * <p>
 * By default, a {@link MutableRegionFlag} will affect all players regardless of whether they are a region member or not.
 * <p>
 * This class allows for a flag to only apply to specific groups.
 * <p>
 * A {@link RegistryRegionFlag} may be converted to a Grouped instance by calling {@link RegistryRegionFlag#asGrouped()}.
 * @param <T> the type this flag has
 * @see MutableRegionFlag
 */
public final class GroupedMutableRegionFlag<T> extends MutableRegionFlag<T> {

    public static final MapCodec<GroupedMutableRegionFlag<?>> CODEC = Registries.FLAGS
            .getCodec()
            .dispatchMap(mu -> Registries.FLAGS.get(mu.key()).orElseThrow(), r -> r.asGrouped()
                    .getCodec(r));

    private final Set<FlagMemberGroup> groups;

    GroupedMutableRegionFlag(Key key, Supplier<T> defaultValue, Codec<T> codec) {
        super(key, defaultValue, codec);
        this.groups = new HashSet<>();
    }

    /**
     * Gets the set of groups this flag is affected by.
     * @return groups affecting this flag
     */
    public Set<FlagMemberGroup> groups() {
        return Set.copyOf(groups);
    }

    public void addGroup(FlagMemberGroup group) {
        groups.add(group);
    }

    public void removeGroup(FlagMemberGroup group) {
        groups.remove(group);
    }

    @Override
    public RegionFlagType<?> getType() {
        return RegionFlagType.GROUPED_MUTABLE;
    }

    @Override
    public MapCodec<? extends GroupedMutableRegionFlag<T>> getCodec(
            RegistryRegionFlag<?> registry) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                        codec.fieldOf("value").forGetter(GroupedMutableRegionFlag::getValue),
                        new EnumCodec<>(FlagMemberGroup.class)
                                .listOf()
                                .fieldOf("groups")
                                .forGetter(gmu -> gmu.groups().stream().toList()))
                .apply(instance, (value, groups) -> {
                    // spotless:off
                    final GroupedMutableRegionFlag<T> mutable = (GroupedMutableRegionFlag<T>) registry.asGrouped();
                    // spotless:on
                    mutable.setValue(value);
                    groups.forEach(mutable::addGroup);
                    return mutable;
                }));
    }
}
