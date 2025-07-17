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
package org.empirewar.orbis.query;

import com.google.common.base.Preconditions;

import org.empirewar.orbis.flag.RegistryRegionFlag;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

non-sealed class RegionQueryFlagBuilder<FR> implements RegionQuery.Flag.Builder<FR> {

    private RegistryRegionFlag<FR> flag;
    private UUID player;

    RegionQueryFlagBuilder() {}

    @Override
    public RegionQuery.Flag<FR> build() {
        Preconditions.checkState(this.flag != null, "Flag cannot be empty");
        return new RegionQuery.Flag<>() {
            @Override
            public RegistryRegionFlag<FR> flag() {
                return flag;
            }

            @Override
            public Optional<UUID> player() {
                return Optional.ofNullable(player);
            }
        };
    }

    @Override
    public RegionQuery.Flag.Builder<FR> flag(RegistryRegionFlag<FR> flag) {
        Objects.requireNonNull(flag, "Flag cannot be null");
        this.flag = flag;
        return this;
    }

    @Override
    public RegionQuery.Flag.Builder<FR> player(UUID player) {
        Objects.requireNonNull(player, "Player cannot be null");
        this.player = player;
        return this;
    }
}
