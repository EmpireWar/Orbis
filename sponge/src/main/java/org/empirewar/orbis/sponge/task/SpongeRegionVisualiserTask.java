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
package org.empirewar.orbis.sponge.task;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.task.RegionVisualiserTaskBase;
import org.joml.Vector3dc;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

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
    protected void showGreenParticle(UUID uuid, Vector3dc point) {
        ServerPlayer player = Sponge.server().player(uuid).orElseThrow();
        ServerWorld world = player.world();
        world.spawnParticles(
                ParticleEffect.builder()
                        .type(ParticleTypes.HAPPY_VILLAGER.get())
                        .build(),
                new Vector3d(point.x(), point.y(), point.z()));
    }

    @Override
    protected void showOrangeParticle(UUID uuid, Vector3dc point) {
        ServerPlayer player = Sponge.server().player(uuid).orElseThrow();
        ServerWorld world = player.world();
        world.spawnParticles(
                ParticleEffect.builder()
                        .type(ParticleTypes.DUST.get())
                        .option(ParticleOptions.COLOR, Color.ofRgb(255, 165, 0))
                        .option(ParticleOptions.SCALE, 2.0)
                        .build(),
                new Vector3d(point.x(), point.y(), point.z()));
    }
}
