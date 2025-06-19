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
package org.empirewar.orbis.registry;

import com.google.common.collect.Maps;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegionFlagType;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.member.MemberType;
import org.empirewar.orbis.region.RegionType;

import java.util.Map;
import java.util.function.Supplier;

// spotless:off
public final class Registries {

    private static final Map<Key, Supplier<?>> DEFAULT_ENTRIES = Maps.newLinkedHashMap();

    public static final Registry<RegistryRegionFlag<?>> FLAGS =
            create(Key.key("orbis", "flags"), r -> DefaultFlags.CAN_BREAK);

    public static final Registry<RegionFlagType<?>> FLAG_TYPE =
            create(Key.key("orbis", "flag_type"), r -> RegionFlagType.MUTABLE);

    public static final Registry<AreaType<?>> AREA_TYPE =
            create(Key.key("orbis", "area_type"), r -> AreaType.CUBOID);

    public static final Registry<RegionType<?>> REGION_TYPE =
            create(Key.key("orbis", "region_type"), r -> RegionType.NORMAL);

    public static final Registry<MemberType<?>> MEMBER_TYPE =
            create(Key.key("orbis", "member_type"), r -> MemberType.PLAYER);

    private static <T> Registry<T> create(Key key, Initializer<T> initializer) {
        final Registry<T> registry = new SimpleRegistry<>(key);
        DEFAULT_ENTRIES.put(key, () -> initializer.run(registry));
        return registry;
    }

    public static void initialize() {
        DEFAULT_ENTRIES.forEach((id, initializer) -> initializer.get());
    }

    @FunctionalInterface
    interface Initializer<T> {
        T run(Registry<T> var1);
    }
}
