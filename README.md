<div align="center">

<img src="images/orbis-logo-black.png" alt="Orbis Logo">
<p>Modern region protection plugin for Minecraft: Java Edition.</p>
<hr>

</div>

[![Discord](https://img.shields.io/discord/282242806695591938?color=7289da&label=Discord&logo=discord&logoColor=white)](https://discord.gg/fh62mxU)

> [!WARNING]
> Orbis is still in early stages of development. There will likely be protection bypasses and other bugs. 
> Please report any bugs or issues to the [GitHub](https://github.com/EmpireWar/Orbis/issues).
> If there is a feature you would like to see, please open an [issue](https://github.com/EmpireWar/Orbis/issues/new/choose).

Orbis is a modern region protection plugin for Minecraft, supporting the latest version and most platforms: Paper, Fabric, NeoForge, and Sponge.

> [!IMPORTANT]
> Orbis is not compatible with Spigot. Please use Paper instead.

## What makes Orbis different?
These are the key features that makes Orbis stand out against other region protection plugins:
- Modern, user-friendly interface/commands.
- Support for Cuboids, Polygons, Polyhedrons, and Spherical region area types.
- Region and selection visualisation using particles for all region types.
- Multi-platform support for Paper, Fabric, NeoForge and Sponge.
- Advanced API that lets you perform a set of queries on a world or region.
- No external dependencies.

### Migrating from WorldGuard?
Orbis has an inbuilt migration tool that allows you to migrate your WorldGuard regions.

Simply run `/orbis migrate` on a server with Orbis and WorldGuard installed, and your WorldGuard regions will be migrated to Orbis (as well as they can be).

#### Key differences
- Regions are not "protected by default". Instead, you need to add what flags you want to a region to make it protected.
- The "`passthrough`" flag: There isn't one, due to the above.
- Flags affect all "members" by default.
- Orbis tries to avoid making flags that don't let you (the user) fine-tune them. 
For example, the `damage-animals` on WorldGuard is instead `damageable_entities` on Orbis. 
It is a list of entity "keys" e.g. `["minecraft:zombie", "minecraft:husk"]`. 
Meaning, you can pick *only the entities you want to allow to take damage*. This will also work with modded entities.
- WorldEdit: Orbis does not use WorldEdit's wand. Instead, it has a custom wand.
- `__global__` region: In Orbis, there is a global region per world and a global region for all worlds.
- Region priorities: By default, a region has a priority of 2. A world's global region has a priority of 1. The global region encompassing all worlds has a priority of 0.
- Plugins that use WorldGuard's API: Orbis does not support WorldGuard's API. Plugin developers will have to add support for Orbis' API.

## 📆 Supported versions
Orbis aims to always support the latest version of Minecraft. It currently targets **1.21.4-1.21.8**. 
Newer or older versions may work, but are not tested.

## 📦 Downloads

[![Latest Release](https://img.shields.io/maven-central/v/org.empirewar.orbis/orbis-api?label=Latest%20Release)](https://repo.empirewar.org/releases)

- **[Releases - GitHub](https://github.com/EmpireWar/Orbis/releases)**
- **[Snapshots - GitHub Actions (nightly.link)](https://nightly.link/EmpireWar/Orbis/workflows/publish/main/Artifacts.zip)**

For snapshot builds, download the `.zip` file, go into the folder of your platform, then find `build/libs/orbis-PLATFORM-VERSION.jar`.

---

## 🚀 Getting Started

1. **⬇️ Download** the plugin for your platform from the [releases](https://repo.empirewar.org/releases) page.
2. **📂 Drop** the `.jar` file into your server's `plugins` or `mods` folder.
3. **🔄 Restart** your server. Orbis will generate its config and be ready to use!

---

## 📚 API & Integration

Replace `PLATFORM` with your server type (e.g., `paper`, `fabric`, `sponge`). For Bukkit-compatible servers, use `paper`.

Replace `VERSION` with the latest Orbis version. For snapshots, use the `/snapshots` repo and append `-SNAPSHOT`.

```kts
repositories {
    maven("https://repo.empirewar.org/releases")
}

dependencies {
    compileOnly("org.empirewar.orbis:PLATFORM-api:VERSION")
}
```

### 🛠 Getting the API instance
```java
OrbisAPI api = OrbisAPI.get();
```

### 🌐 Accessing the RegionisedWorld
```java
RegionisedWorld worldSet = OrbisAPI.get().getRegionisedWorld(Key.key("minecraft", "overworld"));
```

### 🏳️ Registering Custom Flags
You can define and register your own region flags. **Registering a flag requires a [Codec](https://github.com/Mojang/DataFixerUpper) from Mojang DFU for serialisation!**

Here’s how Orbis registers its default flags and how you can register your own:

```java
// Register your flag (in plugin init)
RegistryRegionFlag<Boolean> CAN_FLY = RegistryRegionFlag.<Boolean>builder()
    .key(Key.key("myplugin", "can_fly"))
    .codec(Codec.BOOL) // Mojang DFU Codec
    .defaultValue(() -> false)
    .description("Whether players can fly in this region")
    .build();
OrbisRegistries.FLAGS.register(CAN_FLY.key(), CAN_FLY);

// Usage in a region:
region.addFlag(CAN_FLY);
region.setFlag(CAN_FLY, true); // Allow flying in this region
```

**You must register your flags prior to Orbis being fully enabled! (i.e. before `onEnable()` on Bukkit platforms)**

To query a flag on a region:
```java
Optional<Boolean> canFly = region.query(RegionQuery.Flag.<Boolean>builder()
    .flag(CAN_FLY)
    .build())
    .result();
```

---

### 🔍 Queries
You can perform advanced queries on regions and worlds. Here are real examples:

**Query all regions at a position:**
```java
Set<Region> regions = worldSet.query(RegionQuery.Position.builder()
    .position(new Vector3d(-580, 81, -26))
    .build())
    .result();
```

**Chained query: check if a flag is allowed at a position:**
```java
boolean canBreak = worldSet.query(RegionQuery.Position.builder()
        .position(-580, 81, -26))
    .query(RegionQuery.Flag.builder(DefaultFlags.CAN_BREAK))
    .result()
    .orElse(true);
```

**Priority and parent region queries:**
```java
Region region = new Region("test", new CuboidArea());
GlobalRegion parent = new GlobalRegion("parent");
parent.addFlag(DefaultFlags.CAN_BREAK);
parent.setFlag(DefaultFlags.CAN_BREAK, false);
region.addParent(parent);

Optional<Boolean> canBreak = region.query(RegionQuery.Flag.builder(DefaultFlags.CAN_BREAK)).result();
// canBreak will be false due to parent
```

**Complex world query with priorities:**
```java
// If multiple regions overlap, highest priority wins
final boolean canBreak = worldSet.query(RegionQuery.Position.builder()
        .position(new Vector3d(4, 4, 4))
        .build())
    .query(RegionQuery.Flag.<Boolean>builder()
        .flag(DefaultFlags.CAN_BREAK)
        .build())
    .result()
    .orElse(true);
```

### Other Examples
You may be able to find other examples in the [tests](https://github.com/EmpireWar/Orbis/tree/main/common/src/test/java/org/empirewar/orbis).

---

## 🎉 Events
Orbis fires events for every platform:
- **RegionEnterEvent**
- **RegionLeaveEvent**

Listen to these to react to players entering or leaving regions!

## 📖 Javadocs
Full documentation is available at:
[Orbis Javadocs](https://repo.empirewar.org/javadoc/snapshots/org/empirewar/orbis/common/latest)

---

## 🤝 Contributing
We welcome PRs and ideas! To get started:
- Fork the repo and create a feature branch
- Open a pull request with clear description
- Join our [Discord](https://discord.gg/fh62mxU) for dev chat

## 📄 License
Orbis is released under the MIT License. See [LICENSE](LICENSE) for details.
