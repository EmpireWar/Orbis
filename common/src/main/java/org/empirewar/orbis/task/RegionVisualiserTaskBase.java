/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.task;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3ic;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class RegionVisualiserTaskBase implements Runnable {

    protected final OrbisPlatform platform;

    public RegionVisualiserTaskBase(OrbisPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void run() {
        for (UUID player : platform.getVisualisingPlayers()) {
            final Key playerWorld = platform.getPlayerWorld(player);
            final RegionisedWorld regionisedWorld = platform.getRegionisedWorld(playerWorld);
            final Vector3dc position = getPlayerPosition(player);
            final Optional<Region> region =
                    regionisedWorld.query(RegionQuery.Position.at(position)).result().stream()
                            .findFirst();
            if (region.isEmpty() || region.get().isGlobal()) continue;

            Area area = region.get().area();
            Set<Vector3ic> boundary = area.getBoundaryPoints();
            for (Vector3ic point : boundary) {
                showParticle(
                        player, new Vector3d(point.x() + 0.5, point.y() + 0.5, point.z() + 0.5));
            }
        }
    }

    protected abstract Vector3dc getPlayerPosition(UUID uuid);

    protected abstract void showParticle(UUID uuid, Vector3dc point);
}
