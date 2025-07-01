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
import org.joml.Vector3ic;
import org.junit.jupiter.api.Test;

import java.util.*;

public class AreaBoundaryPointsTest {

    @Test
    void testPolygonAreaBoundaryPoints() {
        PolygonArea area = new PolygonArea();
        // Square: (0,0,0)-(2,0,0)-(2,0,2)-(0,0,2)
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(2, 0, 0));
        area.addPoint(new Vector3i(2, 0, 2));
        area.addPoint(new Vector3i(0, 0, 2));
        Set<Vector3ic> boundary = area.getBoundaryPoints();
        Set<Vector3ic> expected = new HashSet<>(Arrays.asList(
                new Vector3i(0, 0, 0),
                new Vector3i(1, 0, 0),
                new Vector3i(2, 0, 0),
                new Vector3i(1, 0, 1),
                new Vector3i(2, 0, 2),
                new Vector3i(1, 0, 2),
                new Vector3i(0, 0, 2)));
        if (!expected.equals(boundary)) {
            System.out.println("Expected: " + expected);
            System.out.println("Actual:   " + boundary);
            Set<Vector3ic> missing = new HashSet<>(expected);
            missing.removeAll(boundary);
            Set<Vector3ic> extra = new HashSet<>(boundary);
            extra.removeAll(expected);
            System.out.println("Missing:  " + missing);
            System.out.println("Extra:    " + extra);
        }
        assertEquals(
                expected,
                boundary,
                "PolygonArea boundary should contain all integer perimeter points");
    }

    @Test
    void testPolyhedralAreaBoundaryPoints() {
        PolyhedralArea area = new PolyhedralArea();
        // Tetrahedron (simple triangular pyramid)
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(1, 0, 0));
        area.addPoint(new Vector3i(0, 1, 0));
        area.addPoint(new Vector3i(0, 0, 1));
        Set<Vector3ic> boundary = area.getBoundaryPoints();
        // Should include all points along the tetrahedron's edges
        assertTrue(boundary.contains(new Vector3i(0, 0, 0)), "Should contain a vertex");
        assertTrue(boundary.contains(new Vector3i(1, 0, 0)), "Should contain a vertex");
        assertTrue(boundary.contains(new Vector3i(0, 1, 0)), "Should contain a vertex");
        assertTrue(boundary.contains(new Vector3i(0, 0, 1)), "Should contain a vertex");
        // Edge points
        assertTrue(boundary.contains(new Vector3i(0, 0, 0)), "Should contain edge start");
        assertTrue(boundary.contains(new Vector3i(1, 0, 0)), "Should contain edge end");
    }

    @Test
    void testSphericalAreaBoundaryPoints() {
        SphericalArea area = new SphericalArea(new Vector3i(0, 0, 0), 1.0);
        Set<Vector3ic> boundary = area.getBoundaryPoints();
        // Should contain points at radius 1 from center (0,0,0), e.g. (1,0,0), (0,1,0), (0,0,1),
        // etc.
        boolean found = false;
        for (Vector3ic v : boundary) {
            int distSq = v.x() * v.x() + v.y() * v.y() + v.z() * v.z();
            if (distSq == 1) {
                found = true;
                break;
            }
        }
        assertTrue(found, "SphericalArea boundary should contain points at radius 1");
    }
}
