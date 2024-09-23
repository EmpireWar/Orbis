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
package org.empirewar.orbis.fabric.session;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import org.empirewar.orbis.player.PlayerOrbisSession;

public final class PlayerSession extends PlayerOrbisSession {

    private final ServerPlayer player;
    private final CommandSourceStack cause;

    public PlayerSession(ServerPlayer player, CommandSourceStack cause) {
        super(player.getUUID(), player);
        this.player = player;
        this.cause = cause;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public CommandSourceStack getCause() {
        return cause;
    }

    @Override
    public boolean hasPermission(String permission) {
        return Permissions.check(player, permission);
    }
}
