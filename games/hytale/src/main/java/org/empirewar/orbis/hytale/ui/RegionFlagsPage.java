/*
 * This file is part of Orbis, licensed under the MIT License.
 *
 * Copyright (C) 2026 Empire War
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
package org.empirewar.orbis.hytale.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import net.kyori.adventure.key.Key;

import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RegionFlagsPage
        extends InteractiveCustomUIPage<RegionFlagsPage.RegionFlagsData> {

    private final String regionName;

    public RegionFlagsPage(PlayerRef player, CustomPageLifetime lifetime, Region region) {
        super(player, lifetime, RegionFlagsData.CODEC);
        this.regionName = region.name();
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder ev,
            Store<EntityStore> store) {
        ui.append("Pages/Orbis_RegionFlags.ui");

        ui.set("#RegionNameLabel.Text", regionName);

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddFlagButton",
                EventData.of(UIActions.BUTTON, "AddFlag")
                        .append(UIActions.REGION, regionName)
                        .append("@" + UIActions.FLAG, "#FlagSelector.Value"));

        Region region = resolveRegion();
        if (region == null) {
            showMissing(ui);
            return;
        }

        populateDropdown(ui, region);
        renderFlags(ui, ev, region);
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, RegionFlagsData data) {
        if (data.button == null) return;

        switch (data.button) {
            case "AddFlag" -> addFlag(data);
            case "ToggleFlag" -> toggleFlag(data);
            case "RemoveFlag" -> removeFlag(data);
        }

        rebuild();
    }

    @Override
    protected void close() {
        Region region = resolveRegion();
        if (region == null) {
            super.close();
            return;
        }

        Ref<EntityStore> ref = this.playerRef.getReference();
        if (ref != null) {
            Store<EntityStore> store = ref.getStore();
            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            playerComponent
                    .getPageManager()
                    .openCustomPage(
                            ref,
                            store,
                            new RegionInfoPage(playerRef, CustomPageLifetime.CanDismiss, region));
        }
    }

    private void showMissing(UICommandBuilder ui) {
        ui.clear("#FlagCards");

        ui.set("#FlagCountLabel.Text", "Region unavailable");

        ui.appendInline("#FlagCards", """
                Label {
                  Text: "This region no longer exists.";
                  Style: (FontSize: 16, TextColor: #FF6B6B);
                }
                """);
    }

    private void populateDropdown(UICommandBuilder ui, Region region) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        String first = null;

        for (RegistryRegionFlag<?> flag : OrbisRegistries.FLAGS) {
            if (region.getFlag(flag).isPresent()) {
                continue;
            }

            String key = flag.key().asString();
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(key), key));

            if (first == null) {
                first = key;
            }
        }

        if (entries.isEmpty()) {
            ui.set("#FlagSelector.Visible", false);
            ui.set("#AddFlagButton.Visible", false);
            ui.set("#FlagSelector.Value", "");
            return;
        }

        ui.set("#FlagSelector.Visible", true);
        ui.set("#AddFlagButton.Visible", true);
        ui.set("#FlagSelector.Entries", entries);
        ui.set("#FlagSelector.Value", first);
    }

    private void renderFlags(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#FlagCards");

        List<RegistryRegionFlag<?>> flags = OrbisRegistries.FLAGS.getAll().stream()
                .filter(f -> region.getFlag(f).isPresent())
                .toList();

        ui.set(
                "#FlagCountLabel.Text",
                flags.isEmpty()
                        ? "No flags"
                        : flags.size() + " flag" + (flags.size() == 1 ? "" : "s"));

        int index = 0;
        for (RegistryRegionFlag<?> registryFlag : flags) {
            MutableRegionFlag<?> flag = region.getFlag(registryFlag).orElseThrow();

            FlagEntryUI.render(ui, ev, index++, regionName, registryFlag, flag);
        }
    }

    private void addFlag(RegionFlagsData data) {
        Region region = resolveRegion();
        if (region == null || data.flag == null) return;

        RegistryRegionFlag<?> flag =
                OrbisRegistries.FLAGS.get(Key.key(data.flag)).orElse(null);
        if (flag == null) return;

        region.addFlag(flag);
    }

    private void toggleFlag(RegionFlagsData data) {
        Region region = resolveRegion();
        if (region == null || data.flag == null) return;

        RegistryRegionFlag<?> registry =
                OrbisRegistries.FLAGS.get(Key.key(data.flag)).orElse(null);
        if (registry == null) return;

        MutableRegionFlag<?> flag = region.getFlag(registry).orElse(null);
        if (!(flag instanceof MutableRegionFlag<?> m)) return;

        if (m.getValue() instanceof Boolean b) {
            @SuppressWarnings("unchecked")
            MutableRegionFlag<Boolean> bool = (MutableRegionFlag<Boolean>) m;
            bool.setValue(!b);
        }
        // TODO: non-boolean editor
    }

    private void removeFlag(RegionFlagsData data) {
        Region region = resolveRegion();
        if (region == null || data.flag == null) return;

        OrbisRegistries.FLAGS.get(Key.key(data.flag)).ifPresent(region::removeFlag);
    }

    private Region resolveRegion() {
        return OrbisRegistries.REGIONS.get(regionName).orElse(null);
    }

    public static final class RegionFlagsData {

        public static final BuilderCodec<RegionFlagsData> CODEC = BuilderCodec.builder(
                        RegionFlagsData.class, RegionFlagsData::new)
                .addField(
                        new KeyedCodec<>(UIActions.BUTTON, Codec.STRING),
                        (d, v) -> d.button = v,
                        d -> d.button)
                .addField(
                        new KeyedCodec<>(UIActions.FLAG, Codec.STRING),
                        (d, v) -> d.flag = v,
                        d -> d.flag)
                .addField(
                        new KeyedCodec<>("@" + UIActions.FLAG, Codec.STRING),
                        (d, v) -> d.flag = v,
                        d -> d.flag)
                .build();

        @Nullable String button;

        @Nullable String flag;
    }
}
