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
package org.empirewar.orbis.serialization.context.passport;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Passport<P> {

    private final List<Consumer<P>> suppliers = new ArrayList<>();

    public List<Consumer<P>> getSuppliers() {
        return suppliers;
    }

    public void add(Consumer<P> supplier) {
        suppliers.add(supplier);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("suppliers", suppliers).toString();
    }
}
