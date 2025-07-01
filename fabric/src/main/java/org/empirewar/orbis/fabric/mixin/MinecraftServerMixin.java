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
package org.empirewar.orbis.fabric.mixin;

import net.minecraft.server.MinecraftServer;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.OrbisPlatform;
import org.empirewar.orbis.fabric.task.FabricRegionVisualiserTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Unique private long orbis$ticksUntilVisualise = 20L;

    @Unique private final FabricRegionVisualiserTask orbis$regionVisualiserTask =
            new FabricRegionVisualiserTask(
                    (OrbisPlatform) OrbisAPI.get(), (MinecraftServer) (Object) this);

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (--this.orbis$ticksUntilVisualise == 0L) {
            orbis$regionVisualiserTask.run();
            orbis$ticksUntilVisualise = 20L;
        }
    }
}
