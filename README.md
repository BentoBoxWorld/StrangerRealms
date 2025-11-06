# 🚧 StrangerRealms: A Dynamic Vanilla Survival Experience 🚧

---

StrangerRealms is a **BentoBox** game mode that introduces a thrilling, dynamic twist to the classic vanilla Minecraft survival experience. Your available world space constantly changes based on the server's population, making resource control and territory a critical, ever-shifting challenge.

## 🗺️ The Dynamic World Border

The world is constrained by a **variable world border** that expands or contracts in response to the number of players online:

* **Growth:** When new players join the server, the world border instantly **expands** based on a configurable rate per player.
* **Shrinkage:** If players log off, the world border will slowly **contract** over a set period, putting pressure on remote claims.

This mechanic encourages players to work together to keep the server populated, ensuring access to the farthest reaches of the world.

## 🏡 Claim Your Territory

StrangerRealms allows players to **claim territory** far from the central spawn point, providing a secure space to build and thrive.

### Key Claim Rules:

* **Distance:** Claims can only be made a certain distance away from the world spawn.
* **Limit:** Each player can own up to a maximum of **5 claims** (configurable).
* **Boundaries:** Claims cannot overlap with other claims or spawn or extend beyond the current world border.
* **Protection:** Once a claim is made, you own everything within that area, from bedrock to the build limit. **Blocks are fully protected** from all other players.
* **The Wild:** While protected from other players, your claim is **not protected from mobs**. Be sure to secure your base!

### Claim Management

You can fine-tune your claimed space using various settings:

* **Lock Down:** You have the ability to **lock** your claim, preventing all other players from entering.
* **Teleport:** Easily return to your properties using the `/cb` command:
    * `/cb go [claim name]` - Teleports you directly to your named claim.

## 🧭 The Edge of the World

The world border introduces a strategic element to claiming land:

* **Safe Zone:** If your claim is fully contained **within** the current world border, you will only see subtle **particles** at the claim's edge, allowing for free movement up to the boundary.
* **The Frontier:** If you made a claim when the server was heavily populated and the border has since shrunk, your claim may now sit **outside** the main world border.
    * You can still visit these frontier claims, but they will be surrounded by their own **non-traversable world border**, creating a secure, isolated homestead!
    * This rewards players who strategically venture out when the player count is high.

## ⚙️ Administrative Control (Admins)

Admins have two options for setting the world's primary spawn point:

1.  Use normal Minecraft spawn setting commands.
2.  (Future Development) Create a claim and designate it as the **world spawn**. This allows the central point of activity and potential claim area to be dynamically moved, unlocking new parts of the map for players to settle.

---

**Get ready to race the border, secure your land, and prove your ability to survive in the ever-changing world of StrangerRealms!**
