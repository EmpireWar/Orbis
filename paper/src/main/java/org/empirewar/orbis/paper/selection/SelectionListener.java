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
package org.empirewar.orbis.paper.selection;

import io.papermc.paper.math.BlockPosition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.paper.OrbisPaper;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.joml.Vector3i;

import java.time.Duration;

public record SelectionListener(Orbis api) implements Listener {

    @EventHandler
    public void onLeftRightClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        if (item == null || !OrbisPaper.isWand(item)) return;

        final Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            final Selection selection =
                    api.selectionManager().get(player.getUniqueId()).orElse(null);
            if (selection == null) return;
            final Vector3i last = selection.getPoints().stream()
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (last == null) return;
            selection.removePoint(last);
            player.sendMessage(OrbisText.PREFIX.append(
                    Component.text("Removed the last added point.", OrbisText.SECONDARY_RED)));
            return;
        }

        final Selection selection = api.selectionManager()
                .get(player.getUniqueId())
                .orElseGet(() -> {
                    Selection newSelection = new Selection(AreaType.CUBOID);
                    api.selectionManager().add(player.getUniqueId(), newSelection);
                    player.sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Started a new cuboid selection.", OrbisText.EREBOR_GREEN)));
                    return newSelection;
                });

        Vector3i point;
        final Block block = event.getClickedBlock();
        if (block == null || block.getType().isAir()) {
            final BlockPosition blockPos = player.getLocation().toBlock();
            point = new Vector3i(blockPos.blockX(), blockPos.blockY(), blockPos.blockZ());
        } else {
            point = new Vector3i(block.getX(), block.getY(), block.getZ());
        }

        event.setCancelled(true);

        selection.addPoint(point);
        final TextComponent teleportPart = Component.text(
                        "[" + point.x + ", " + point.y + ", " + point.z + "]",
                        OrbisText.EREBOR_GREEN)
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to teleport.", OrbisText.EREBOR_GREEN)))
                .clickEvent(ClickEvent.callback(
                        audience -> player.teleport(new Location(
                                player.getWorld(),
                                point.x,
                                point.y,
                                point.z,
                                player.getYaw(),
                                player.getPitch())),
                        ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(3))
                                .build()));
        player.sendMessage(
                OrbisText.PREFIX.append(Component.text("Added point ", OrbisText.EREBOR_GREEN)
                        .append(teleportPart)
                        .append(Component.text(" to selection.", OrbisText.EREBOR_GREEN))));
    }
}
