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
