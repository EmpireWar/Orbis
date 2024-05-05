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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PolygonAreaTest {

    @Test
    @Order(1)
    void testContains() {
        PolygonArea area = new PolygonArea();
        // Make a triangle
        assertTrue(area.addPoint(new Vector3i(0, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(6, 0, 0)));
        assertTrue(area.addPoint(new Vector3i(3, 0, 8)));
        assertTrue(area.contains(3, 0, 4));
        assertTrue(area.contains(4, 0, 4));
        // TODO how to fix algorithm so that a position on a block that a polygon line passes over is considered valid?
//        assertTrue(area.contains(5.5, 0, 4.5));
        assertFalse(area.contains(6, 0, 4));
    }
}
