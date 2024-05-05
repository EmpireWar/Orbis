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
package org.empirewar.orbis.selection;

import static net.kyori.adventure.text.Component.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.EncompassingArea;
import org.empirewar.orbis.area.PolygonArea;
import org.empirewar.orbis.exception.IncompleteAreaException;
import org.empirewar.orbis.util.OrbisText;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Selection {

    public static final Component WAND_NAME = text("Orbis Wand", OrbisText.MAIN);
    public static final List<Component> WAND_LORE = List.of(
            text("Left-Click", OrbisText.EREBOR_GREEN, TextDecoration.BOLD)
                    .append(text(" to add a point.", OrbisText.EREBOR_GREEN)),
            text("Right-Click", OrbisText.SECONDARY_RED, TextDecoration.BOLD)
                    .append(text(" to remove the last added point.", OrbisText.SECONDARY_RED)));

    private AreaType<?> selectionType;
    private final Set<Vector3i> points;

    public Selection(AreaType<?> selectionType) {
        this.selectionType = selectionType;
        this.points = new HashSet<>();
    }

    public AreaType<?> getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(AreaType<?> selectionType) {
        this.selectionType = selectionType;
    }

    public Set<Vector3i> getPoints() {
        return Set.copyOf(points);
    }

    public void addPoint(Vector3i point) {
        this.points.add(point);
    }

    public void removePoint(Vector3i point) {
        this.points.remove(point);
    }

    public void clear() {
        points.clear();
    }

    public Area build() throws IncompleteAreaException {
        EncompassingArea area;
        if (selectionType == AreaType.CUBOID) {
            area = new CuboidArea();
        } else {
            area = new PolygonArea();
        }

        if (area.getExpectedPoints().isPresent()
                && area.getExpectedPoints().get() != points.size()) {
            throw new IncompleteAreaException();
        }

        points.forEach(area::addPoint);
        return area;
    }
}
