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
package org.empirewar.orbis.fabric.mixin;

import net.kyori.adventure.key.Keyed;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.fabric.api.event.RegionEnterEvent;
import org.empirewar.orbis.fabric.api.event.RegionLeaveEvent;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class PlayerTickEventMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo info) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        final Vec3 to = player.position();
        final Vec3 from = player.oldPosition();
        final RegionisedWorld world =
                OrbisAPI.get().getRegionisedWorld(((Keyed) player.level().dimension()).key());

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> toQuery =
                world.query(RegionQuery.Position.builder().position(to.x(), to.y(), to.z()));
        final boolean canMove = toQuery.query(
                        RegionQuery.Flag.builder(DefaultFlags.CAN_ENTER).player(player.getUUID()))
                .result()
                .orElse(true);

        if (!canMove) {
            player.teleportTo(from.x, from.y, from.z);
            return;
        }

        final RegionQuery.FilterableRegionResult<RegionQuery.Position> fromQuery =
                world.query(RegionQuery.Position.builder().position(from.x(), from.y(), from.z()));
        final Set<Region> toRegions = toQuery.result();
        final Set<Region> fromRegions = fromQuery.result();
        for (Region possiblyEntered : toRegions) {
            if (fromRegions.contains(possiblyEntered)) continue;
            RegionEnterEvent.EVENT
                    .invoker()
                    .enter(player, player.level(), to, world, possiblyEntered);
        }

        for (Region possiblyLeft : fromRegions) {
            if (toRegions.contains(possiblyLeft)) continue;
            RegionLeaveEvent.EVENT.invoker().leave(player, player.level(), to, world, possiblyLeft);
        }
    }
}
