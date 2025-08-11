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
package org.empirewar.orbis.neoforge.selection;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;

import org.empirewar.orbis.Orbis;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.neoforge.OrbisNeoForge;
import org.empirewar.orbis.selection.Selection;
import org.empirewar.orbis.util.OrbisText;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.time.Duration;

public final class SelectionListener {

    private final Orbis api;

    public SelectionListener(Orbis api) {
        this.api = api;
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (!PermissionAPI.getPermission(player, OrbisNeoForge.ORBIS_MANAGE)) return;

        ItemStack item = player.getItemInHand(event.getHand());
        if (item.isEmpty()
                || !item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .contains("orbis_is_wand")) {
            event.setCancellationResult(InteractionResult.PASS);
            return;
        }

        Selection selection = api.selectionManager().get(player.getUUID()).orElse(null);
        if (selection == null) {
            ((Audience) player)
                    .sendMessage(OrbisText.PREFIX.append(Component.text(
                            "You don't have an active selection.", OrbisText.SECONDARY_RED)));
            event.setCancellationResult(InteractionResult.PASS);
            return;
        }

        Vector3ic last =
                selection.getPoints().stream().reduce((first, second) -> second).orElse(null);
        if (last == null) {
            event.setCancellationResult(InteractionResult.PASS);
            return;
        }

        selection.removePoint(last);
        ((Audience) player)
                .sendMessage(OrbisText.PREFIX.append(
                        Component.text("Removed the last added point.", OrbisText.SECONDARY_RED)));
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;

        if (handleLeftClick((ServerPlayer) event.getEntity(), event.getPos(), event.getHand())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        handleLeftClick((ServerPlayer) event.getEntity(), event.getPos(), event.getHand());
    }

    private boolean handleLeftClick(ServerPlayer player, BlockPos blockPos, InteractionHand hand) {
        if (!PermissionAPI.getPermission(player, OrbisNeoForge.ORBIS_MANAGE)) return false;

        ItemStack item = player.getItemInHand(hand);
        if (item.isEmpty()
                || !item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .contains("orbis_is_wand")) return false;

        Selection selection = api.selectionManager().get(player.getUUID()).orElseGet(() -> {
            Selection newSelection = new Selection(AreaType.CUBOID);
            api.selectionManager().add(player.getUUID(), newSelection);
            ((Audience) player)
                    .sendMessage(OrbisText.PREFIX.append(Component.text(
                            "Started a new cuboid selection.", OrbisText.EREBOR_GREEN)));
            return newSelection;
        });

        Vector3i point;
        BlockState block = player.level().getBlockState(blockPos);
        if (block.isAir()) {
            BlockPos playerPos = player.blockPosition();
            point = new Vector3i(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        } else {
            point = new Vector3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        selection.addPoint(point);
        TextComponent teleportPart = Component.text(
                        "[" + point.x + ", " + point.y + ", " + point.z + "]",
                        OrbisText.EREBOR_GREEN)
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to teleport.", OrbisText.EREBOR_GREEN)))
                .clickEvent(ClickEvent.callback(
                        audience -> player.teleportTo(point.x, point.y, point.z),
                        ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(3))
                                .build()));

        ((Audience) player)
                .sendMessage(OrbisText.PREFIX.append(
                        Component.text("Added point ", OrbisText.EREBOR_GREEN)
                                .append(teleportPart)
                                .append(Component.text(" to selection.", OrbisText.EREBOR_GREEN))));

        return true;
    }
}
