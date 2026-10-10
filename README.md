# XRAZ AUCTION

Client-side Minecraft Fabric utility for manually configured auction countdowns and a streamer HUD.

## Included
- Right Shift opens the dashboard.
- O toggles the overlay.
- P pauses/resumes the selected auction.
- Search the Minecraft item registry by display name or registry ID.
- Configure a manual price and duration.
- Save auction settings in `.minecraft/config/xrazauction.json`.
- Start, pause, resume, reset, end, and delete the selected record.

## Build
Requires JDK 21 and a compatible Gradle installation.

```bash
gradle --version
gradle clean build
```

The JAR should be generated under `build/libs/`.

## Important
Prices are user-entered and are not live Donut SMP market prices. The mod does not issue server commands, automate trades, or alter server state.

This project has been scaffolded from the provided specification. Build against the exact Minecraft/Fabric dependency versions before installing; API compatibility must be confirmed by a successful Gradle build.
