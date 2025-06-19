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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.OrbisAPI;

import java.util.Objects;
import java.util.UUID;

public final class PermissionMember extends Member {

    public static MapCodec<PermissionMember> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING
                            .fieldOf("permission")
                            .forGetter(PermissionMember::permission))
                    .apply(instance, PermissionMember::new));

    private final String permission;

    public PermissionMember(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return permission;
    }

    @Override
    public boolean checkMember(UUID member) {
        return OrbisAPI.get().hasPermission(member, permission);
    }

    @Override
    public MemberType<?> getType() {
        return MemberType.PERMISSION;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PermissionMember that)) return false;
        return Objects.equals(permission, that.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(permission);
    }
}
