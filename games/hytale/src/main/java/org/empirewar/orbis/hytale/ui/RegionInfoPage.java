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

import com.google.common.collect.Iterables;
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

import org.empirewar.orbis.OrbisAPI;
import org.empirewar.orbis.area.Area;
import org.empirewar.orbis.member.Member;
import org.empirewar.orbis.member.PermissionMember;
import org.empirewar.orbis.member.PlayerMember;
import org.empirewar.orbis.region.Region;
import org.empirewar.orbis.registry.OrbisRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RegionInfoPage extends InteractiveCustomUIPage<RegionInfoPage.RegionInfoData> {

    private final String regionName;

    public RegionInfoPage(PlayerRef player, CustomPageLifetime lifetime, Region region) {
        super(player, lifetime, RegionInfoData.CODEC);
        this.regionName = region.name();
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder ev,
            Store<EntityStore> store) {
        ui.append("Pages/Orbis_RegionInfo.ui");

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ManageFlags",
                EventData.of(UIActions.BUTTON, "Flags").append(UIActions.REGION, regionName));

        Region region = resolveRegion();
        if (region == null) {
            renderMissing(ui);
            return;
        }

        ui.set("#RegionNameLabel.Text", region.name());

        renderPriority(ui, ev, region);
        renderParents(ui, ev, region);
        renderMembers(ui, ev, region);
        renderArea(ui, region);
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, RegionInfoData data) {
        if (data.button == null) return;

        switch (data.button) {
            case "Back" -> close();

            case "Flags" -> openFlags(store, ref);

            case "AddParent" -> addParent(data);
            case "RemoveParent" -> removeParent(data);
            case "AddMember" -> {
                /* TODO */
            }
            case "RemoveMember" -> {
                /* TODO */
            }
            case "SetPriority" -> {
                /* TODO */
            }
        }
    }

    private void openFlags(Store<EntityStore> store, Ref<EntityStore> ref) {
        Player player = store.getComponent(ref, Player.getComponentType());
        Region region = resolveRegion();
        if (region == null) return;

        player.getPageManager()
                .openCustomPage(
                        ref,
                        store,
                        new RegionFlagsPage(playerRef, CustomPageLifetime.CanDismiss, region));
    }

    private Region resolveRegion() {
        return OrbisRegistries.REGIONS.get(regionName).orElse(null);
    }

    private void renderMissing(UICommandBuilder ui) {
        ui.clear("#PrioritySummary");
        ui.clear("#ParentsList");
        ui.clear("#MembersList");
        ui.clear("#AreaDetails");

        ui.appendInline(
                "#PrioritySummary",
                "Label { Text:\"Region not found\"; Style:(FontSize:18,TextColor:#FF6B6B); }");
    }

    private void renderPriority(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.set("#PriorityValue.Text", String.valueOf(region.priority()));

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AdjustPriority",
                EventData.of(UIActions.BUTTON, "SetPriority").append(UIActions.REGION, regionName));
    }

    private void renderParents(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#ParentCards");

        if (region.isGlobal()) {
            ui.appendInline("#ParentCards", """
                    Label {
                      Text: "Global regions cannot have parents.";
                      Style: (FontSize: 15, TextColor: #8A90B2);
                    }
                    """);
            return;
        }

        ui.append("#ParentCards", "Entries/Orbis_ParentAddRow.ui");

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ParentCards[0] #AddParent",
                EventData.of(UIActions.BUTTON, "AddParent")
                        .append(UIActions.REGION, regionName)
                        .append("@" + UIActions.PARENT, "#ParentCards[0] #ParentSelector.Value"));

        List<WrappedDropdownEntryInfo> parentChoices;
        try {
            parentChoices = buildParentChoices(region);
        } catch (Exception e) {
            OrbisAPI.get().logger().error("Error trying to build parent choices", e);
            return;
        }

        OrbisAPI.get().logger().info("Parent choices: {}", parentChoices);
        if (parentChoices.isEmpty()) {
            ui.set("#ParentCards[0] #ParentSelector.Visible", false);
            ui.set("#ParentCards[0] #AddParent.Visible", false);
            ui.set("#ParentCards[0] #ParentSelector.Value", "");
        } else {
            ui.set("#ParentCards[0] #ParentSelector.Visible", true);
            ui.set("#ParentCards[0] #AddParent.Visible", true);
            ui.set(
                    "#ParentCards[0] #ParentSelector.Entries",
                    parentChoices.stream()
                            .map(WrappedDropdownEntryInfo::toEntry)
                            .toList());
            ui.set(
                    "#ParentCards[0] #ParentSelector.Value",
                    parentChoices.getFirst().value());
        }

        if (region.parents().isEmpty()) {
            ui.appendInline("#ParentCards", """
                    Label {
                      Text: "No parents assigned.";
                      Style: (FontSize: 15, TextColor: #8A90B2);
                    }
                    """);
            return;
        }

        int index = 1;
        for (Region parent : region.parents()) {
            ui.append("#ParentCards", "Entries/Orbis_ParentEntry.ui");
            String entryBase = "#ParentCards[" + index + "]";
            ui.set(entryBase + " #ParentName.Text", parent.name());

            ev.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    entryBase + " #RemoveParent",
                    EventData.of(UIActions.BUTTON, "RemoveParent")
                            .append(UIActions.REGION, regionName)
                            .append(UIActions.PARENT, parent.name()));

            index++;
        }
    }

    private void renderMembers(UICommandBuilder ui, UIEventBuilder ev, Region region) {
        ui.clear("#MemberCards");

        ui.append("#MemberCards", "Entries/Orbis_MemberAddRow.ui");

        ev.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#MemberCards[0] #AddMember",
                EventData.of(UIActions.BUTTON, "AddMember").append(UIActions.REGION, regionName));

        if (region.members().isEmpty()) {
            ui.appendInline("#MemberCards", """
                    Label {
                      Text: "No members.";
                      Style: (FontSize: 15, TextColor: #8A90B2);
                    }
                    """);
            return;
        }

        int index = 1;
        for (Member member : region.members()) {
            String label = memberDisplay(member);

            ui.append("#MemberCards", "Entries/Orbis_MemberEntry.ui");
            String entryBase = "#MemberCards[" + index + "]";
            ui.set(entryBase + " #MemberName.Text", label);

            ev.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    entryBase + " #RemoveMember",
                    EventData.of(UIActions.BUTTON, "RemoveMember")
                            .append(UIActions.REGION, regionName)
                            .append("Member", label));

            index++;
        }
    }

    private List<WrappedDropdownEntryInfo> buildParentChoices(Region region) {
        List<WrappedDropdownEntryInfo> entries = new ArrayList<>();

        for (Region candidate : OrbisRegistries.REGIONS.getAll()) {
            if (candidate.equals(region)) continue;
            if (region.parents().contains(candidate)) continue;
            if (!candidate.isGlobal() && candidate.parents().contains(region)) continue;
            boolean loopViaExisting = region.parents().stream()
                    .anyMatch(existing ->
                            !existing.isGlobal() && existing.parents().contains(candidate));
            if (loopViaExisting) continue;

            entries.add(new WrappedDropdownEntryInfo(candidate.name(), candidate.name()));
        }

        entries.sort(Comparator.comparing(entry -> entry.label().toLowerCase()));
        return entries;
    }

    private record WrappedDropdownEntryInfo(String label, String value) {

        public DropdownEntryInfo toEntry() {
            return new DropdownEntryInfo(LocalizableString.fromString(label), value);
        }
    }

    private void addParent(RegionInfoData data) {
        if (data.parent == null || data.parent.isBlank()) return;

        Region region = resolveRegion();
        if (region == null) return;

        Region parent = OrbisRegistries.REGIONS.get(data.parent).orElse(null);
        if (parent == null) return;

        try {
            region.addParent(parent);
        } catch (IllegalArgumentException ignored) {
            // Selection became invalid between render and submission; ignore.
        }

        rebuild();
    }

    private void removeParent(RegionInfoData data) {
        if (data.parent == null || data.parent.isBlank()) return;

        Region region = resolveRegion();
        if (region == null) return;

        Region parent = OrbisRegistries.REGIONS.get(data.parent).orElse(null);
        if (parent == null) return;

        region.removeParent(parent);
        rebuild();
    }

    private String memberDisplay(Member member) {
        if (member instanceof PlayerMember p) {
            return p.playerId().toString();
        }
        if (member instanceof PermissionMember p) {
            return "perm:" + p.permission();
        }
        return member.toString();
    }

    private void renderArea(UICommandBuilder ui, Region region) {
        ui.clear("#AreaDetails");

        if (region.isGlobal()) {
            ui.appendInline("#AreaDetails", """
                    Label {
                      Text: "Global region (entire world).";
                      Style: (FontSize: 16, TextColor: #8A90B2);
                    }
                    """);
            return;
        }

        Area area = region.area();
        Vector3ic min = area.getMin();
        Vector3ic max = area.getMax();

        ui.appendInline("#AreaDetails", """
                Label { Text: "Bounds:"; Style:(FontSize:16,RenderBold:true); }
                Label { Text: "(%d, %d, %d) - (%d, %d, %d)"; Style:(FontSize:15); }
                Label { Text: "Volume: %,d blocks"; Style:(FontSize:15); }
                Label { Text: "Points: %d"; Style:(FontSize:15); }
                """.formatted(
                        min.x(),
                        min.y(),
                        min.z(),
                        max.x(),
                        max.y(),
                        max.z(),
                        Iterables.size(area),
                        area.points().size()));
    }

    public static final class RegionInfoData {

        public static final BuilderCodec<RegionInfoData> CODEC = BuilderCodec.builder(
                        RegionInfoData.class, RegionInfoData::new)
                .addField(
                        new KeyedCodec<>(UIActions.BUTTON, Codec.STRING),
                        (d, v) -> d.button = v,
                        d -> d.button)
                .addField(
                        new KeyedCodec<>(UIActions.PARENT, Codec.STRING),
                        (d, v) -> d.parent = v,
                        d -> d.parent)
                .addField(
                        new KeyedCodec<>("@" + UIActions.PARENT, Codec.STRING),
                        (d, v) -> d.parent = v,
                        d -> d.parent)
                .build();

        @Nullable String button;

        @Nullable String parent;
    }
}
