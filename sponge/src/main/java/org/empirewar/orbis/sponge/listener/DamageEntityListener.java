/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 EmpireWar
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
package org.empirewar.orbis.sponge.listener;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.flag.DefaultFlags;
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

        if (!query.query(RegionQuery.Flag.builder(DefaultFlags.INVULNERABILITY))
                .result()
                .orElse(false)) {
            event.setCancelled(true);
        }
    }
}
