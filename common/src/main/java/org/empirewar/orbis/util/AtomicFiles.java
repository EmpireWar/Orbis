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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AtomicFiles {

    private AtomicFiles() {}

    /**
     * Writes {@code contents} to {@code target} via a temporary file in the same directory, then
     * moves it into place.
     * <p>
     * If anything fails, {@code target} is left exactly as it was.
     *
     * @param target the file to (over)write
     * @param contents the UTF-8 contents to write
     * @throws IOException if the contents could not be written or moved into place
     */
    public static void writeString(Path target, String contents) throws IOException {
        final Path directory = target.getParent();
        Files.createDirectories(directory);

        // The temporary file lives in the same directory so the move stays on one filesystem, and
        // its ".tmp" suffix keeps it out of the ".json" scan in OrbisPlatform#loadRegions.
        final Path temp = Files.createTempFile(directory, target.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temp, contents, StandardCharsets.UTF_8);
            try {
                Files.move(
                        temp,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException | UnsupportedOperationException e) {
                // Some filesystems cannot do an atomic move - notably network and overlay mounts on
                // Linux, and FAT volumes. A plain replacing move is still far safer than
                // truncating the target in place.
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            // No-op on success; cleans up if the write or the move threw.
            Files.deleteIfExists(temp);
        }
    }
}
