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
package org.empirewar.orbis.sponge.task;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.task.RegionVisualiserTaskBase;
import org.joml.Vector3dc;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.UUID;

public class SpongeRegionVisualiserTask extends RegionVisualiserTaskBase implements Runnable {

    public SpongeRegionVisualiserTask(OrbisPlatform platform) {
        super(platform);
    }

    @Override
    protected Vector3dc getPlayerPosition(UUID uuid) {
        ServerPlayer player = Sponge.server().player(uuid).orElseThrow();
        var loc = player.position();
        return new org.joml.Vector3d(loc.x(), loc.y(), loc.z());
    }

    @Override
    protected void showParticle(UUID uuid, Vector3dc point) {
        ServerPlayer player = Sponge.server().player(uuid).orElseThrow();
        ServerWorld world = player.world();
        world.spawnParticles(
                ParticleEffect.builder()
                        .type(ParticleTypes.HAPPY_VILLAGER.get())
                        .build(),
                new org.spongepowered.math.vector.Vector3d(point.x(), point.y(), point.z()));
    }
}
