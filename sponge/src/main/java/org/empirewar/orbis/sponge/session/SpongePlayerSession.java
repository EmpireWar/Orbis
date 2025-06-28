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
package org.empirewar.orbis.sponge.session;

import org.empirewar.orbis.player.PlayerOrbisSession;
import org.empirewar.orbis.sponge.key.SpongeDataKeys;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class SpongePlayerSession extends PlayerOrbisSession {

    private final ServerPlayer player;
    private final CommandCause cause;

    public SpongePlayerSession(ServerPlayer player, CommandCause cause) {
        super(player.uniqueId(), player);
        this.player = player;
        this.cause = cause;
    }

    public CommandCause getCause() {
        return cause;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void giveWandItem() {
        player.inventory().offer(SpongeDataKeys.WAND_ITEM);
    }

    @Override
    public Vector3dc getPosition() {
        final org.spongepowered.math.vector.Vector3d position = player.position();
        return new Vector3d(position.x(), position.y(), position.z());
    }
}
