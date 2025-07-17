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
package org.empirewar.orbis.registry;

import com.google.common.collect.Maps;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegionFlagType;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.member.MemberType;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.region.RegionType;
import org.empirewar.orbis.registry.lifecycle.RegistryLifecycles;

import java.util.Map;
import java.util.function.Supplier;

// spotless:off
public final class OrbisRegistries {

    private static final Map<Key, Supplier<?>> DEFAULT_ENTRIES = Maps.newLinkedHashMap();

    public static final ResolvableStringOrbisRegistry<Region> REGIONS =
            createStringResolvable(Key.key("orbis", "regions"), r -> null);

    public static final KeyOrbisRegistry<RegistryRegionFlag<?>> FLAGS =
            create(Key.key("orbis", "flags"), r -> DefaultFlags.CAN_BREAK);

    public static final KeyOrbisRegistry<RegionFlagType<?>> FLAG_TYPE =
            create(Key.key("orbis", "flag_type"), r -> {
                final RegionFlagType<MutableRegionFlag<?>> type = RegionFlagType.MUTABLE;
                r.setLifecycle(RegistryLifecycles.frozen());
                return type;
            });

    public static final KeyOrbisRegistry<AreaType<?>> AREA_TYPE =
            create(Key.key("orbis", "area_type"), r -> {
                final AreaType<CuboidArea> type = AreaType.CUBOID;
                r.setLifecycle(RegistryLifecycles.frozen());
                return type;
            });

    public static final KeyOrbisRegistry<RegionType<?>> REGION_TYPE =
            create(Key.key("orbis", "region_type"), r -> {
                final RegionType<Region> type = RegionType.NORMAL;
                r.setLifecycle(RegistryLifecycles.frozen());
                return type;
            });

    public static final KeyOrbisRegistry<MemberType<?>> MEMBER_TYPE =
            create(Key.key("orbis", "member_type"), r -> {
                final MemberType<PlayerMember> type = MemberType.PLAYER;
                r.setLifecycle(RegistryLifecycles.frozen());
                return type;
            });

    private static <T> KeyOrbisRegistry<T> create(Key key, Initializer<T, Key> initializer) {
        final KeyOrbisRegistry<T> registry = new KeyOrbisRegistry<>(key);
        DEFAULT_ENTRIES.put(key, () -> initializer.run(registry));
        return registry;
    }

    private static <T extends RegistryResolvable<String>>
            ResolvableStringOrbisRegistry<T> createStringResolvable(
                    Key key, Initializer<T, String> initializer) {
        final ResolvableStringOrbisRegistry<T> registry = new ResolvableStringOrbisRegistry<>(key);
        DEFAULT_ENTRIES.put(key, () -> initializer.run(registry));
        return registry;
    }

    public static void initialize() {
        DEFAULT_ENTRIES.forEach((id, initializer) -> initializer.get());
        DEFAULT_ENTRIES.clear();
    }

    @FunctionalInterface
    interface Initializer<T, K> {
        T run(OrbisRegistry<T, K> var1);
    }
}
