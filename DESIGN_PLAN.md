# Design Plan
This document sets out the planned development for Orbis.

## Areas
Areas must be cross-platform, and so by design, *non-locational*, meaning they contain no world-related data.

## Regions
A region is an extension of an area that is identified by a String and contains a set of flags.

## Platform world implementations
A "World" on a platform will track the set of regions it contains, and in turn, the players within that world and the set of regions they are within.

Each world will, similarly to WorldGuard, have a "Global" region which applies flags to the entire world.

Using this design, an area may span multiple worlds whilst needing no additional configuration.

## Flags
Have a FlagRegistry that contains all registered flags. Flags should be Keyed via Adventure to identify the implementing plugin.

A flag is testable against a specific Vector3d position, returning *true* if the flag is applicable at that position, 
which is the case if that position is within a region that contains that flag.

## Events
Use a custom event bus to allow for cross-platform events. LuckPerms has such a system, there is also https://github.com/seiama/event.

## Serialization/Deserialization
Use Mojang Codecs for this, like Battlegrounds.

## Testing
Common module should be mostly unit tested. Plugin modules can be tested where applicable.

## Commands
Commands must be cross-platform. We need to specify a custom sender type.

It's likely we will be able to use our player object representation in the common module.

All commands must have tab completion. We shall use *Cloud* for commands.

## WorldGuard migration
It should be possible to migrate the majority of data from WorldGuard to Orbis.

However, do not let this constrain our design. Some data loss is acceptable. 
We do not need to implement all WorldGuard flags, nor should we.

## WorldEdit support
Support WorldEdit's wand, but *do not depend on WorldEdit*.

## Supported platforms
Sponge and Paper. Spigot support can be looked into in the future.