# Contributing to Dyson Cube Project

## Development Guidelines

### NeoForge Capabilities API

This project uses **NeoForge 21.1.208** for Minecraft 1.21.1. When working with capabilities, please use the correct package imports:

#### ✅ Correct Imports (NeoForge 21+)
```java
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
```

#### ❌ Incorrect Imports (Old/Deprecated)
```java
// DO NOT USE - This package path no longer exists in NeoForge 21+
import net.neoforged.neoforge.common.capabilities.Capability;
```

### Package Structure Changes

In NeoForge 21+, the capabilities system was reorganized:
- **Old path (Forge/Early NeoForge):** `net.neoforged.neoforge.common.capabilities.*`
- **New path (NeoForge 21+):** `net.neoforged.neoforge.capabilities.*`

If you encounter compilation errors about `package net.neoforged.neoforge.common.capabilities does not exist`, you are likely using outdated imports or following deprecated documentation.

### Registering Capabilities

Example of correctly registering capabilities in NeoForge 21+:

```java
EventManager.mod(RegisterCapabilitiesEvent.class).process(event -> {
    event.registerBlock(Capabilities.EnergyStorage.BLOCK, 
        (level, blockPos, blockState, blockEntity, direction) -> {
            // Your capability logic here
            return yourEnergyStorage;
        }, 
        YOUR_BLOCK);
}).subscribe();
```

### Using Capabilities

Example of correctly accessing capabilities:

```java
var capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, direction);
if (capability != null) {
    // Use the capability
}
```

## Building the Project

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Resources

- [NeoForge Documentation](https://docs.neoforged.net/)
- [NeoForge Capabilities Guide](https://docs.neoforged.net/docs/datastorage/capabilities)
