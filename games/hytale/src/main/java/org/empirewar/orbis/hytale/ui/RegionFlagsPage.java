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
import org.empirewar.orbis.hytale.util.Translations;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Lists and edits the flags set on a region. */
public final class RegionFlagsPage extends ParentedPage<RegionFlagsPage.RegionFlagsData> {

    private final String regionName;

    public RegionFlagsPage(
            PlayerRef player,
            CustomPageLifetime lifetime,
            Region region,
            @Nullable InteractiveCustomUIPage<?> parent) {
        super(player, lifetime, RegionFlagsData.CODEC, parent);
        this.regionName = region.name();
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder ev,
            Store<EntityStore> store) {
        ui.append("Pages/Orbis_RegionFlags.ui");
        super.build(ref, ui, ev, store);

        ui.set("#RegionNameLabel.Text", regionName);

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddFlagButton",
                EventData.of(UIActions.BUTTON, UIActions.ADD_FLAG)
                        .append(UIActions.REGION, regionName)
                        .append(UIActions.live(UIActions.FLAG), "#FlagSelector.Value"));

        final Region region = resolveRegion();
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
        super.handleDataEvent(ref, store, data);
        if (isBack(data) || data.button == null) return;

        switch (data.button) {
            case UIActions.ADD_FLAG -> addFlag(data);
            case UIActions.TOGGLE_FLAG -> toggleFlag(data);
            case UIActions.MODIFY_FLAG ->
                sendMessage(Translations.message("orbis.error.notEditable"));
            case UIActions.REMOVE_FLAG -> removeFlag(data);
            default -> {
                return;
            }
        }

        rebuild();
    }

    private void showMissing(UICommandBuilder ui) {
        ui.clear("#FlagCards");

        ui.set("#FlagCountLabel.Text", translate("orbis.region.flags.unavailable"));

        ui.appendInline(
                "#FlagCards",
                "Label { Text: %ui.orbis.region.flags.missing;"
                        + " Style: (FontSize: 16, TextColor: #FF6B6B); }");
    }

    private void populateDropdown(UICommandBuilder ui, Region region) {
        final List<DropdownEntryInfo> entries = new ArrayList<>();
        String first = null;

        for (RegistryRegionFlag<?> flag : OrbisRegistries.FLAGS) {
            if (region.getFlag(flag).isPresent()) {
                continue;
            }

            final String key = flag.key().asString();
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(key), key));

            if (first == null) {
                first = key;
            }
        }

        final boolean any = !entries.isEmpty();
        ui.set("#FlagSelector.Visible", any);
        ui.set("#AddFlagButton.Visible", any);

        if (!any) {
            ui.set("#FlagSelector.Value", "");
            return;
        }

        ui.set("#FlagSelector.Entries", entries);
        ui.set("#FlagSelector.Value", first);
    }

    private void renderFlags(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#FlagCards");

        final List<RegistryRegionFlag<?>> flags = OrbisRegistries.FLAGS.getAll().stream()
                .filter(f -> region.getFlag(f).isPresent())
                .toList();

        ui.set("#FlagCountLabel.Text", flagCount(flags.size()));

        int index = 0;
        for (RegistryRegionFlag<?> registryFlag : flags) {
            final MutableRegionFlag<?> flag = region.getFlag(registryFlag).orElseThrow();

            FlagEntryUI.render(ui, ev, index++, regionName, registryFlag, flag);
        }
    }

    private String flagCount(int count) {
        if (count == 0) {
            return translate("orbis.region.flags.noFlags");
        }

        final String noun = translate(
                count == 1
                        ? "orbis.region.flags.flagCountSingular"
                        : "orbis.region.flags.flagCount");
        return count + " " + noun;
    }

    private String translate(String key) {
        return Translations.ui(key, playerRef);
    }

    private void addFlag(RegionFlagsData data) {
        final Region region = resolveRegion();
        if (region == null || data.flag == null) return;

        final RegistryRegionFlag<?> flag =
                OrbisRegistries.FLAGS.get(Key.key(data.flag)).orElse(null);
        if (flag == null) {
            sendMessage(Translations.message("orbis.error.flagGone"));
            return;
        }

        region.addFlag(flag);
    }

    private void toggleFlag(RegionFlagsData data) {
        final Region region = resolveRegion();
        if (region == null || data.flag == null) return;

        final RegistryRegionFlag<?> registry =
                OrbisRegistries.FLAGS.get(Key.key(data.flag)).orElse(null);
        if (registry == null) {
            sendMessage(Translations.message("orbis.error.flagGone"));
            return;
        }

        final MutableRegionFlag<?> flag = region.getFlag(registry).orElse(null);
        if (flag == null) return;

        if (flag.getValue() instanceof Boolean value) {
            @SuppressWarnings("unchecked")
            final MutableRegionFlag<Boolean> bool = (MutableRegionFlag<Boolean>) flag;
            bool.setValue(!value);
            return;
        }

        // TODO: non-boolean editor - see #ModifyFlag in Orbis_FlagEntry.ui.
        sendMessage(Translations.message("orbis.error.notEditable"));
    }

    private void removeFlag(RegionFlagsData data) {
        final Region region = resolveRegion();
        if (region == null || data.flag == null) return;

        OrbisRegistries.FLAGS.get(Key.key(data.flag)).ifPresent(region::removeFlag);
    }

    private @Nullable Region resolveRegion() {
        final Region region = OrbisRegistries.REGIONS.get(regionName).orElse(null);
        if (region == null) {
            sendMessage(Translations.message("orbis.error.regionGone"));
        }
        return region;
    }

    /** Event payload for this page. */
    public static final class RegionFlagsData extends ParentedPage.ParentedData {

        public static final BuilderCodec<RegionFlagsData> CODEC = ParentedData.addBackField(
                        BuilderCodec.builder(RegionFlagsData.class, RegionFlagsData::new))
                .addField(
                        new KeyedCodec<>(UIActions.BUTTON, Codec.STRING),
                        (d, v) -> d.button = v,
                        d -> d.button)
                .addField(
                        new KeyedCodec<>(UIActions.FLAG, Codec.STRING),
                        (d, v) -> d.flag = v,
                        d -> d.flag)
                .addField(
                        new KeyedCodec<>(UIActions.live(UIActions.FLAG), Codec.STRING),
                        (d, v) -> d.flag = v,
                        d -> d.flag)
                .build();

        @Nullable String button;

        @Nullable String flag;
    }
}
