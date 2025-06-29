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
package org.empirewar.orbis.area;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

public class SphericalAreaTest {

    @Test
    void testContains() {
        SphericalArea area = new SphericalArea(new Vector3i(0, 0, 0), 5.0);
        // Inside
        assertTrue(area.contains(0, 0, 0));
        assertTrue(area.contains(3, 4, 0)); // 3-4-5 triangle
        // On the edge
        assertTrue(area.contains(5, 0, 0));
        // Outside
        assertFalse(area.contains(6, 0, 0));
        assertFalse(area.contains(0, 0, 5.1));
    }

    @Test
    void testCenterAndRadius() {
        Vector3i center = new Vector3i(1, 2, 3);
        double radius = 2.5;
        SphericalArea area = new SphericalArea(center, radius);
        assertEquals(center, area.getCenter());
        assertEquals(radius, area.getRadius());
    }

    @Test
    void testMinMax() {
        SphericalArea area = new SphericalArea(new Vector3i(10, 10, 10), 3);
        assertEquals(new Vector3i(7, 7, 7), area.getMin());
        assertEquals(new Vector3i(13, 13, 13), area.getMax());
    }
}
