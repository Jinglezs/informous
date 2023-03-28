Informous is a plugin specifically for Paper servers that relays exceptions thrown on the server to configured text channels on a Discord server.

# Plugin Configuration
This plugin does not save a default configuration, and certain information must be provided to enable Discord functionality.
The following fields are required in this plugin's config.yml:
- "discord_token" (String) Defines the token required to make use of Discord's API. This can be obtained in the Discord Developer Portal. [Click here](https://discord.com/developers/docs/topics/oauth2#bots) to read the official documentation on Discord bot set-up and authentication.
- "guild-id" (Long) Defines which Discord guild to register the slash-commands under. This can be obtained by enabling developer settings in Discord and right clicking on the guild your bot resides in.

The IDs of register relay channels will automatically be saved to/removed from the configuration file after using the designated slash commands. The list can be edited manually, but should only be done so while the plugin is disabled. Changes to the configuration only take effect upon enabling the plugin.

# Defining Relay Channels
Informous registers two slash commands that allow you to add or remove relay channels. Note that these commands are only functional while the plugin is enabled and has connected to Discord.
- `/relay channel add <text channel>`
- `/relay channel remove <text channel>`
  
The channel ID cannot be entered directly while executing a slash command. Discord will suggest channels while typing in the command arguments- use tab completion or click on a suggest channel to select it.
Relay channels added/removed via slash command take effect immediately.
