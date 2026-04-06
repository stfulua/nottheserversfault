<div align="center">

# 🛠️ NotTheServersFault

**The ultimate "unfair" Minecraft challenge plugin.**

[![Version](https://img.shields.io/badge/version-1.1.0-blue.svg?style=for-the-badge)](https://modrinth.com/plugin/nottheserversfault)

[![Platform](https://img.shields.io/badge/platform-Bukkit%20%2F%20Spigot%20%2F%20Paper%20%2F%20Purpur-green.svg?style=for-the-badge)](https://papermc.io)
[![Java](https://img.shields.io/badge/java-21-orange.svg?style=for-the-badge)](https://www.oracle.com/java/)

---

</div>

### ❓ Overview
**NotTheServersFault** is a unique challenge plugin designed for content creators or friends who want a bit of chaotic fun. The goal is simple: **Get a Diamond Block.** 

However, the world is full of "glitches" and reality-bending twists. From fake players in chat to blocks that refuse to stay broken, it creates an atmosphere of server lag that is actually part of the game.

### 🏆 Competition
This plugin is built for **multiple players** to compete at once. 
- **The Lobby:** Everyone starts in a glass box at Y=6700 with Interstellar-style music. 
- **Invincibility:** The lobby is a safe zone; damage and PvP are disabled.
- **Ready Up:** Use `/start` to mark yourself as ready. (You cannot start until terrain generation is finished).
- **Winning:** The first person to obtain a **Diamond Block** wins, and the challenge ends (displaying the total time taken).
- **Reset:** The plugin automatically shuts down the server once a winner is crowned. Upon restarting, it completely deletes the `world`, `world_nether`, and `world_the_end` folders (including `level.dat`, `uid.dat`, and locks) to force the server to natively generate a fresh, truly random new world.

---

### 🌑 The Twists
<details>
<summary><b>Click to reveal the "glitches" ⚠️</b></summary>

1.  **Block Desync:** Common blocks like Stone or Dirt might "revert" instantly after being broken.
2.  **Cursed Crafting:** 10% chance for crafts to fail and give you "trash" instead.
3.  **The Secret Diamond Ore:** Diamond Ore gives Dirt. Diamonds are found by mining a random **Secret Ore** (Gold, Redstone, Lapis, Emerald, Iron, or Coal) that changes every time the server starts.
4.  **Mob Ambush:** Mining the Secret Ore up close triggers scary sounds and an immediate ambush of Zombies, Skeletons, and Witches!
5.  **Daylight Spawns:** Hostile mobs spawn near you during the day and are immune to sunlight.
6.  **Ghost Players:** Realistic fake players join, chat, and leave to mess with your head.
7.  **Inventory Shuffle:** Your inventory randomly shuffles every 5 minutes.
8.  **Vanishing Structures:** Villages and structures disappear when you get too close.
9.  **Sun Immunity:** Monsters are permanently protected from fire and sunlight.

</details>

---

### ⚙️ Setup
- `/start`: Ready up in the lobby.
- `/ntsf reload`: Reload the configuration file.
- `/ntsf spectator <add|remove> <player>`: Add or remove players from the spectator list (automatically updates config).
- **Setup:** Drop the `.jar` into your plugins folder.

### 📝 Requirements
- **Minecraft 1.21.x** (Bukkit, Spigot, Paper, Purpur)
- **Java 21**
- **PacketEvents** (Required dependency)
- **Chunky** (Highly recommended for pre-generating terrain)

---

### 🔗 Links
- **Discord:** [Join for Support & Bug Reports](https://discord.gg/SNzUYWbc5Q)
- **Donation:** [Support the project on Ko-fi](https://ko-fi.com/v4bi)

### 📄 License

This project is licensed under the **vProLabs General License**.

- **Non-Commercial Use**: Free for personal and non-commercial use
- **Attribution Required**: Credit vProLabs and link to the original project
- **Share Alike**: Derivative works must use the same license
- **No Competing Works**: Derivatives cannot compete with or disparage the original
- **Commercial Licensing**: Contact vProLabs for commercial use rights

See [LICENSE](LICENSE) for full terms.

<div align="center">
  <sub>Built with ❤️ by vProLabs</sub>
</div>
