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
package org.empirewar.orbis.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.DefaultFlags;
import org.empirewar.orbis.paper.command.Commands;
import org.empirewar.orbis.query.RegionQuery;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.world.RegionisedWorld;
import org.empirewar.orbis.world.RegionisedWorldSet;
import org.joml.Vector3d;

import java.util.Set;
import java.util.UUID;

public class OrbisPaper extends JavaPlugin implements Orbis, Listener {

    private final RegionisedWorldSet set = new RegionisedWorldSet();

    @Override
    public void onLoad() {
        OrbisAPI.set(this);
    }

    @Override
    public void onEnable() {
        new Commands(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        Vector3d pos = new Vector3d(block.getX(), block.getY(), block.getZ());
        final Set<Region> regionsAtPos = set.query(RegionQuery.Position.builder().position(pos).build()).result();
        System.out.println("regions at pos: " + regionsAtPos);
        for (Region region : regionsAtPos) {
            System.out.println("result: "
                    + region.query(RegionQuery.Flag.<Boolean>builder().flag(DefaultFlags.CAN_BREAK).build()).result());
            if (!region.query(RegionQuery.Flag.<Boolean>builder().flag(DefaultFlags.CAN_BREAK).build()).result()
                    .orElse(true)) {
                System.out.println("cannot break!");
                event.getPlayer()
                        .sendMessage(Component.text("Hey!", NamedTextColor.RED, TextDecoration.BOLD)
                                .append(Component.space())
                                .append(Component.text("Sorry, but you can't break that here.", NamedTextColor.GRAY)));
                event.setCancelled(true);
            }
        }
    }

    @Override
    public RegionisedWorld getRegionisedWorld(UUID worldId) {
        return set;
    }
}
