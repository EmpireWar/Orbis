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
package org.empirewar.orbis.bukkit.session;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.bukkit.OrbisBukkitPlatform;
import org.empirewar.orbis.player.PlayerOrbisSession;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class BukkitPlayerSession extends PlayerOrbisSession {

    private final Player player;

    public BukkitPlayerSession(Player player) {
        super(
                player.getUniqueId(),
                ((OrbisBukkitPlatform<?>) OrbisAPI.get()).senderAsAudience(player));
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void giveWandItem() {
        player.getInventory().addItem(((OrbisBukkitPlatform<?>) OrbisAPI.get()).wandItem());
    }

    @Override
    public Vector3dc getPosition() {
        final Location location = player.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }
}
