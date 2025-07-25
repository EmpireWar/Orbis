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
package org.empirewar.orbis.selection;

import static net.kyori.adventure.text.Component.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.area.AreaType;
import org.empirewar.orbis.area.CuboidArea;
import org.empirewar.orbis.area.EncompassingArea;
import org.empirewar.orbis.area.PolygonArea;
import org.empirewar.orbis.area.PolyhedralArea;
import org.empirewar.orbis.area.SphericalArea;
import org.empirewar.orbis.exception.IncompleteAreaException;
import org.empirewar.orbis.util.OrbisText;
import org.joml.Vector3ic;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Selection {

    private static final Component RESET_LORE =
            text().decoration(TextDecoration.ITALIC, false).build();

    public static final Component WAND_NAME = text("Orbis Wand", OrbisText.MAIN);
    public static final List<Component> WAND_LORE = List.of(
            RESET_LORE
                    .append(text("Left-Click", OrbisText.EREBOR_GREEN, TextDecoration.BOLD))
                    .append(text(" to add a point.", OrbisText.EREBOR_GREEN)),
            RESET_LORE
                    .append(text("Right-Click", OrbisText.SECONDARY_RED, TextDecoration.BOLD))
                    .append(text(" to remove the last added point.", OrbisText.SECONDARY_RED)));

    private AreaType<?> selectionType;
    private final Set<Vector3ic> points;

    public Selection(AreaType<?> selectionType) {
        this.selectionType = selectionType;
        this.points = new LinkedHashSet<>();
    }

    public AreaType<?> getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(AreaType<?> selectionType) {
        this.selectionType = selectionType;
    }

    public Set<Vector3ic> getPoints() {
        return Set.copyOf(points);
    }

    public void addPoint(Vector3ic point) {
        this.points.add(point);
    }

    public void removePoint(Vector3ic point) {
        this.points.remove(point);
    }

    public void clear() {
        points.clear();
    }

    public Area build() throws IncompleteAreaException {
        EncompassingArea area;
        if (selectionType == AreaType.CUBOID) {
            area = new CuboidArea();
        } else if (selectionType == AreaType.POLYHEDRAL) {
            area = new PolyhedralArea();
        } else if (selectionType == AreaType.SPHERE) {
            area = new SphericalArea();
        } else {
            area = new PolygonArea();
        }

        final Optional<Integer> max = area.getMaximumPoints();
        final int min = area.getMinimumPoints();
        if (max.isPresent() && points.size() > max.get()) {
            throw new IncompleteAreaException(
                    "Expected at most " + max.get() + " points, but got " + points.size());
        } else if (points.size() < min) {
            throw new IncompleteAreaException(
                    "Expected at least " + min + " points, but got " + points.size());
        }

        points.forEach(area::addPoint);
        return area;
    }
}
