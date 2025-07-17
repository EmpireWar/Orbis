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
package org.empirewar.orbis.command.caption;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

/**
 * Caption registry that uses bi-functions to produce messages
 *
 * @param <C> Command sender type
 */
public final class OrbisCaptionProvider<C> extends DelegatingCaptionProvider<C> {

    private static CaptionProvider<?> PROVIDER;

    public static void registerTranslations() {
        PROVIDER = CaptionProvider.constantProvider()
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys
                                                .ARGUMENT_PARSE_FAILURE_REGION_FLAG_NOT_FOUND
                                                .key())))
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD_NOT_FOUND
                                                .key())))
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_FLAG_VALUE_INVALID
                                                .key())))
                .putCaption(
                        OrbisCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_VALUE_NOT_FOUND,
                        plainText()
                                .serialize(translatable(
                                        OrbisCaptionKeys
                                                .ARGUMENT_PARSE_FAILURE_REGISTRY_VALUE_NOT_FOUND
                                                .key())))
                .build();
    }

    @Override
    public @NonNull CaptionProvider<C> delegate() {
        return (CaptionProvider<C>) PROVIDER;
    }
}
