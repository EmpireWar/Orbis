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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.util.ExtraCodecs;

import java.util.Objects;
import java.util.UUID;

public final class PlayerMember extends Member {

    public static MapCodec<PlayerMember> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.STRING_UUID
                            .fieldOf("player_id")
                            .forGetter(PlayerMember::playerId))
                    .apply(instance, PlayerMember::new));

    private final UUID playerId;

    public PlayerMember(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }

    @Override
    public boolean checkMember(UUID member) {
        return member.equals(playerId);
    }

    @Override
    public MemberType<?> getType() {
        return MemberType.PLAYER;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PlayerMember that)) return false;
        return Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(playerId);
    }
}
