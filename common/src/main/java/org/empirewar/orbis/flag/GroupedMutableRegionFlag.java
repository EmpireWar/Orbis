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
package org.empirewar.orbis.flag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.registry.OrbisRegistries;
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

    public static final MapCodec<GroupedMutableRegionFlag<?>> CODEC = OrbisRegistries.FLAGS
            .getCodec()
            .dispatchMap(mu -> OrbisRegistries.FLAGS.get(mu.key()).orElseThrow(), r -> r.asGrouped()
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
