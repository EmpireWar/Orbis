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
package org.empirewar.orbis.migrations.worldguard;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.region.Region;

import java.util.Set;

@FunctionalInterface
public interface FlagTransformer {

    FlagTransformer DEFAULT = (audience, region, flag, orbisRegion, orbisFlag) -> {
        final boolean group = flag.getName().equals("entry") || flag.getName().equals("exit");
        // From WorldGuard docs:
        // The entry and exit flags default to "non-member", meaning setting them to "deny" will
        // prevent non-members from entering/exiting the region.
        if (group) {
            orbisRegion.addGroupedFlag(orbisFlag, Set.of(FlagMemberGroup.NONMEMBER));
        } else {
            orbisRegion.addFlag(orbisFlag);
        }

        if (flag instanceof StateFlag stateFlag) {
            final StateFlag.State state = region.getFlag(stateFlag);

            // invincible flag is flipped
            if (flag.getName().equals("invincible")) {
                if (state == StateFlag.State.ALLOW) {
                    orbisRegion.setFlag((RegistryRegionFlag<Boolean>) orbisFlag, false);
                } else {
                    orbisRegion.setFlag((RegistryRegionFlag<Boolean>) orbisFlag, true);
                }
            } else {
                switch (state) {
                    case DENY -> orbisRegion.setFlag(
                            (RegistryRegionFlag<Boolean>) orbisFlag, false);
                    case ALLOW -> orbisRegion.setFlag(
                            (RegistryRegionFlag<Boolean>) orbisFlag, true);
                }
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
            RegistryRegionFlag<?> orbisFlag);
}
