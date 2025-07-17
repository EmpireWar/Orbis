/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2024 Empire War
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
