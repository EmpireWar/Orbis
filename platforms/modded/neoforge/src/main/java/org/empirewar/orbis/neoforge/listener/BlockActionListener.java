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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.modded.util.FlagActions;
import org.empirewar.orbis.neoforge.OrbisNeoForge;

public final class BlockActionListener {

    private final OrbisNeoForge orbis;

    public BlockActionListener(OrbisNeoForge orbis) {
        this.orbis = orbis;
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (FlagActions.shouldPreventBlockAction(
                (Level) event.getLevel(),
                event.getPos(),
                event.getPlayer(),
                DefaultFlags.CAN_BREAK)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (FlagActions.shouldPreventBlockAction(
                    (Level) event.getLevel(), event.getPos(), player, DefaultFlags.CAN_PLACE)) {
                event.setCanceled(true);
            }
        }
    }
}
