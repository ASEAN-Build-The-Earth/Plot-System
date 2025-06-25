# Latest Stable – v4.2.X
### _ASEAN Build The Earth Plot-System_

This project is based on [AlpsBTE/Plot-System v4.X.X](https://github.com/AlpsBTE/Plot-System).  
Release: [v4.2.1](https://github.com/ASEAN-Build-The-Earth/Plot-System/releases/tag/v4.2.1)

> [!IMPORTANT]
> Plot-System is moving to a new major version **5.X.X**
>
> This branch (`legacy/4.2.X`) is the latest stable version,
> but is now in maintenance mode and only receives critical bug fixes.

---

# 🗂️ Features

The following custom features and modifications were introduced in this fork:

## 🔗 Discord Integration w/ [DiscordSRV Plot-System](https://github.com/ASEAN-Build-The-Earth/discordsrv-plotsystem)

Offering optional plugin to integrate the system to discord server.

1. Download the latest release of the plugin [here](https://github.com/ASEAN-Build-The-Earth/discordsrv-plotsystem)
2. [DiscordSRV **v1.29.0**](https://www.spigotmc.org/resources/discordsrv.18494/) is required for the integration to complete
3. Upload the plugin, restart the server and configure the plugin's configuration
4. The slash command `/setup help` can then be used for assists with the integrations

### Notifications
New inactivity notification can be set in the plugin's config file

<table><tbody><tr><td><sub>

```yml
# How many days of inactivity it will take before a claimed plot is automatically abandoned
inactivity-interval: 14

# How long left until the plot gets abandon due to inactivity and the system will start pinging owner on discord
# Be careful, if set >= inactivity-interval will ping the owner every day after plot is created
inactivity-notification-days: 5

# What local-time in a day to ping owner's discord for their inactive plot
inactivity-notification-time: 16
```

<p><code>config.yml</code> <b>line: 30</b></p>
</sub>
</td></tr></tbody></table>

## 🌎 Coordinates Options for Bedrock Support

New configurable coordinate features to customize the terra server with plot-system

### 📌 **plot-shifting** 

> [!TIP]
> This feature aims to solve plot coordinates issue being too large for Bedrock client to handle.
>
> Enabling this will make supporting bedrock client with Geyser/Floodgate possible.

Enables shifting of single-plot worlds so the plot is positioned at (0, 0).

Opt-in by setting required-version to `3` and set enabled to `true` to use this feature.

<table><tbody><tr><td><sub>

```yml
plot-shifting:
    enabled: true
    required-version: 3
```

<p><code>config.yml</code> <b>line: 57</b></p>
</sub>
</td></tr></tbody></table>

---
### ♻ **terra-offset**
Optional config to terra server coordinates offset.
Set this if the reference terra server is using a different coordinate system.

<table><tbody><tr><td><sub>

```yml
terra-offset:
    offset-x: 0
    offset-z: 0
```

<p><code>config.yml</code> <b>line: 54</b></p>
</sub>
</td></tr></tbody></table>
