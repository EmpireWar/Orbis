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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

import org.empirewar.orbis.fabric.access.ServerPlayerDuck;
import org.empirewar.orbis.fabric.event.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public class PlayerConnectionMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onTeleport(
            PositionMoveRotation positionMoveRotation, Set<Relative> set, CallbackInfo ci) {
        final ServerPlayerDuck duck = (ServerPlayerDuck) player;
        // We won't call teleport event if this is the initial teleport, otherwise we'll have
        // problems.
        // This allows a player to spawn inside a region they shouldn't be able to enter (if they
        // left inside its area),
        // which is fine.
        if (duck.orbis$getLastTickPosition() == null) {
            return;
        }

        PositionMoveRotation positionMoveRotation2 = PositionMoveRotation.of(player);
        PositionMoveRotation targetPosition = PositionMoveRotation.calculateAbsolute(
                positionMoveRotation2, positionMoveRotation, set);
        if (PlayerTeleportEvent.EVENT
                .invoker()
                .teleport(player, player.level(), targetPosition.position())) {
            ci.cancel();
        } else {
            duck.orbis$setLastTickPosition(targetPosition.position());
        }
    }
}
