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
package org.empirewar.orbis.migrations.worldguard;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.empirewar.orbis.flag.RegionFlag;
import org.empirewar.orbis.region.Region;

@FunctionalInterface
public interface FlagTransformer {

    FlagTransformer DEFAULT = (audience, region, flag, orbisRegion, orbisFlag) -> {
        orbisRegion.addFlag(orbisFlag);
        if (flag instanceof StateFlag stateFlag) {
            final StateFlag.State state = region.getFlag(stateFlag);
            switch (state) {
                case DENY -> orbisRegion.setFlag((RegionFlag<Boolean>) orbisFlag, false);
                case ALLOW -> orbisRegion.setFlag((RegionFlag<Boolean>) orbisFlag, true);
            }
            audience.sendMessage(Component.text(
                    "Processed state flag '" + flag.getName() + "' with state '" + state + "'...",
                    NamedTextColor.LIGHT_PURPLE));
        }
    };

    void transform(
            Audience audience,
            ProtectedRegion region,
            Flag<?> flag,
            Region orbisRegion,
            RegionFlag<?> orbisFlag);
}
