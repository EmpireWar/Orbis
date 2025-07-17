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
                new Vector3i(2, 0, 1),
                new Vector3i(2, 0, 2),
                new Vector3i(1, 0, 2),
                new Vector3i(0, 0, 2),
                new Vector3i(0, 0, 1)));
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

    @Test
    void testCuboidAreaBoundaryPoints() {
        CuboidArea area = new CuboidArea();
        // Define a cuboid from (0,0,0) to (2,2,2)
        area.addPoint(new Vector3i(0, 0, 0));
        area.addPoint(new Vector3i(2, 2, 2));
        Set<Vector3ic> boundary = area.getBoundaryPoints();

        // All 8 corners should be present
        Vector3ic[] corners = {
            new Vector3i(0, 0, 0),
            new Vector3i(2, 0, 0),
            new Vector3i(2, 2, 0),
            new Vector3i(0, 2, 0),
            new Vector3i(0, 0, 2),
            new Vector3i(2, 0, 2),
            new Vector3i(2, 2, 2),
            new Vector3i(0, 2, 2)
        };
        for (Vector3ic corner : corners) {
            assertTrue(boundary.contains(corner), "Should contain cuboid corner: " + corner);
        }

        // Check some edge points
        assertTrue(boundary.contains(new Vector3i(1, 0, 0)), "Should contain edge point (1,0,0)");
        assertTrue(boundary.contains(new Vector3i(2, 1, 0)), "Should contain edge point (2,1,0)");
        assertTrue(boundary.contains(new Vector3i(0, 2, 1)), "Should contain edge point (0,2,1)");
        assertTrue(boundary.contains(new Vector3i(1, 2, 2)), "Should contain edge point (1,2,2)");

        // The total number of unique boundary points should be correct for a 3x3x3 cuboid
        // There are 12 edges, each of length 2 (so 3 points per edge, but corners overlap)
        // For a 3x3x3 cuboid, the expected number is 36
        Set<Vector3ic> expected = new HashSet<>();
        // Generate all edge points for a cuboid from (0,0,0) to (2,2,2)
        int[][] edges = {
            {0, 0, 0, 2, 0, 0},
            {2, 0, 0, 2, 2, 0},
            {2, 2, 0, 0, 2, 0},
            {0, 2, 0, 0, 0, 0}, // bottom
            {0, 0, 2, 2, 0, 2},
            {2, 0, 2, 2, 2, 2},
            {2, 2, 2, 0, 2, 2},
            {0, 2, 2, 0, 0, 2}, // top
            {0, 0, 0, 0, 0, 2},
            {2, 0, 0, 2, 0, 2},
            {0, 2, 0, 0, 2, 2},
            {2, 2, 0, 2, 2, 2} // sides
        };
        for (int[] e : edges) {
            int x1 = e[0], y1 = e[1], z1 = e[2], x2 = e[3], y2 = e[4], z2 = e[5];
            int dx = Integer.compare(x2, x1);
            int dy = Integer.compare(y2, y1);
            int dz = Integer.compare(z2, z1);
            int length =
                    Math.max(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)), Math.abs(z2 - z1));
            for (int i = 0; i <= length; i++) {
                expected.add(new Vector3i(x1 + dx * i, y1 + dy * i, z1 + dz * i));
            }
        }
        if (boundary.size() != expected.size()) {
            System.out.println("Expected: " + expected.size());
            System.out.println("Actual:   " + boundary.size());
            Set<Vector3ic> missing = new HashSet<>(expected);
            missing.removeAll(boundary);
            Set<Vector3ic> extra = new HashSet<>(boundary);
            extra.removeAll(expected);
            System.out.println("Missing:  " + missing);
            System.out.println("Extra:    " + extra);
        }
        assertEquals(
                expected.size(),
                boundary.size(),
                "Cuboid boundary should have " + expected.size() + " unique points");
    }

    @Test
    void testLargePolygonAreaBoundaryPerformance() {
        int[][] points = new int[][] {
            {1483, 442, 5451},
            {1809, 442, 4464},
            {1468, 442, 5276},
            {1808, 442, 4808},
            {1499, 442, 4403},
            {1894, 442, 4660},
            {1962, 442, 4654},
            {1734, 442, 4465},
            {2169, 442, 5526},
            {1756, 442, 5339},
            {1657, 442, 5438},
            {1369, 442, 4886},
            {2032, 442, 4402},
            {1489, 442, 4884},
            {1760, 442, 5272},
            {2386, 442, 4705},
            {2335, 442, 4395},
            {1845, 442, 4762},
            {1900, 442, 4762},
            {1963, 442, 4760},
            {2439, 442, 5003},
            {2021, 442, 4759},
            {2490, 442, 5301},
            {1842, 442, 4808},
            {2330, 442, 5517},
            {2792, 442, 5293},
            {1852, 442, 5270},
            {1370, 442, 5272},
            {2838, 442, 4998},
            {2021, 442, 5214}
        };
        PolygonArea area = new PolygonArea();
        for (int i = 0; i < points.length; i++) {
            int[] p = points[i];
            long start = System.nanoTime();
            area.addPoint(new org.joml.Vector3i(p[0], p[1], p[2]));
            long end = System.nanoTime();
            System.out.println(
                    "After " + (i + 1) + " points: " + (end - start) / 1_000_000.0 + " ms");
        }
    }
}
