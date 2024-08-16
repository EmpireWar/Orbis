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
package org.empirewar.orbis.area;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Vector3i;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PolygonAreaTest {

    @Test
    @Order(1)
    void testContains() {
        PolygonArea area = new PolygonArea();
        // Make a triangle
        assertTrue(area.addPoint(new Vector3i(0, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(3, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(3, 0, 4)));

        assertTrue(area.contains(2, 0, 1.6));
        // TODO how to fix algorithm so that a position on a block that a polygon line passes over
        // is considered valid?
        //        assertTrue(area.contains(3, 0, 4));
        assertFalse(area.contains(4, 0, 4));
        assertFalse(area.contains(6, 0, 4));

        area.clearPoints();

        List<Vector3i> squarePolygon = Arrays.asList(
                new Vector3i(1, 0, 1), // Bottom-left corner
                new Vector3i(5, 0, 1), // Bottom-right corner
                new Vector3i(5, 0, 5), // Top-right corner
                new Vector3i(1, 0, 5) // Top-left corner
                );
        squarePolygon.forEach(area::addPoint);
        assertTrue(area.contains(3, 0, 3));
        assertFalse(area.contains(6, 0, 3));

        area.clearPoints();

        List<Vector3i> pentagonPolygon = Arrays.asList(
                new Vector3i(3, 0, 1), // Bottom vertex
                new Vector3i(6, 0, 3), // Bottom-right vertex
                new Vector3i(5, 0, 6), // Top-right vertex
                new Vector3i(1, 0, 6), // Top-left vertex
                new Vector3i(0, 0, 3) // Bottom-left vertex
                );
        pentagonPolygon.forEach(area::addPoint);
        // TODO THIS DOESN'T EVEN WORK WITH WORLDGUARD ALGO I DON'T UNDERSTAND IS THE UNIVERSE BUGGED?
        //        assertTrue(area.contains(3, 0, 4));
        assertFalse(area.contains(7, 0, 3));
    }
}
