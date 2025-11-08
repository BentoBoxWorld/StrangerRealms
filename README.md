
<img width="600" height="300" alt="Stranger Realms" src="https://github.com/user-attachments/assets/4893497a-7c92-4947-b1af-23c1daf05261" />

# Strange Realms: A BentoBox Game Mode

A chilling new survival experience where the familiar world is shadowed by a terrifying, inverted dimension. *Stranger Realms* is a custom BentoBox game mode that replaces the Nether with a dark, distressed copy of the Overworld — the **Upside Down**.

## 🌌 Game Concept: Welcome to the Upside Down

Dive into a world where reality is twisted. Inspired by the chilling dimension in the popular TV show, the Upside Down is a dimension of constant gloom and danger.

  * **The Inversion:** The Upside Down is a dark, distressed, block-for-block copy of the Overworld. It is generated dynamically when a player first enters a location within the dimension.
  * **Corrupted Mobs:** The Overworld's fauna has been corrupted. Most Overworld mobs are transformed into their Nether counterparts, creating unique and dangerous encounters.
  * **The Glimmer (Dimensional Link):** Be careful what you interact with\! Activating a lever or pressing a button in the Upside Down can cause the same action to be triggered in the corresponding block in the Overworld. This dangerous connection can be used for hidden automation or can expose your position to the evils lurking on the other side.

## ✨ Key Features

### Protected Claims (BentoBox Core)

As a BentoBox game mode, survival hinges on laying claim to your piece of the world.

  * **Security:** Claims are protected square areas, with a default size of 64 blocks on each side.
  * **Customization:** Use the powerful BentoBox settings GUI to fine-tune your claim permissions — allow visitors to break blocks, use containers, or restrict access entirely.
  * **Dimensional Span:** Your claims are safe in **both** the Overworld and the Upside Down, and The End!
  * **Cooperative Growth:** Claims can grow\! Add other players as members to your claim to increase its size. The specific amount of claim growth per member is configurable.

### The Warped Compass

  * **Chunk Regeneration:** Discover the custom recipe for the **Warped Compass**. When a player holds this powerful item while traveling through a Nether Portal, it forces the regeneration of chunks in all directions, ensuring you always have fresh territory to explore based on the Overworld.

### Dynamic World Border (Admin Feature)

Encourage players to join and grow your server with the Dynamic World Border option.

  * **Adaptive Size:** Admins can set a world border that automatically grows or shrinks based on the current number of online players. Swarm the server and expand your playable area\!
  * **Claim Retention:** If the world border shrinks, players retain their claims even if they fall outside the new border boundary. However, once outside the border, they can only operate within the protected confines of their pre-existing claim borders, encouraging strategic land ownership.

## 🔨 Commands

*Stranger Realms* uses the robust BentoBox command structure.

The main player command is `/strange` or `/st` and the admin command is `/stranger`

| Command        | Description                                                                                                            |
|:---------------|:-----------------------------------------------------------------------------------------------------------------------|
| `/st claim`    | Creates a claim at your current location (if you have room).                                                           |
| `/st info`     | Shows information about the claim you are currently standing in.                                                       |
| `/st settings` | Opens the GUI to manage claim permissions (e.g., visitor block break, chest access).                                   |
| `/st team`     | Goes the Team GUI where you can invites players to become a member of your claim, which also increases the claim size. |

## Use with Other Addons

Like all BentoBox Game Modes you can load other Addons. These ones are recommended:

* Warps - enables player's to place one warp sign in a claim so others can teleport there
* InvSwitcher - keeps inventories and other player aspects separate between Game Modes

*Do not* use the Border Addon in the Stranger Realms because it will clash. Stranger Realms handles its own borders.

## ⚙️ Configuration Settings

The core game mechanics and claim features are highly configurable via the BentoBox configuration files or GUIs. The primary configuration file for this game mode is `config.yml`. 

## 💖 Support the Author

This game mode is built upon the incredible foundation of **BentoBox**. Please consider supporting **tastybento**, the author of BentoBox and other contributors to ensure the continued development and maintenance of this fantastic platform.

Your sponsorship helps keep projects like *Stranger Realms* possible\!

