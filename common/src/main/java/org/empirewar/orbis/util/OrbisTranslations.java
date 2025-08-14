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
package org.empirewar.orbis.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public final class OrbisTranslations {

    private OrbisTranslations() {}

    public static final TranslatableComponent GENERIC_CLICK_TO_TELEPORT =
            Component.translatable("generic.click_to_teleport");

    public static final TranslatableComponent ENABLE_REGION_VISUALISATION =
            Component.translatable("command.region.visualisation.enabled");
    public static final TranslatableComponent DISABLE_REGION_VISUALISATION =
            Component.translatable("command.region.visualisation.disabled");

    public static final TranslatableComponent REGION_ALREADY_EXISTS =
            Component.translatable("command.region.already_exists");
    public static final TranslatableComponent REGION_SELECTION_REQUIRED =
            Component.translatable("command.region.selection_required");
    public static final TranslatableComponent REGION_SELECTION_TYPE_MISMATCH =
            Component.translatable("command.region.selection_type_mismatch");
    public static final TranslatableComponent REGION_INCOMPLETE_SELECTION =
            Component.translatable("command.region.incomplete_selection");
    public static final TranslatableComponent REGION_USED_SELECTION_NOTE = Component.translatable(
            "command.region.used_selection_note", OrbisText.SECONDARY_ORANGE);
    public static final TranslatableComponent REGION_CREATED =
            Component.translatable("command.region.created");
    public static final TranslatableComponent REGION_GLOBAL_AREA_NOT_SUPPORTED =
            Component.translatable("command.region.global_area_not_supported");
    public static final TranslatableComponent REGION_NO_ACTIVE_SELECTION =
            Component.translatable("command.region.no_active_selection");
    public static final TranslatableComponent REGION_SET_AREA_TYPE_MISMATCH =
            Component.translatable("command.region.setarea_type_mismatch");
    public static final TranslatableComponent REGION_SET_AREA_SUCCESS =
            Component.translatable("command.region.setarea_success");
    public static final TranslatableComponent REGION_SELECTED_AREA =
            Component.translatable("command.region.selarea");
    public static final TranslatableComponent REGION_REMOVE_SUCCESS =
            Component.translatable("command.region.remove_success");
    public static final TranslatableComponent REGION_REMOVE_FAILED =
            Component.translatable("command.region.remove_failed");
    public static final TranslatableComponent REGION_INVALID_GROUP =
            Component.translatable("command.region.invalid_group");
    public static final TranslatableComponent REGION_FLAG_ADDED =
            Component.translatable("command.region.flag_added");
    public static final TranslatableComponent REGION_FLAG_REMOVED =
            Component.translatable("command.region.flag_removed");
    public static final TranslatableComponent REGION_FLAG_SET =
            Component.translatable("command.region.flag_set");
    public static final TranslatableComponent REGION_PRIORITY_GLOBAL =
            Component.translatable("command.region.priority_global");
    public static final TranslatableComponent REGION_PRIORITY_SET =
            Component.translatable("command.region.priority_set");
    public static final TranslatableComponent REGION_PARENT_GLOBAL =
            Component.translatable("command.region.parent_global");
    public static final TranslatableComponent REGION_PARENT_SELF =
            Component.translatable("command.region.parent_self");
    public static final TranslatableComponent REGION_PARENT_ALREADY =
            Component.translatable("command.region.parent_already");
    public static final TranslatableComponent REGION_PARENT_ADDED =
            Component.translatable("command.region.parent_added");
    public static final TranslatableComponent REGION_PARENT_CIRCULAR =
            Component.translatable("command.region.parent_circular");
    public static final TranslatableComponent REGION_PARENT_REMOVED =
            Component.translatable("command.region.parent_removed");
    public static final TranslatableComponent REGION_MEMBER_PLAYER_ADDED =
            Component.translatable("command.region.member_player_added");
    public static final TranslatableComponent REGION_MEMBER_PLAYER_REMOVED =
            Component.translatable("command.region.member_player_removed");
    public static final TranslatableComponent REGION_MEMBER_PLAYER_NOT_FOUND =
            Component.translatable("command.region.member_player_not_found");
    public static final TranslatableComponent REGION_MEMBER_PERMISSION_ADDED =
            Component.translatable("command.region.member_permission_added");
    public static final TranslatableComponent REGION_MEMBER_PERMISSION_REMOVED =
            Component.translatable("command.region.member_permission_removed");
    public static final TranslatableComponent REGION_MEMBER_PERMISSION_NOT_FOUND =
            Component.translatable("command.region.member_permission_not_found");

    public static final TranslatableComponent REGION_WORLD_GLOBAL =
            Component.translatable("command.region.world_global");
    public static final TranslatableComponent REGION_WORLD_ADDED =
            Component.translatable("command.region.world_added");
    public static final TranslatableComponent REGION_WORLD_ADD_FAILED =
            Component.translatable("command.region.world_add_failed");
    public static final TranslatableComponent REGION_WORLD_REMOVED =
            Component.translatable("command.region.world_removed");
    public static final TranslatableComponent REGION_WORLD_REMOVE_FAILED =
            Component.translatable("command.region.world_remove_failed");
    public static final TranslatableComponent REGION_POINT_ADDED =
            Component.translatable("command.region.point_added");
    public static final TranslatableComponent REGION_POINT_ADD_FAILED =
            Component.translatable("command.region.point_add_failed");
    public static final TranslatableComponent REGION_LIST_EMPTY =
            Component.translatable("command.region.list_empty");

    public static final TranslatableComponent SELECTION_SET_TYPE =
            Component.translatable("command.selection.set_type");
    public static final TranslatableComponent SELECTION_CLEARED =
            Component.translatable("command.selection.cleared");
    public static final TranslatableComponent SELECTION_NOT_ACTIVE =
            Component.translatable("command.selection.not_active");
    public static final TranslatableComponent SELECTION_POINT_ADDED =
            Component.translatable("command.selection.point_added");
    public static final TranslatableComponent SELECTION_POINT_REMOVED =
            Component.translatable("command.selection.point_removed");
}
