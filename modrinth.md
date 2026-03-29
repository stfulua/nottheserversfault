# 👻 NotTheServersFault

**"It's not the server's fault, it's just the game... or is it?"**

NotTheServersFault is a high-quality, production-ready "Troll Challenge" plugin for Minecraft servers. Perfect for content creators, events, or large-scale multiplayer chaos. **Every player who joins becomes a target**, tasked with reaching the goal while the world slowly starts to "break" around them.

---

## 💎 Features Overview

- **Mass Multiplayer Support**: No longer limited to one target! Every non-admin player is part of the challenge.
- **Ready System**: The challenge only begins once **all online players** have typed `/start`. No more starting too early!
- **Winner Broadcasts**: When a player completes the challenge, the entire server is notified of their victory.
- **Dynamic Lobby System**: Custom-built high-altitude glass box with immersive music and visual effects.
- **Tab List Ghosting**: Simulated "fake" players randomly appear in the Tab list to create a sense of not being alone.
- **Fake Player Chat**: These ghostly presences will occasionally send cryptic messages in chat.
- **Configurable Exclusions**: Easily exclude admins or specific players from the twists via the `config.yml`.
- **Seamless Integration**: Fully compatible with **PacketEvents** for high-performance ghostly visuals.

---

## 🎭 The Twists (Spoiler Alert!)

<details>
<summary><b>CLICK TO REVEAL THE TWISTS (PLAYERS BEWARE!)</b></summary>

The "Twists" are the core of the experience. They activate after a short grace period and subtly disrupt the target players:

- **🔄 Inventory Shuffle**: At random intervals, the player's inventory contents will shuffle themselves.
- **🧱 Block Desync**: Common blocks (stone, dirt, wood) have a chance to "desync"—they disappear when broken, but reappear a split second later.
- **⚒️ Cursed Crafting**: Crafting attempts can "glitch," resulting in useless items instead of what they intended.
- **🏚️ Vanishing Civilizations**: As the player approaches a generated structure, the building materials will rapidly vanish into thin air.
- **👀 Stalker (Herobrine)**: A ghostly entity stalks players from the distance. Sometimes he watches, sometimes he gets close...
- **👻 Phantom Interactions**: Nearby doors and trapdoors will randomly open and close by themselves.
- **🎧 Paranoia Ambience**: Creepy sounds play behind players, accompanied by occasional "phantom hits."
- **💎 Diamond Swap**: The ultimate troll—Diamond ores drop dirt, while Lapis ores drop Diamonds!

</details>

---

## 🛠️ Installation

1. Download and install **[PacketEvents](https://modrinth.com/plugin/packetevents)** (Required).
2. Download and install **[Chunky](https://modrinth.com/plugin/chunky)** (Highly Recommended for pre-generating the world).
3. Drop `NotTheServersFault.jar` into your `plugins` folder.
4. Restart your server.
5. Configure excluded players (admins) in `plugins/NotTheServersFault/config.yml`.

---

## ⚙️ Configuration

The plugin is designed for multi-player support. List players you want to exclude:

```yaml
settings:
  excluded-players:
    - "AdminUser1"
    - "YourUsername"
```

---

## 🚀 Performance Notes

- **Packet-Based Ghosting**: Herobrine and fake players are client-side via packets, ensuring zero impact on server entity counts.
- **Optimized Scanning**: Structure disappearance uses an efficient batch-removal system.
- **Version Compatibility**: Supports Minecraft 1.21 up to 1.21.4.

---

## 🔗 Links & Credits

- **Developer**: [vProLabs](https://vprolabs.xyz)
- **Discord**: [Join our Discord](https://discord.gg/SNzUYWbc5Q)
- **Support the Project**: [Ko-fi](https://ko-fi.com/v4bi)
- **GitHub**: [Source Code](https://github.com/stfulua/nottheserversfault)

---

*Licensed under the MIT License.*
