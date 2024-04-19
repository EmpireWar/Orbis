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
package org.empirewar.orbis.region;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.serialization.context.CodecContext;
import org.empirewar.orbis.world.RegionisedWorldSet;

import java.util.List;
import java.util.Set;

/**
 * Represents a region that encompasses an entire world.
 */
public final class GlobalRegion extends Region {

    public static final Codec<GlobalRegion> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            Codec.STRING.fieldOf("name").forGetter(Region::name),
                            MutableRegionFlag.TYPE_CODEC
                                    .listOf()
                                    .fieldOf("flags")
                                    .forGetter(r -> r.flags.values().stream().toList()))
                    .apply(instance, GlobalRegion::new));

    public GlobalRegion(RegionisedWorldSet set) {
        super(set.worldName().orElseThrow());
        this.priority(0);
    }

    public GlobalRegion(String name) {
        super(name);
        this.priority(0);
    }

    private GlobalRegion(String name, List<MutableRegionFlag<?>> flags) {
        super(name);
        this.priority(0);
        flags.forEach(mu -> this.flags.put(mu.key(), mu));
        CodecContext.queue().rewardPatience(Region.class, this);
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
