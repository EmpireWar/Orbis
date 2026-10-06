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
package org.empirewar.orbis.sponge.listener;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.minecraft.flags.MinecraftFlags;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.world.RegionisedWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;

public class DamageEntityListener {

    private final Orbis orbis;

    public DamageEntityListener(Orbis orbis) {
        this.orbis = orbis;
    }

    @Listener(order = Order.EARLY)
    public void onDamage(DamageEntityEvent event) {
        final Entity attacked = event.entity();
        if (!(attacked instanceof ServerPlayer)) return;

        final RegionisedWorld world =
                orbis.getRegionisedWorld(attacked.serverLocation().world().key());
        final RegionQuery.FilterableRegionResult<RegionQuery.Position> query =
                world.query(RegionQuery.Position.builder()
                        .position(
                                attacked.position().x(),
                                attacked.position().y(),
                                attacked.position().z()));

        if (query.query(RegionQuery.Flag.builder(MinecraftFlags.INVULNERABILITY))
                .result()
                .orElse(false)) {
            event.setCancelled(true);
        }
    }
}
