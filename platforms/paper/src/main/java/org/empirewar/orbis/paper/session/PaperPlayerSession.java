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
package org.empirewar.orbis.paper.session;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.paper.OrbisPaperPlatform;
import org.empirewar.orbis.player.PlayerOrbisSession;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class PaperPlayerSession extends PlayerOrbisSession {

    private final CommandSourceStack source;

    private final Player player;

    public Player getPlayer() {
        return player;
    }

    public PaperPlayerSession(Player player, CommandSourceStack source) {
        super(
                player.getUniqueId(),
                ((OrbisPaperPlatform<?>) OrbisAPI.get()).senderAsAudience(player));
        this.player = player;
        this.source = source;
    }

    public CommandSourceStack source() {
        return source;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void giveWandItem() {
        player.getInventory().addItem(((OrbisPaperPlatform<?>) OrbisAPI.get()).wandItem());
    }

    @Override
    public Vector3dc getPosition() {
        final Location location = player.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }
}
