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
package org.empirewar.orbis.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A list codec that never discards data it cannot understand.
 * <p>
 * Each element is decoded with {@code elementCodec}. If that fails <i>for any reason</i> - including
 * a thrown exception - the raw serialised element is captured verbatim as a {@link Dynamic} on the
 * right side of the {@link Either} instead of being dropped. Retained elements are written back out
 * unchanged, so a round-trip is lossless even for elements this build of Orbis has no idea how to
 * interpret.
 * <p>
 * This exists because {@link Codec#listOf()} quietly discards failing elements into a partial
 * result, which meant a region referencing a flag from a plugin that failed to load would lose that
 * flag's data on the next save.
 *
 * @param elementCodec the codec used for elements that <i>can</i> be understood
 * @param errorReporter receives the decode failure message for each retained element
 * @param <A> the decoded element type
 */
public record RetainingListCodec<A>(Codec<A> elementCodec, Consumer<String> errorReporter)
        implements Codec<List<Either<A, Dynamic<?>>>> {

    @Override
    public <T> DataResult<Pair<List<Either<A, Dynamic<?>>>, T>> decode(
            final DynamicOps<T> ops, final T input) {
        return ops.getStream(input).map(stream -> {
            final List<Either<A, Dynamic<?>>> elements = new ArrayList<>();
            stream.forEach(element -> {
                final DataResult<Pair<A, T>> decoded = decodeElement(ops, element);
                final Optional<Pair<A, T>> success = decoded.result();
                if (success.isPresent()) {
                    elements.add(Either.left(success.get().getFirst()));
                    return;
                }

                elements.add(Either.right(new Dynamic<>(ops, element)));
                errorReporter.accept(
                        decoded.error().map(DataResult.Error::message).orElse("no error message"));
            });
            return Pair.of(List.copyOf(elements), ops.empty());
        });
    }

    private <T> DataResult<Pair<A, T>> decodeElement(final DynamicOps<T> ops, final T element) {
        try {
            return elementCodec.decode(ops, element);
        } catch (Exception e) {
            // Codecs are allowed to throw instead of erroring - Adventure's Key.key raises
            // InvalidKeyException on a malformed identifier. Treat that as a failed decode rather
            // than letting it tear down the whole region.
            return DataResult.error(() -> "Threw while decoding element: " + e);
        }
    }

    @Override
    public <T> DataResult<T> encode(
            final List<Either<A, Dynamic<?>>> input, final DynamicOps<T> ops, final T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();
        for (Either<A, Dynamic<?>> element : input) {
            element.ifLeft(known -> builder.add(elementCodec.encodeStart(ops, known)))
                    // convert (not cast) so this still works if a region is ever encoded to ops
                    // other than the JSON ops it was decoded with. Same-ops is an identity.
                    .ifRight(retained -> builder.add(retained.convert(ops).getValue()));
        }
        return builder.build(prefix);
    }

    @Override
    public String toString() {
        return "RetainingListCodec[" + elementCodec + "]";
    }
}
