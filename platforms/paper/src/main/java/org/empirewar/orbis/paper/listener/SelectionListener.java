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
package org.empirewar.orbis.paper.listener;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
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
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.util.Permissions;
import org.empirewar.orbis.paper.OrbisPaperPlatform;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.util.OrbisTranslations;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.time.Duration;

public record SelectionListener(OrbisPaperPlatform<?> api) implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission(Permissions.MANAGE)) return;

        final ItemStack item = event.getItem();
        if (item == null || !api.isWand(item)) return;

        final Audience audience = api.senderAsAudience(player);

        final Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            final Selection selection =
                    api.selectionManager().get(player.getUniqueId()).orElse(null);
            if (selection == null) {
                audience.sendMessage(
                        OrbisText.PREFIX.append(OrbisTranslations.SELECTION_NOT_ACTIVE));
                return;
            }

            final Vector3ic last = selection.getPoints().stream()
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (last == null) return;
            selection.removePoint(last);
            audience.sendMessage(
                    OrbisText.PREFIX.append(OrbisTranslations.SELECTION_POINT_REMOVED));
            return;
        }

        final Selection selection = api.selectionManager()
                .get(player.getUniqueId())
                .orElseGet(() -> {
                    Selection newSelection = new Selection(AreaType.CUBOID);
                    api.selectionManager().add(player.getUniqueId(), newSelection);
                    audience.sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Started a new cuboid selection.", OrbisText.EREBOR_GREEN)));
                    return newSelection;
                });

        Vector3i point;
        final Block block = event.getClickedBlock();
        if (block == null || block.getType().isAir()) {
            final Location blockPos = player.getLocation();
            point = new Vector3i(blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ());
        } else {
            point = new Vector3i(block.getX(), block.getY(), block.getZ());
        }

        event.setCancelled(true);

        selection.addPoint(point);
        player.sendMessage(OrbisText.PREFIX
                .append(OrbisTranslations.SELECTION_POINT_ADDED.arguments(
                        Component.text(point.x), Component.text(point.y), Component.text(point.z)))
                .hoverEvent(HoverEvent.showText(OrbisTranslations.GENERIC_CLICK_TO_TELEPORT))
                .clickEvent(ClickEvent.callback(
                        u -> player.teleport(new Location(
                                player.getWorld(),
                                point.x,
                                point.y,
                                point.z,
                                player.getLocation().getYaw(),
                                player.getLocation().getPitch())),
                        ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(3))
                                .build())));
    }
}
