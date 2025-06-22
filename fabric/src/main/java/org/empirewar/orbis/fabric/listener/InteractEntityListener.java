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
