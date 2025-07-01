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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class SphericalArea extends EncompassingArea {

    public static final MapCodec<SphericalArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            ExtraCodecs.VEC_3I
                                    .fieldOf("center")
                                    .forGetter(SphericalArea::getCenter),
                            Codec.DOUBLE.fieldOf("radius").forGetter(SphericalArea::getRadius))
                    .apply(instance, SphericalArea::new));

    private final double radius;

    public SphericalArea() {
        this.radius = 0.0;
    }

    SphericalArea(Vector3ic point, double radius) {
        super(Collections.singletonList(point));
        this.radius = radius;
        calculateEncompassingArea();
    }

    @Override
    public boolean contains(Vector3dc point) {
        return contains(point.x(), point.y(), point.z());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        final Vector3ic center = getCenter();
        double dx = x - center.x();
        double dy = y - center.y();
        double dz = z - center.z();
        return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.SPHERE;
    }

    @Override
    public Optional<Integer> getMaximumPoints() {
        return Optional.of(1);
    }

    @Override
    public int getMinimumPoints() {
        return 1;
    }

    @Override
    protected void calculateEncompassingArea() {
        super.calculateEncompassingArea();

        final Vector3ic center = getCenter();
        Vector3ic min = new Vector3i(
                (int) Math.floor(center.x() - radius), (int) Math.floor(center.y() - radius), (int)
                        Math.floor(center.z() - radius));
        this.min.x = min.x();
        this.min.y = min.y();
        this.min.z = min.z();

        Vector3ic max = new Vector3i(
                (int) Math.ceil(center.x() + radius), (int) Math.ceil(center.y() + radius), (int)
                        Math.ceil(center.z() + radius));
        this.max.x = max.x();
        this.max.y = max.y();
        this.max.z = max.z();
    }

    public Vector3ic getCenter() {
        return points.stream().findFirst().orElse(new Vector3i());
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public Set<Vector3ic> getBoundaryPoints() {
        Set<Vector3ic> points = new HashSet<>();
        Vector3ic c = getCenter();
        double r = getRadius();
        int samples = (int) (20 * Math.PI * r);
        for (int i = 0; i < samples; i++) {
            double phi = Math.acos(2.0 * i / samples - 1.0);
            double theta = Math.PI * (1 + Math.sqrt(5)) * i;
            double x = c.x() + r * Math.sin(phi) * Math.cos(theta);
            double y = c.y() + r * Math.sin(phi) * Math.sin(theta);
            double z = c.z() + r * Math.cos(phi);
            points.add(new Vector3i((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)));
        }
        return points;
    }
}
