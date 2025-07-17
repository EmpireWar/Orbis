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
