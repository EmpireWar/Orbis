package org.empirewar.orbis.area;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3i;

import java.util.List;
import java.util.Optional;

/**
 * Polygonal areas that span across the entire world height
 */
public final class PolygonalArea extends EncompassingArea {

    public static Codec<PolygonalArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.VEC_3I.listOf().fieldOf("points").forGetter(c -> c.points().stream()
                            .toList()))
            .apply(instance, PolygonalArea::new));

    public PolygonalArea() {
        super();
    }

    private PolygonalArea(List<Vector3i> points) {
        super(points);
    }

    //todo when a polygonal area is created, make it span across the entire world height
    @Override
    public Optional<Integer> getExpectedPoints() {
        return Optional.empty();
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYGON;
    }
}
