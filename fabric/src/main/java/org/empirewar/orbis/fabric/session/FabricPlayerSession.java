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
