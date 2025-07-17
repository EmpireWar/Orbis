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
package org.empirewar.orbis.fabric.listener;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.kyori.adventure.key.Keyed;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;

public final class InteractEntityListener {

    public InteractEntityListener(Orbis orbis) {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer) {
                if (shouldPreventEntityAction(entity, DefaultFlags.INVULNERABILITY)) {
                    return false;
                }

                if (!(source.getDirectEntity() instanceof ServerPlayer)) {
                    return !shouldPreventEntityAction(
                            entity, DefaultFlags.CAN_TAKE_MOB_DAMAGE_SOURCES);
                }
            }
            return true;
        });
    }

    public static boolean shouldPreventEntityAction(
            Entity entity, RegistryRegionFlag<Boolean> flag) {
        final BlockPos location = entity.blockPosition();
        return !OrbisAPI.get()
                .getRegionisedWorld(((Keyed) entity.level().dimension()).key())
                .query(RegionQuery.Position.at(location.getX(), location.getY(), location.getZ()))
                .query(RegionQuery.Flag.builder(flag).player(entity.getUUID()))
                .result()
                .orElse(true);
    }
}
