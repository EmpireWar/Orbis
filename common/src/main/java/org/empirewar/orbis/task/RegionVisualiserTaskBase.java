/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.task;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.exception.IncompleteAreaException;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.world.RegionisedWorld;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3ic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class RegionVisualiserTaskBase implements Runnable {

    protected final OrbisPlatform platform;

    // To prevent constantly building the same area, which will cause lag (due to generating
    // boundary points etc.), cache it
    private final Map<UUID, CachedArea> areaCache = new HashMap<>();

    /**
     * @param pointsHash To detect changes in selection points
     */
    private record CachedArea(Area area, int pointsHash) {}

    public RegionVisualiserTaskBase(OrbisPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void run() {
        Set<UUID> currentPlayers = new HashSet<>(platform.getVisualisingPlayers());

        // Clean up cache for players who are no longer visualizing
        Set<UUID> playersToRemove = areaCache.keySet().stream()
                .filter(uuid -> !currentPlayers.contains(uuid))
                .collect(Collectors.toSet());
        playersToRemove.forEach(areaCache::remove);

        // Process current players
        for (UUID player : currentPlayers) {
            showSelection(player);
            showCurrentPrioritisedRegionArea(player);
        }
    }

    private void showCurrentPrioritisedRegionArea(UUID player) {
        final Key playerWorld = platform.getPlayerWorld(player);
        final RegionisedWorld regionisedWorld = platform.getRegionisedWorld(playerWorld);
        final Vector3dc position = getPlayerPosition(player);
        final Optional<Region> region =
                regionisedWorld.query(RegionQuery.Position.at(position)).result().stream()
                        .findFirst();
        if (region.isEmpty() || region.get().isGlobal()) return;

        Area area = region.get().area();
        showParticlesForArea(area, player, this::showGreenParticle);
    }

    private void showSelection(UUID player) {
        final Selection selection = platform.selectionManager().get(player).orElse(null);
        if (selection == null) {
            areaCache.remove(player);
            return;
        }

        try {
            // Calculate hash of current selection points for change detection
            int currentHash = Objects.hash(selection.getPoints().toArray());

            // Get cached area or build a new one if needed
            CachedArea cached = areaCache.get(player);
            if (cached == null || cached.pointsHash != currentHash) {
                final Area area = selection.build();
                areaCache.put(player, new CachedArea(area, currentHash));
                showParticlesForArea(area, player, this::showOrangeParticle);
            } else {
                // Use cached area
                showParticlesForArea(cached.area, player, this::showOrangeParticle);
            }
        } catch (IncompleteAreaException ignored) {
            areaCache.remove(player);
            // Not complete yet
        }
    }

    private static final double MAX_DISTANCE_SQUARED =
            128.0 * 128.0; // 128 blocks squared for distance check

    private void showParticlesForArea(
            Area area, UUID player, BiConsumer<UUID, Vector3dc> particle) {
        Vector3dc playerPos = getPlayerPosition(player);
        Set<Vector3ic> boundary = area.getBoundaryPoints();
        for (Vector3ic point : boundary) {
            Vector3d particlePos = new Vector3d(point.x() + 0.5, point.y() + 0.5, point.z() + 0.5);
            // Only show particle if it's within 96 blocks of the player
            if (playerPos.distanceSquared(particlePos) <= MAX_DISTANCE_SQUARED) {
                particle.accept(player, particlePos);
            }
        }
    }

    protected abstract Vector3dc getPlayerPosition(UUID uuid);

    // Really not sure how to make a better abstracted particle system - no adventure API and each
    // platform is vastly different
    protected abstract void showGreenParticle(UUID uuid, Vector3dc point);

    protected abstract void showOrangeParticle(UUID uuid, Vector3dc point);
}
