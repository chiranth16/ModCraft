Resource Roulette

Resource Roulette is a Minecraft Java Edition 1.21.1 Fabric mod that adds a custom **Roulette Hopper** with a risk-and-reward resource processing system.

Players place resources into an input chest connected to the Roulette Hopper. After a short processing period, the hopper performs a weighted random roll and sends a result to the output chest. Results can range from losing the entire input to receiving a small return, a decent reward, or a rare jackpot.


Features

* Custom Roulette Hopper block
* Weighted random reward system
* Five resource tiers:

  * Basic
  * Common
  * Valuable
  * Rare
  * Endgame
* Four result categories:

  * Loss
  * Small
  * Decent
  * Jackpot
* Three-second processing period
* Input chest and output chest integration
* Pending reward handling when the output inventory is full
* Category-specific sounds, particles, and action-bar feedback
* Quantity scaling based on the input stack
* Separate reward, block, and block-entity logic

How It Works

```text
Input Chest
     |
     v
Roulette Hopper
     |
     v
Output Chest
```

1. Resources are placed in the inventory above the Roulette Hopper.
2. The hopper takes an available stack.
3. The stack is processed for three seconds.
4. A reward category is selected using weighted randomness.
5. A reward is selected from the pool associated with the input item's tier.
6. The result is delivered to the output inventory.
7. The player receives feedback describing the result.

Reward System

The default category weights are:

| Category | Weight | Probability |
| -------- | -----: | ----------: |
| Loss     |     15 |         15% |
| Small    |     45 |         45% |
| Decent   |     30 |         30% |
| Jackpot  |     10 |         10% |

Result Categories

Loss

The input is consumed and nothing is returned.

Small

The player receives a minor consolation reward.

Decent

The player receives a solid, worthwhile reward.

Jackpot

The player receives a rare and potentially powerful reward.

Reward Flow

```text
Input Item
    |
    v
Determine Item Tier
    |
    v
Select Reward Category
    |
    v
Select Weighted Reward
    |
    v
Calculate Quantity
    |
    v
Deliver Reward
```

Rewards can either return the original item using a quantity multiplier or provide a different item using a fixed quantity.

Example:

```text
64 Iron
   |
   +--> Loss    -> Nothing
   |
   +--> Small   -> Small consolation reward
   |
   +--> Decent  -> Worthwhile reward
   |
   +--> Jackpot -> Rare high-value reward
```

Inventory Handling

Rewards are not deleted when the output inventory cannot accept them.

```text
Reward Generated
       |
       v
Output Inventory
    /       \
 Space     Full
   |          |
   v          v
Deliver    Keep Pending
              |
              v
          Retry Later
```

This prevents a generated reward from being discarded simply because the output inventory is temporarily full.

Crafting Recipe

The Roulette Hopper is designed as an upgraded version of the vanilla hopper.

```text
Diamond   Diamond   Diamond
Diamond   Hopper    Diamond
Diamond   Diamond   Diamond

            |

            v

       Roulette Hopper
```

Recipe:

8 Diamonds + 1 Hopper → 1 Roulette Hopper

Technology

* Minecraft Java Edition 1.21.1
* Fabric
* Kotlin
* Gradle
* IntelliJ IDEA
* Fabric API
* Fabric Language Kotlin
* Fabric Data Generation API
* Git

Running the Mod

Open the project in IntelliJ IDEA and allow Gradle to synchronize.

Run the generated **Minecraft Client** configuration, or use:

```bash
./gradlew runClient
```

On Windows:

```bash
gradlew.bat runClient
```

Testing

Test different resource tiers and stack sizes, including:

```text
1 Iron
16 Iron
64 Iron

1 Diamond
16 Diamond
64 Diamond

1 Netherite Ingot
16 Netherite Ingot
64 Netherite Ingot
```


Design Goal

Resource Roulette is built around a simple mechanic:

> Risk your resources for the chance to receive something better.

The system is intended to provide meaningful risk without making every non-jackpot result feel worthless. Reward pools and probabilities can be adjusted through playtesting to maintain a balanced and enjoyable experience.

License

This project is currently intended as a personal portfolio and development project.

Add a preferred open-source license before public distribution.

Author

Chiranth C V
