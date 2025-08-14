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

/**
 * A spherical area with a single center point and a radius.
 */
public final class SphericalArea extends EncompassingArea {

    public static final MapCodec<SphericalArea> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            ExtraCodecs.VEC_3I
                                    .fieldOf("center")
                                    .forGetter(SphericalArea::getCenter),
                            Codec.DOUBLE.fieldOf("radius").forGetter(SphericalArea::getRadius))
                    .apply(instance, SphericalArea::new));

    private double radius;

    public SphericalArea() {
        this.radius = 5;
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

    public void setRadius(double radius) {
        this.radius = radius;
        calculateEncompassingArea();
    }

    @Override
    public Set<Vector3ic> generateBoundaryPoints() {
        Vector3ic c = getCenter();
        int samples = (int) (20 * Math.PI * radius);
        Set<Vector3ic> points = new HashSet<>(samples);
        for (int i = 0; i < samples; i++) {
            double phi = Math.acos(2.0 * i / samples - 1.0);
            double theta = Math.PI * (1 + Math.sqrt(5)) * i;
            double x = c.x() + radius * Math.sin(phi) * Math.cos(theta);
            double y = c.y() + radius * Math.sin(phi) * Math.sin(theta);
            double z = c.z() + radius * Math.cos(phi);
            points.add(new Vector3i((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)));
        }
        return points;
    }
}
