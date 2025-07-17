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
package org.empirewar.orbis.area;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public sealed class PolygonArea extends EncompassingArea permits PolyhedralArea {

    public static MapCodec<PolygonArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.VEC_3I
                            .listOf()
                            .fieldOf("points")
                            .forGetter(c -> new LinkedList<>(c.points)))
                    .apply(instance, PolygonArea::new));

    public PolygonArea() {
        super();
    }

    protected PolygonArea(List<Vector3ic> points) {
        super(points);
    }

    @Override
    public boolean contains(Vector3dc point) {
        return contains(point.x(), point.y(), point.z());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        // Quick and dirty check.
        if (x < min.x() || x > max.x() || z < min.z() || z > max.z()) {
            return false;
        }

        // https://www.eecs.umich.edu/courses/eecs380/HANDOUTS/PROJ2/InsidePoly.html
        double angle = 0;

        final Vector3dc blockPosition = new Vector3d(x, y, z);
        final LinkedList<Vector3ic> pointsList = new LinkedList<>(points);
        for (int i = 0; i < pointsList.size(); i++) {
            final Vector3ic point = pointsList.get(i);
            final Vector3ic nextPoint = pointsList.get((i + 1) % pointsList.size());
            double xOffset = point.x() - blockPosition.x();
            double zOffset = point.z() - blockPosition.z();
            double nextXOffset = nextPoint.x() - blockPosition.x();
            double nextZOffset = nextPoint.z() - blockPosition.z();
            angle += angle(xOffset, zOffset, nextXOffset, nextZOffset);
        }

        // If the angle is zero, it is on the edge
        return Math.abs(angle) >= Math.PI || angle == 0;
    }

    /**
     * Return the angle between two vectors on a plane.
     * The angle is from vector 1 to vector 2, positive anticlockwise.
     * The result is between -pi and pi.
     */
    private double angle(double x1, double y1, double x2, double y2) {
        double dTheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dTheta = theta2 - theta1;
        while (dTheta > Math.PI) dTheta -= Math.PI * 2;
        while (dTheta < -Math.PI) dTheta += Math.PI * 2;

        return dTheta;
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYGON;
    }

    @Override
    public Optional<Integer> getMaximumPoints() {
        return Optional.empty();
    }

    @Override
    public int getMinimumPoints() {
        return 3;
    }

    @Override
    public Set<Vector3ic> generateBoundaryPoints() {
        Set<Vector3ic> boundary = new HashSet<>();
        List<Vector3ic> pts = new ArrayList<>(points());
        if (pts.size() < 3) return boundary;
        // Project to 2D (XZ), compute convex hull, then reconstruct 3D points
        List<Vector3ic> hull = convexHullXZ(pts);
        for (int i = 0; i < hull.size(); i++) {
            Vector3ic a = hull.get(i);
            Vector3ic b = hull.get((i + 1) % hull.size());
            boundary.addAll(getLinePoints(a, b));
        }
        return boundary;
    }

    /**
     * Computes the convex hull of the given points in XZ plane using Andrew's monotone chain algorithm.
     * Returns the hull points in counter-clockwise order.
     */
    private List<Vector3ic> convexHullXZ(List<Vector3ic> pts) {
        List<Vector3ic> sorted = new ArrayList<>(pts);
        sorted.sort(Comparator.comparingInt(Vector3ic::x).thenComparingInt(Vector3ic::z));
        List<Vector3ic> lower = new ArrayList<>();
        for (Vector3ic p : sorted) {
            while (lower.size() >= 2 && cross(lower.get(lower.size() - 2), lower.getLast(), p) <= 0)
                lower.removeLast();
            lower.add(p);
        }
        List<Vector3ic> upper = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Vector3ic p = sorted.get(i);
            while (upper.size() >= 2 && cross(upper.get(upper.size() - 2), upper.getLast(), p) <= 0)
                upper.removeLast();
            upper.add(p);
        }
        lower.removeLast();
        upper.removeLast();
        lower.addAll(upper);
        return lower;
    }

    // Cross product for 2D (XZ)
    private long cross(Vector3ic o, Vector3ic a, Vector3ic b) {
        return (long) (a.x() - o.x()) * (b.z() - o.z()) - (long) (a.z() - o.z()) * (b.x() - o.x());
    }

    private Set<Vector3i> getLinePoints(Vector3ic start, Vector3ic end) {
        Set<Vector3i> points = new HashSet<>();
        int x1 = start.x(), y1 = start.y(), z1 = start.z();
        int x2 = end.x(), y2 = end.y(), z2 = end.z();
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
        int xs = x2 > x1 ? 1 : -1, ys = y2 > y1 ? 1 : -1, zs = z2 > z1 ? 1 : -1;
        int n = Math.max(Math.max(dx, dy), dz);
        for (int i = 0; i <= n; i++) {
            int x = x1 + i * (x2 - x1) / n;
            int y = y1 + i * (y2 - y1) / n;
            int z = z1 + i * (z2 - z1) / n;
            points.add(new Vector3i(x, y, z));
        }
        return points;
    }
}
