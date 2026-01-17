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

import static org.empirewar.orbis.hytale.ui.RegionFlagsPage.RegionFlagsData.KEY_FLAG;
import static org.empirewar.orbis.hytale.ui.RegionFlagsPage.RegionFlagsData.KEY_REGION;

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

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.flag.GroupedMutableRegionFlag;
import org.empirewar.orbis.flag.MutableRegionFlag;
import org.empirewar.orbis.flag.RegistryRegionFlag;
import org.empirewar.orbis.member.FlagMemberGroup;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interactive UI page used to list, add, modify, and remove flags for a region.
 */
public class RegionFlagsPage extends InteractiveCustomUIPage<RegionFlagsPage.RegionFlagsData> {

    private final String regionName;

    public RegionFlagsPage(PlayerRef playerRef, CustomPageLifetime lifetime, Region region) {
        super(playerRef, lifetime, RegionFlagsData.CODEC);
        this.regionName = region.name();
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder uiCommandBuilder,
            UIEventBuilder uiEventBuilder,
            Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/Orbis_RegionFlags.ui");
        uiCommandBuilder.set("#RegionNameLabel.Text", regionName);
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, "#BackButton", EventData.of("Button", "Back"));
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddFlagButton",
                EventData.of("Button", "AddFlag")
                        .append(KEY_REGION, regionName)
                        .append("@" + KEY_FLAG, "#FlagSelector.Value"));

        Region region = OrbisRegistries.REGIONS.get(regionName).orElse(null);
        if (region == null) {
            showMissingRegion(uiCommandBuilder);
            return;
        }

        populateFlagSelector(uiCommandBuilder, region);
        renderFlags(uiCommandBuilder, uiEventBuilder, region);
    }

    private void showMissingRegion(UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.clear("#FlagList");
        uiCommandBuilder.appendInline(
                "#FlagList",
                "Label { Text: \"Region no longer exists.\"; Style: (FontSize: 16, TextColor: #C96161); }");
    }

    private void renderFlags(
            UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder, Region region) {
        uiCommandBuilder.clear("#FlagList");

        var flagEntries = collectFlags(region);

        if (flagEntries.isEmpty()) {
            uiCommandBuilder.appendInline(
                    "#FlagList",
                    "Label { Text: \"No flags set.\"; Style: (FontSize: 15, TextColor: #8A90B2); }");
            return;
        }

        int index = 0;
        for (FlagEntry entry : flagEntries) {
            RegistryRegionFlag<?> registryFlag = entry.registryFlag();
            MutableRegionFlag<?> flag = entry.flag();
            String value = String.valueOf(flag.getValue());
            Optional<String> description = registryFlag.description();
            Set<String> groups = entry.groups();
            boolean isBooleanFlag = isBooleanFlag(registryFlag, flag);
            String actionLabel = isBooleanFlag ? "Toggle" : "Modify";
            String actionEvent = isBooleanFlag ? "ToggleFlag" : "ModifyFlag";

            StringBuilder card =
                    new StringBuilder("""
                Group {
                  LayoutMode: Top;
                  Background: #1A1C29;
                  Padding: (Top: 10, Bottom: 10, Left: 12, Right: 12);
                  Anchor: (Bottom: 8);

                  Label {
                    Text: "%s";
                    Style: (FontSize: 16, RenderBold: true, TextColor: #FFFFFF);
                  }
                """.formatted(escape(registryFlag.key().asString())));

            description.ifPresent(desc -> card.append("""
                  Label {
                    Text: "%s";
                    Style: (FontSize: 14, TextColor: #8A90B2);
                    Anchor: (Bottom: 4);
                  }
                """.formatted(escape(desc))));

            card.append("""
              Label {
                Text: "Value: %s";
                Style: (FontSize: 15, TextColor: #FFFFFF);
                Anchor: (Bottom: 4);
              }
            """.formatted(escape(value)));

            if (!groups.isEmpty()) {
                card.append("""
                  Label {
                    Text: "Groups: %s";
                    Style: (FontSize: 14, TextColor: #8A90B2);
                    Anchor: (Bottom: 6);
                  }
                """.formatted(escape(String.join(", ", groups))));
            }

            card.append("""
              Group {
                LayoutMode: Left;
                Anchor: (Height: 32);

                Button #ModifyFlag {
                  Style: ButtonStyle();
                  Background: #2D5D91;
                  Anchor: (Width: 120, Height: 30);

                  Label {
                    Text: "%s";
                    Style: (FontSize: 14, RenderBold: true, TextColor: #FFFFFF);
                  }
                }

                Button #RemoveFlag {
                  Style: ButtonStyle();
                  Background: #AA3E3E;
                  Anchor: (Width: 120, Height: 30);

                  Label {
                    Text: "Remove";
                    Style: (FontSize: 14, RenderBold: true, TextColor: #FFFFFF);
                  }
                }
              }
            }
            """.formatted(escape(actionLabel)));

            uiCommandBuilder.appendInline("#FlagList", card.toString());

            String baseSelector = "#FlagList[" + index + "]";
            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    baseSelector + " #ModifyFlag",
                    EventData.of("Button", actionEvent)
                            .append("Region", regionName)
                            .append("Flag", registryFlag.key().asString()));
            if (!isBooleanFlag) {
                // TODO: Provide editor to modify non-boolean flags.
            }
            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    baseSelector + " #RemoveFlag",
                    EventData.of("Button", "RemoveFlag")
                            .append("Region", regionName)
                            .append("Flag", registryFlag.key().asString()));
            index++;
        }
    }

    private Set<String> extractGroups(MutableRegionFlag<?> flag) {
        if (flag instanceof GroupedMutableRegionFlag<?> grouped) {
            return grouped.groups().stream().map(FlagMemberGroup::name).collect(Collectors.toSet());
        }
        return Set.of();
    }

    private java.util.List<FlagEntry> collectFlags(Region region) {
        java.util.List<FlagEntry> entries = new ArrayList<>();
        for (RegistryRegionFlag<?> registryFlag : OrbisRegistries.FLAGS) {
            Optional<? extends MutableRegionFlag<?>> optionalFlag = region.getFlag(registryFlag);
            if (optionalFlag.isEmpty()) {
                continue;
            }
            MutableRegionFlag<?> flag = optionalFlag.orElseThrow();
            Set<String> groups = extractGroups(flag);
            entries.add(new FlagEntry(registryFlag, flag, groups));
        }
        return entries;
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, RegionFlagsData data) {
        super.handleDataEvent(ref, store, data);

        if (data.button == null) {
            return;
        }

        if (Objects.equals(data.button, "Back")) {
            this.close();
            return;
        }

        if (Objects.equals(data.button, "AddFlag")) {
            handleAddFlag(data);
            return;
        }

        if (Objects.equals(data.button, "ToggleFlag")) {
            handleToggleFlag(data);
            return;
        }

        if (Objects.equals(data.button, "RemoveFlag")) {
            handleRemoveFlag(data);
            return;
        }

        // We leave other actions to the command integration layer in a later step.
        this.sendUpdate();
    }

    private void handleAddFlag(RegionFlagsData data) {
        String flagId = data.flag;
        if (flagId == null || flagId.isBlank()) {
            OrbisAPI.get().logger().warn("Flag ID {} is null or blank", flagId);
            return;
        }

        Key flagKey;
        try {
            flagKey = Key.key(flagId);
        } catch (InvalidKeyException e) {
            OrbisAPI.get().logger().warn("Flag ID {} is invalid", flagId);
            this.close();
            return;
        }

        RegistryRegionFlag<?> registryFlag = OrbisRegistries.FLAGS.get(flagKey).orElse(null);
        if (registryFlag == null) {
            OrbisAPI.get().logger().warn("Flag ID {} is not registered", flagKey);
            return;
        }

        String targetRegion = data.region != null ? data.region : regionName;
        Region region = OrbisRegistries.REGIONS.get(targetRegion).orElse(null);
        if (region == null) {
            OrbisAPI.get().logger().warn("Region {} does not exist", targetRegion);
            return;
        }

        if (region.getFlag(registryFlag).isPresent()) {
            OrbisAPI.get().logger().warn("Region {} already has flag {}", targetRegion, flagKey);
            return;
        }

        region.addFlag(registryFlag);
        this.rebuild();
    }

    private void handleToggleFlag(RegionFlagsData data) {
        String flagId = data.flag;
        if (flagId == null || flagId.isBlank()) {
            OrbisAPI.get().logger().warn("Flag ID {} is null or blank", flagId);
            return;
        }

        Key flagKey;
        try {
            flagKey = Key.key(flagId);
        } catch (IllegalArgumentException exception) {
            OrbisAPI.get().logger().warn("Flag ID {} is invalid", flagId);
            return;
        }

        RegistryRegionFlag<?> registryFlag = OrbisRegistries.FLAGS.get(flagKey).orElse(null);
        if (registryFlag == null) {
            OrbisAPI.get().logger().warn("Flag ID {} is not registered", flagId);
            return;
        }

        Region region = resolveRegion(data.region);
        if (region == null) {
            return;
        }

        Optional<? extends MutableRegionFlag<?>> optionalMutable = region.getFlag(registryFlag);
        if (optionalMutable.isEmpty()) {
            OrbisAPI.get().logger().warn("Region {} does not have flag {}", region.name(), flagId);
            return;
        }

        MutableRegionFlag<?> mutable = optionalMutable.orElseThrow();
        Object currentValue = mutable.getValue();
        if (!(currentValue instanceof Boolean boolValue)) {
            OrbisAPI.get()
                    .logger()
                    .warn(
                            "Flag {} is not boolean; skipping toggle",
                            registryFlag.key().asString());
            return;
        }

        @SuppressWarnings("unchecked")
        MutableRegionFlag<Boolean> booleanFlag = (MutableRegionFlag<Boolean>) mutable;
        booleanFlag.setValue(!boolValue);
        this.rebuild();
    }

    private void handleRemoveFlag(RegionFlagsData data) {
        String flagId = data.flag;
        if (flagId == null || flagId.isBlank()) {
            return;
        }

        Key flagKey;
        try {
            flagKey = Key.key(flagId);
        } catch (IllegalArgumentException exception) {
            return;
        }

        RegistryRegionFlag<?> registryFlag = OrbisRegistries.FLAGS.get(flagKey).orElse(null);
        if (registryFlag == null) {
            return;
        }

        Region region = resolveRegion(data.region);
        if (region == null) {
            return;
        }

        if (region.getFlag(registryFlag).isEmpty()) {
            OrbisAPI.get()
                    .logger()
                    .warn(
                            "Region {} does not have flag {}",
                            region.name(),
                            registryFlag.key().asString());
            return;
        }

        region.removeFlag(registryFlag);
        this.rebuild();
    }

    private void populateFlagSelector(UICommandBuilder uiCommandBuilder, Region region) {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        String initial = null;
        for (RegistryRegionFlag<?> flag : OrbisRegistries.FLAGS) {
            if (region.getFlag(flag).isPresent()) {
                continue;
            }

            String key = flag.key().asString();
            if (initial == null) {
                initial = key;
            }

            entries.add(new DropdownEntryInfo(LocalizableString.fromString(key), key));
        }

        if (entries.isEmpty()) {
            uiCommandBuilder.set("#FlagSelector.Visible", false);
            uiCommandBuilder.set("#AddFlagButton.Visible", false);
            return;
        }

        uiCommandBuilder.set("#FlagSelector.Entries", entries);
        uiCommandBuilder.set("#FlagSelector.Value", initial);
        uiCommandBuilder.set("#AddFlagButton.Visible", true);
    }

    private Region resolveRegion(@Nullable String requestedRegion) {
        String name = requestedRegion != null ? requestedRegion : regionName;
        Region region = OrbisRegistries.REGIONS.get(name).orElse(null);
        if (region == null) {
            OrbisAPI.get().logger().warn("Region {} does not exist", name);
        }
        return region;
    }

    private boolean isBooleanFlag(RegistryRegionFlag<?> registryFlag, MutableRegionFlag<?> flag) {
        Class<?> defaultType = registryFlag.defaultValueType();
        return flag.getValue() instanceof Boolean
                || Boolean.class.equals(defaultType)
                || boolean.class.equals(defaultType);
    }

    public static class RegionFlagsData {

        static final String KEY_BUTTON = "Button";
        static final String KEY_REGION = "Region";
        static final String KEY_FLAG = "Flag";

        public static final BuilderCodec<RegionFlagsData> CODEC = BuilderCodec.builder(
                        RegionFlagsData.class, RegionFlagsData::new)
                .addField(
                        new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (data, value) -> data.button = value,
                        data -> data.button)
                .addField(
                        new KeyedCodec<>(KEY_REGION, Codec.STRING),
                        (data, value) -> data.region = value,
                        data -> data.region)
                .addField(
                        new KeyedCodec<>(KEY_FLAG, Codec.STRING),
                        (data, value) -> data.flag = value,
                        data -> data.flag)
                .addField(
                        new KeyedCodec<>("@" + KEY_FLAG, Codec.STRING),
                        (data, value) -> data.flag = value,
                        data -> data.flag)
                .build();

        private @Nullable String button;
        private @Nullable String region;
        private @Nullable String flag;
    }

    private record FlagEntry(
            RegistryRegionFlag<?> registryFlag, MutableRegionFlag<?> flag, Set<String> groups) {}
}
