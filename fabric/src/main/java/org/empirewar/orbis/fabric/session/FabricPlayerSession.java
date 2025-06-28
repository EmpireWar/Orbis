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
package org.empirewar.orbis.fabric.session;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.fabric.OrbisFabric;
import org.empirewar.orbis.player.PlayerOrbisSession;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class FabricPlayerSession extends PlayerOrbisSession {

    private final ServerPlayer player;
    private final CommandSourceStack cause;

    public FabricPlayerSession(ServerPlayer player, CommandSourceStack cause) {
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
        return Permissions.check(player, permission, 3);
    }

    @Override
    public void giveWandItem() {
        player.addItem(((OrbisFabric) OrbisAPI.get()).getWandItem());
    }

    @Override
    public Vector3dc getPosition() {
        final Vec3 position = player.position();
        return new Vector3d(position.x, position.y, position.z);
    }
}
