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
package org.empirewar.orbis.modded.util;

import net.kyori.adventure.key.Keyed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jetbrains.annotations.Nullable;

public final class FlagActions {

    private FlagActions() {
        throw new UnsupportedOperationException();
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

    public static boolean shouldPreventEntityAction(Entity entity, RegistryRegionFlag<Boolean> flag) {
        return shouldPreventEntityAction(entity, entity, flag);
    }

    public static boolean shouldPreventEntityAction(
            Entity entity, @Nullable Entity player, RegistryRegionFlag<Boolean> flag) {
        final RegionisedWorld world = OrbisAPI.get()
                .getRegionisedWorld(((Keyed) entity.level().dimension()).key());
        if (world == null) return false;

        RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) {
            builder.player(player.getUUID());
        }

        final BlockPos location = entity.blockPosition();
        return !world
                .query(RegionQuery.Position.at(location.getX(), location.getY(), location.getZ()))
                .query(builder)
                .result()
                .orElse(true);
    }
}
