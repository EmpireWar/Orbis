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
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class CuboidArea extends EncompassingArea {

    public static MapCodec<CuboidArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(ExtraCodecs.VEC_3I
                            .listOf()
                            .fieldOf("points")
                            .forGetter(c -> c.points.stream().toList()))
                    .apply(instance, CuboidArea::new));

    public CuboidArea() {
        super();
    }

    private CuboidArea(List<Vector3ic> points) {
        super(points);
    }

    @Override
    public boolean contains(Vector3dc point) {
        return contains(point.x(), point.y(), point.z());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        if (points.size() != getMaximumPoints().orElseThrow()) return false;

        return x >= min.x()
                && x <= max.x()
                && y >= min.y()
                && y <= max.y()
                && z >= min.z()
                && z <= max.z();
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.CUBOID;
    }

    @Override
    public Optional<Integer> getMaximumPoints() {
        return Optional.of(2);
    }

    @Override
    public int getMinimumPoints() {
        // A cuboid area may actually have only one point
        // In which case the area is a point and only spans a single block
        // However, to validate the area is complete, we shall require two points.
        return 2;
    }

    @Override
    public Set<Vector3ic> generateBoundaryPoints() {
        Set<Vector3ic> points = new HashSet<>();
        if (points().size() < 2) return points;
        Vector3ic min = getMin();
        Vector3ic max = getMax();
        // 12 edges of the cuboid
        addLine(points, min.x(), min.y(), min.z(), max.x(), min.y(), min.z()); // bottom front
        addLine(points, max.x(), min.y(), min.z(), max.x(), max.y(), min.z()); // bottom right
        addLine(points, max.x(), max.y(), min.z(), min.x(), max.y(), min.z()); // bottom back
        addLine(points, min.x(), max.y(), min.z(), min.x(), min.y(), min.z()); // bottom left
        addLine(points, min.x(), min.y(), max.z(), max.x(), min.y(), max.z()); // top front
        addLine(points, max.x(), min.y(), max.z(), max.x(), max.y(), max.z()); // top right
        addLine(points, max.x(), max.y(), max.z(), min.x(), max.y(), max.z()); // top back
        addLine(points, min.x(), max.y(), max.z(), min.x(), min.y(), max.z()); // top left
        addLine(points, min.x(), min.y(), min.z(), min.x(), min.y(), max.z()); // front left
        addLine(points, max.x(), min.y(), min.z(), max.x(), min.y(), max.z()); // front right
        addLine(points, min.x(), max.y(), min.z(), min.x(), max.y(), max.z()); // back left
        addLine(points, max.x(), max.y(), min.z(), max.x(), max.y(), max.z()); // back right
        return points;
    }

    private void addLine(Set<Vector3ic> points, int x1, int y1, int z1, int x2, int y2, int z2) {
        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);
        int dz = Integer.compare(z2, z1);
        int length = Math.max(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)), Math.abs(z2 - z1));
        for (int i = 0; i <= length; i++) {
            int x = x1 + dx * i;
            int y = y1 + dy * i;
            int z = z1 + dz * i;
            points.add(new Vector3i(x, y, z));
        }
    }
}
