package org.empirewar.orbis.area;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.empirewar.orbis.util.ExtraCodecs;
import org.joml.Vector3i;

import java.util.*;

/**
 * Polyhedral areas, with complex shapes
 */
public final class PolyhedralArea extends EncompassingArea {

    public static Codec<PolyhedralArea> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.VEC_3I.listOf().fieldOf("points").forGetter(c -> c.points().stream()
                            .toList()))
            .apply(instance, PolyhedralArea::new));

    public PolyhedralArea() {
        super();
    }

    private PolyhedralArea(List<Vector3i> points) {
        super(points);
    }

    /**
     * Vertices that are contained in the convex hull.
     */
    private final Set<Vector3i> vertices = new LinkedHashSet<>();

    /**
     * Vertices that are coplanar to the first 3 vertices
     */
    private final Set<Vector3i> vertexBacklog = new LinkedHashSet<>();

    @Override
    public Optional<Integer> getExpectedPoints() {
        return Optional.empty();
    }

    /**
     * Obtain all the vertices for a polygonal region
     *
     * @return vertices of
     */
    public Collection<Vector3i> getVertices() {
        if (vertexBacklog.isEmpty()) return vertices;

        final List<Vector3i> allVertices = new ArrayList<>(vertices);
        allVertices.addAll(vertexBacklog);

        return allVertices;
    }

    @Override
    public AreaType<?> getType() {
        return AreaType.POLYHEDRON;
    }
}
