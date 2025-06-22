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
package org.empirewar.orbis.fabric.listener;

import net.fabricmc.fabric.api.event.player.*;
import net.kyori.adventure.key.Keyed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.HitResult;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.fabric.OrbisFabric;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jetbrains.annotations.Nullable;

public final class BlockActionListener {

    private final OrbisFabric orbis;

    public BlockActionListener(OrbisFabric orbis) {
        this.orbis = orbis;

        // Block breaking
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            return !shouldPreventBlockAction(world, pos, player, DefaultFlags.CAN_BREAK);
        });

        // Block placing
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                if (shouldPreventBlockAction(world, pos, player, DefaultFlags.CAN_PLACE)) {
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static boolean shouldPreventBlockAction(
            Level level, BlockPos pos, @Nullable Player player, RegistryRegionFlag<Boolean> flag) {
        if (pos == null) return false;

        RegionisedWorld world =
                OrbisAPI.get().getRegionisedWorld(((Keyed) level.dimension()).key());
        if (world == null) return false;

        RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) {
            builder.player(player.getUUID());
        }

        return !world.query(RegionQuery.Position.at(pos.getX(), pos.getY(), pos.getZ()))
                .query(builder)
                .result()
                .orElse(true);
    }
}
