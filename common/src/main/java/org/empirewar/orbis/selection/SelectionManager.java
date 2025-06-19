/*
 * This file is part of Orbis, licensed under the GNU GPL v3 License.
 *
 * Copyright (C) 2024 Empire War
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
package org.empirewar.orbis.selection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SelectionManager {

    private final Map<UUID, Selection> selectors;

    public SelectionManager() {
        this.selectors = new HashMap<>();
    }

    public Optional<Selection> get(UUID player) {
        return Optional.ofNullable(selectors.get(player));
    }

    public void add(UUID player, Selection selection) {
        this.selectors.put(player, selection);
    }

    public void remove(UUID player) {
        this.selectors.remove(player);
    }
}
