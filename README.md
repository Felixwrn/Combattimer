# Combattimer

Tracks combat between players, displays the remaining time in the action bar,
and blocks configured commands during combat.

## Requirements

- Spigot 26.3 or a compatible Paper 26.3 server
- Java 25
- Maven 3.9+ to build

## Build and install

Run `mvn -B -ntp clean verify` with JDK 25. Copy
`target/Combattimer-1.0.jar` into the server's `plugins/` directory and restart.
The default configuration is written to `plugins/Combattimer/config.yml`.
Set `combat-time`, `blocked-commands`, messages and action-bar settings there.
Plugin resources are packaged from the standard `src/main/resources` directory.
