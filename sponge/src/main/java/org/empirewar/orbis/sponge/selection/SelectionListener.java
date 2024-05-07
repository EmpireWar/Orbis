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
package org.empirewar.orbis.sponge.selection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.command.Permissions;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.sponge.key.SpongeDataKeys;
import org.empirewar.orbis.util.OrbisText;
import org.joml.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.math.vector.Vector3d;

import java.time.Duration;

public final class SelectionListener {

    private final Orbis api;

    public SelectionListener(Orbis api) {
        this.api = api;
    }

    @Listener
    public void onRightClick(InteractItemEvent.Secondary event, @Root ServerPlayer player) {
        if (!player.hasPermission(Permissions.MANAGE)) return;

        final ItemStackSnapshot item =
                event.context().get(EventContextKeys.USED_ITEM).orElse(ItemStackSnapshot.empty());
        if (item.isEmpty() || !item.getOrElse(SpongeDataKeys.IS_WAND, false)) return;

        final Selection selection =
                api.selectionManager().get(player.uniqueId()).orElse(null);
        if (selection == null) {
            player.sendMessage(OrbisText.PREFIX.append(Component.text(
                    "You don't have an active selection.", OrbisText.SECONDARY_RED)));
            return;
        }

        final Vector3i last =
                selection.getPoints().stream().reduce((first, second) -> second).orElse(null);
        if (last == null) return;
        selection.removePoint(last);
        player.sendMessage(OrbisText.PREFIX.append(
                Component.text("Removed the last added point.", OrbisText.SECONDARY_RED)));
    }

    @Listener
    public void onLeftClick(InteractBlockEvent.Primary.Start event, @Root ServerPlayer player) {
        if (!player.hasPermission(Permissions.MANAGE)) return;

        final ItemStackSnapshot item =
                event.context().get(EventContextKeys.USED_ITEM).orElse(ItemStackSnapshot.empty());
        if (item.isEmpty() || !item.getOrElse(SpongeDataKeys.IS_WAND, false)) return;

        final Selection selection = api.selectionManager()
                .get(player.uniqueId())
                .orElseGet(() -> {
                    Selection newSelection = new Selection(AreaType.CUBOID);
                    api.selectionManager().add(player.uniqueId(), newSelection);
                    player.sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Started a new cuboid selection.", OrbisText.EREBOR_GREEN)));
                    return newSelection;
                });

        Vector3i point;
        final BlockSnapshot block = event.block();
        if (block == null
                || block.state()
                        .type()
                        .isAnyOf(
                                BlockTypes.AIR.get(),
                                BlockTypes.CAVE_AIR.get(),
                                BlockTypes.VOID_AIR.get())) {
            final org.spongepowered.math.vector.Vector3i blockPos = player.blockPosition();
            point = new Vector3i(blockPos.x(), blockPos.y(), blockPos.z());
        } else {
            final org.spongepowered.math.vector.Vector3i blockPos = block.position();
            point = new Vector3i(blockPos.x(), blockPos.y(), blockPos.z());
        }

        selection.addPoint(point);
        final TextComponent teleportPart = Component.text(
                        "[" + point.x + ", " + point.y + ", " + point.z + "]",
                        OrbisText.EREBOR_GREEN)
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to teleport.", OrbisText.EREBOR_GREEN)))
                .clickEvent(ClickEvent.callback(
                        audience -> player.setPosition(new Vector3d(point.x, point.y, point.z)),
                        ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(3))
                                .build()));
        player.sendMessage(
                OrbisText.PREFIX.append(Component.text("Added point ", OrbisText.EREBOR_GREEN)
                        .append(teleportPart)
                        .append(Component.text(" to selection.", OrbisText.EREBOR_GREEN))));
    }

    @Listener
    public void onBreak(ChangeBlockEvent.All event, @Root ServerPlayer player) {
        final ItemStack item = player.itemInHand(HandTypes.MAIN_HAND.get());
        if (item.isEmpty() || !item.getOrElse(SpongeDataKeys.IS_WAND, false)) return;
        event.setCancelled(true);
    }
}
