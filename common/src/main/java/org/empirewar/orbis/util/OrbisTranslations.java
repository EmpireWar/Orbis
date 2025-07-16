/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2025 Empire War
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
package org.empirewar.orbis.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public final class OrbisTranslations {

    private OrbisTranslations() {}

    public static final TranslatableComponent ENABLE_REGION_VISUALISATION =
            Component.translatable("command.region.visualisation.enabled", OrbisText.EREBOR_GREEN);

    public static final TranslatableComponent DISABLE_REGION_VISUALISATION =
            Component.translatable("command.region.visualisation.disabled", OrbisText.EREBOR_GREEN);

    public static final TranslatableComponent REGION_ALREADY_EXISTS =
            Component.translatable("command.region.already_exists", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_SELECTION_REQUIRED =
            Component.translatable("command.region.selection_required", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_SELECTION_TYPE_MISMATCH =
            Component.translatable("command.region.selection_type_mismatch", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_INCOMPLETE_SELECTION =
            Component.translatable("command.region.incomplete_selection", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_USED_SELECTION_NOTE =
            Component.translatable("command.region.used_selection_note", OrbisText.SECONDARY_ORANGE);
    public static final TranslatableComponent REGION_CREATED =
            Component.translatable("command.region.created", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_GLOBAL_AREA_NOT_SUPPORTED =
            Component.translatable("command.region.global_area_not_supported", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_NO_ACTIVE_SELECTION =
            Component.translatable("command.region.no_active_selection", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_SET_AREA_TYPE_MISMATCH =
            Component.translatable("command.region.setarea_type_mismatch", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_SET_AREA_SUCCESS =
            Component.translatable("command.region.setarea_success", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_REMOVE_SUCCESS =
            Component.translatable("command.region.remove_success", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_REMOVE_FAILED =
            Component.translatable("command.region.remove_failed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_INVALID_GROUP =
            Component.translatable("command.region.invalid_group", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_FLAG_ADDED =
            Component.translatable("command.region.flag_added", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_FLAG_REMOVED =
            Component.translatable("command.region.flag_removed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_FLAG_SET =
            Component.translatable("command.region.flag_set", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_PRIORITY_GLOBAL =
            Component.translatable("command.region.priority_global", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_PRIORITY_SET =
            Component.translatable("command.region.priority_set", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_PARENT_GLOBAL =
            Component.translatable("command.region.parent_global", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_PARENT_SELF =
            Component.translatable("command.region.parent_self", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_PARENT_ALREADY =
            Component.translatable("command.region.parent_already", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_PARENT_ADDED =
            Component.translatable("command.region.parent_added", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_PARENT_CIRCULAR =
            Component.translatable("command.region.parent_circular", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_PARENT_REMOVED =
            Component.translatable("command.region.parent_removed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent SELECTION_SET_TYPE =
            Component.translatable("command.selection.set_type", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent SELECTION_CLEARED =
            Component.translatable("command.selection.cleared", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent SELECTION_NOT_ACTIVE =
            Component.translatable("command.selection.not_active", OrbisText.SECONDARY_RED);

    public static final TranslatableComponent REGION_WORLD_GLOBAL =
            Component.translatable("command.region.world_global", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_WORLD_ADDED =
            Component.translatable("command.region.world_added", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_WORLD_ADD_FAILED =
            Component.translatable("command.region.world_add_failed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_WORLD_REMOVED =
            Component.translatable("command.region.world_removed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_WORLD_REMOVE_FAILED =
            Component.translatable("command.region.world_remove_failed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_POINT_ADDED =
            Component.translatable("command.region.point_added", OrbisText.EREBOR_GREEN);
    public static final TranslatableComponent REGION_POINT_ADD_FAILED =
            Component.translatable("command.region.point_add_failed", OrbisText.SECONDARY_RED);
    public static final TranslatableComponent REGION_LIST_EMPTY =
            Component.translatable("command.region.list_empty", OrbisText.SECONDARY_RED);
}
