/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale.util;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public final class QueryUtil {

    public static boolean shouldPreventBlockAction(
            @Nullable Vector3i block, World world, RegistryRegionFlag<Boolean> flag) {
        return shouldPreventBlockAction(block, null, world, flag);
    }

    // spotless:off
    public static boolean shouldPreventBlockAction(@Nullable Vector3i block, @Nullable Player player, World world, RegistryRegionFlag<Boolean> flag) {
        // spotless:on
        if (block == null) return false;
        final Vector3d pos = new Vector3d(block.getX(), block.getY(), block.getZ());
        final RegionisedWorld regionisedWorld =
                OrbisAPI.get().getRegionisedWorld(Key.key("hytale", world.getName()));
        final RegionQuery.Flag.Builder<Boolean> builder = RegionQuery.Flag.builder(flag);
        if (player != null) builder.player(player.getUuid());
        final boolean canAct = regionisedWorld
                .query(RegionQuery.Position.builder().position(pos))
                .query(builder)
                .result()
                .orElse(true);
        return !canAct;
    }
}
