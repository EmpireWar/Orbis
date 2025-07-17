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
package org.empirewar.orbis.fabric.task;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;

import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.task.RegionVisualiserTaskBase;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.UUID;

public class FabricRegionVisualiserTask extends RegionVisualiserTaskBase implements Runnable {

    private final MinecraftServer server;

    public FabricRegionVisualiserTask(OrbisPlatform platform, MinecraftServer server) {
        super(platform);
        this.server = server;
    }

    @Override
    protected Vector3dc getPlayerPosition(UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        return new Vector3d(player.getX(), player.getY(), player.getZ());
    }

    @Override
    protected void showGreenParticle(UUID uuid, Vector3dc point) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        player.connection.send(new ClientboundLevelParticlesPacket(
                ParticleTypes.HAPPY_VILLAGER,
                false,
                true,
                (float) point.x(),
                (float) point.y(),
                (float) point.z(),
                0,
                0,
                0,
                0,
                1));
    }

    @Override
    protected void showOrangeParticle(UUID uuid, Vector3dc point) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        // Create orange color (RGB: 255, 165, 0)
        float r = 1.0F; // 255/255
        float g = 0.647F; // 165/255
        float b = 0.0F; // 0/255
        float size = 2.0F; // Particle size

        // Create dust particle options
        DustParticleOptions particleOptions =
                new DustParticleOptions(ARGB.colorFromFloat(r, g, b, 1f), size);

        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                particleOptions,
                false,
                true, // long distance
                point.x(),
                point.y(),
                point.z(),
                0,
                0,
                0, // xd, yd, zd (velocity)
                0, // speed
                1 // count
                );

        player.connection.send(packet);
    }
}
