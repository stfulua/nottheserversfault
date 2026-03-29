# NotTheServersFault

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft: 1.21.x](https://img.shields.io/badge/Minecraft-1.21--1.21.4-blue.svg)](https://www.minecraft.net/)

A Minecraft Spigot/Paper plugin designed for a unique "Twist" gameplay experience, where one target player is subtly (and not so subtly) trolled by the environment while trying to complete a goal.

## 🚀 Features

- **Lobby System**: Players start in a high-altitude lobby with custom music and visuals.
- **Grace Period**: A configurable countdown before the twists activate.
- **Dynamic Twists**:
  - **Inventory Shuffle**: Items randomly rearrange themselves.
  - **Block Desync**: Blocks occasionally reappear after being broken.
  - **Cursed Crafting**: Crafting attempts might "glitch" and yield useless items.
  - **Structure Disappearance**: Generated structures (villages, etc.) vanish as the player approaches.
  - **Herobrine**: A ghostly entity that stalks the player from the distance.
  - **Ambience & Paranoia**: Creepy sounds and subtle visual cues.
- **Admin Tools**: Hidden commands and "Chaos Wands" for manual trolling.

## 🛠️ Installation

1. Download the latest `.jar` from the [releases page](https://github.com/stfulua/nottheserversfault/releases).
2. Place the jar in your server's `plugins/` folder.
3. **Requirement**: Install [PacketEvents](https://github.com/retrooper/packetevents) on your server.
4. **Recommended**: Install [Chunky](https://github.com/pop4959/Chunky) for pre-generating the world.
5. Restart the server.

## ⚙️ Configuration

Edit `plugins/NotTheServersFault/config.yml` to set your target and admin players:

```yaml
settings:
  target-player: "Saf1_K"
  admin-player: "v4bi"
```

## 🎮 Commands

- `/start` - Begins the game (must be in lobby).
- `/ntsf <herobrine|vanish>` - Admin commands (requires `ntsf.admin` permission or being the configured admin).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Credits

Developed by **vProLabs**.
- Website: [vprolabs.xyz](https://vprolabs.xyz)
- Discord: [Join our community](https://discord.gg/SNzUYWbc5Q)
- Support: [Donate via Ko-fi](https://ko-fi.com/v4bi)
