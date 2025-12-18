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
                    case DENY ->
                        orbisRegion.setFlag((RegistryRegionFlag<Boolean>) orbisFlag, false);
                    case ALLOW ->
                        orbisRegion.setFlag((RegistryRegionFlag<Boolean>) orbisFlag, true);
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
