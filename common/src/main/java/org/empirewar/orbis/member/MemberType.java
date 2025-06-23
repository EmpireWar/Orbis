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
package org.empirewar.orbis.member;

import com.mojang.serialization.MapCodec;

import org.empirewar.orbis.registry.OrbisRegistries;
import org.empirewar.orbis.registry.OrbisRegistry;

public interface MemberType<M extends Member> {

    // spotless:off
    MemberType<PlayerMember> PLAYER = register("player", PlayerMember.CODEC);
    MemberType<PermissionMember> PERMISSION = register("permission", PermissionMember.CODEC);
    // spotless:on

    MapCodec<M> codec();

    private static <M extends Member> MemberType<M> register(String id, MapCodec<M> codec) {
        return OrbisRegistry.register(OrbisRegistries.MEMBER_TYPE, id, () -> codec);
    }
}
