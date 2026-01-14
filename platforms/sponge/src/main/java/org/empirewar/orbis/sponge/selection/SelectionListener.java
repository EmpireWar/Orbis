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
package org.empirewar.orbis.sponge.selection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.minecraft.command.Permissions;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.sponge.key.SpongeDataKeys;
import org.empirewar.orbis.util.OrbisText;
import org.empirewar.orbis.util.OrbisTranslations;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.math.vector.Vector3d;

import java.time.Duration;

public final class SelectionListener {

    private final Orbis api;

    public SelectionListener(Orbis api) {
        this.api = api;
    }

    @Listener
    public void onRightClick(InteractItemEvent.Secondary.Pre event, @Root ServerPlayer player) {
        if (!player.hasPermission(Permissions.MANAGE)) return;

        final ItemStackSnapshot item =
                event.context().get(EventContextKeys.USED_ITEM).orElse(ItemStackSnapshot.empty());
        if (item.isEmpty() || !item.getOrElse(SpongeDataKeys.IS_WAND, false)) return;

        final Selection selection =
                api.selectionManager().get(player.uniqueId()).orElse(null);
        if (selection == null) {
            player.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.SELECTION_NOT_ACTIVE));
            return;
        }

        final Vector3ic last =
                selection.getPoints().stream().reduce((first, second) -> second).orElse(null);
        if (last == null) return;
        selection.removePoint(last);
        player.sendMessage(OrbisText.PREFIX.append(OrbisTranslations.SELECTION_POINT_REMOVED));
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
        player.sendMessage(OrbisText.PREFIX
                .append(OrbisTranslations.SELECTION_POINT_ADDED.arguments(
                        Component.text(point.x), Component.text(point.y), Component.text(point.z)))
                .hoverEvent(HoverEvent.showText(OrbisTranslations.GENERIC_CLICK_TO_TELEPORT))
                .clickEvent(ClickEvent.callback(
                        audience -> player.setPosition(new Vector3d(point.x, point.y, point.z)),
                        ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(3))
                                .build())));

        event.setCancelled(true);
    }
}
