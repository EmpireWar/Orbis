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
package org.empirewar.orbis.neoforge.listener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.modded.util.FlagActions;

public final class InteractEntityListener {

    public InteractEntityListener(Orbis orbis) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!FlagActions.shouldPreventEntityAction(player, MinecraftFlags.INVULNERABILITY)) {
                event.setCanceled(true);
                return;
            }

            Entity direct = event.getSource().getDirectEntity();
            if (!(direct instanceof ServerPlayer)) {
                if (FlagActions.shouldPreventEntityAction(
                        player, MinecraftFlags.CAN_TAKE_MOB_DAMAGE_SOURCES)) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
