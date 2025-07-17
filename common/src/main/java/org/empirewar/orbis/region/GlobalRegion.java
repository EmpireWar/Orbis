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
package org.empirewar.orbis.region;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.world.RegionisedWorldSet;

import java.util.List;
import java.util.Set;

/**
 * Represents a region that encompasses an entire world.
 */
public final class GlobalRegion extends Region {

    public static final MapCodec<GlobalRegion> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codec.STRING.fieldOf("name").forGetter(Region::name),
                            MutableRegionFlag.TYPE_CODEC
                                    .listOf()
                                    .fieldOf("flags")
                                    .forGetter(r -> r.flags.values().stream().toList()))
                    .apply(instance, GlobalRegion::new));

    public GlobalRegion(RegionisedWorldSet set) {
        super(set.worldId().orElseThrow().asString(), null);
        this.priority(1);
    }

    public GlobalRegion(String name) {
        super(name, null);
        this.priority(1);
    }

    private GlobalRegion(String name, List<MutableRegionFlag<?>> flags) {
        super(name, null);
        this.priority(1);
        flags.forEach(mu -> this.flags.put(mu.key(), mu));
    }

    @Override
    public Area area() {
        throw new IllegalStateException("Cannot access area for global region");
    }

    @Override
    public Set<Region> parents() {
        throw new IllegalStateException("Cannot access parents for global region");
    }

    @Override
    public void addParent(Region region) {
        throw new IllegalStateException("Cannot access parents for global region");
    }

    @Override
    public void removeParent(Region region) {
        throw new IllegalStateException("Cannot access parents for global region");
    }

    @Override
    public RegionType<?> getType() {
        return RegionType.GLOBAL;
    }
}
