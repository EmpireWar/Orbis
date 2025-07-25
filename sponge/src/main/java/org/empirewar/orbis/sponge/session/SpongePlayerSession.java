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
