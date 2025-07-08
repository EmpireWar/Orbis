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
        float size = 1.0F; // Particle size

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
