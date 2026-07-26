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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class AtomicFilesTest {

    @TempDir
    Path tempDir;

    @Test
    void writeReplacesExistingContent() throws IOException {
        final Path target = tempDir.resolve("region.json");
        Files.writeString(target, "OLD");

        AtomicFiles.writeString(target, "NEW");

        assertEquals("NEW", Files.readString(target, StandardCharsets.UTF_8));
    }

    @Test
    void writeCreatesMissingDirectories() throws IOException {
        final Path target = tempDir.resolve("nested").resolve("deeper").resolve("region.json");

        AtomicFiles.writeString(target, "hello");

        assertEquals("hello", Files.readString(target, StandardCharsets.UTF_8));
    }

    @Test
    void writeLeavesNoTempFileBehind() throws IOException {
        final Path target = tempDir.resolve("region.json");

        AtomicFiles.writeString(target, "contents");

        try (Stream<Path> files = Files.list(tempDir)) {
            final List<Path> remaining = files.toList();
            assertEquals(1, remaining.size(), "expected only the target file, found " + remaining);
            assertTrue(remaining.get(0).getFileName().toString().endsWith(".json"));
        }
    }

    @Test
    void utf8IsRoundTripped() throws IOException {
        final Path target = tempDir.resolve("region.json");
        final String contents = "{\"name\":\"café – über 世界\"}";

        AtomicFiles.writeString(target, contents);

        assertEquals(contents, Files.readString(target, StandardCharsets.UTF_8));
    }
}
