<div align="center">

# 🌍 Orbis
<p>Modern region protection plugin for Minecraft: Java Edition.</p>
<hr>

</div>

[![Discord](https://img.shields.io/discord/282242806695591938?color=7289da&label=Discord&logo=discord&logoColor=white)](https://discord.gg/fh62mxU)

Orbis is a modern region protection plugin for Minecraft, supporting the latest version.

## 🚀 What makes Orbis different?
These are the key features that makes Orbis stand out against other region protection plugins:
- Modern, user-friendly interface/commands.
- Support for Cuboids, Polygons, Polyhedrons, and Spherical region area types.
- Multi-platform support for Paper, Spigot, Fabric, NeoForge and Sponge.
- Advanced API that lets you perform a set of queries on a world or region.
- No external dependencies.

## 📆 Supported versions
Orbis aims to always support the latest version of Minecraft. It currently targets **1.21.4**. 
Newer or older versions may work, but are not tested.

## 📦 Downloads

[![Latest Release](https://img.shields.io/maven-central/v/org.empirewar.orbis/orbis-api?label=Latest%20Release)](https://repo.empirewar.org/releases)

- **[Releases](https://repo.empirewar.org/releases)**
- **[Snapshots](https://repo.empirewar.org/snapshots)**

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
// Register your flag (e.g. in plugin init)
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

---

## 🎉 Events
Orbis fires events for every platform:
- **RegionEnterEvent**
- **RegionLeaveEvent**

Listen to these to react to players entering or leaving regions!

## 📖 Javadocs
Full documentation is available at:
[Orbis Javadocs](https://repo.empirewar.org/javadoc/snapshots/org/empirewar/orbis/common/1.0.0-SNAPSHOT)

---

## 🤝 Contributing
We welcome PRs and ideas! To get started:
- Fork the repo and create a feature branch
- Open a pull request with clear description
- Join our [Discord](https://discord.gg/fh62mxU) for dev chat

## 📄 License
Orbis is released under the MIT License. See [LICENSE](LICENSE) for details.
