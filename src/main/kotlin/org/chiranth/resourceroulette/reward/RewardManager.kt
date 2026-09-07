package org.chiranth.resourceroulette.reward

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import kotlin.random.Random

data class RollResult(val category: RewardCategory, val item: Item?, val count: Int)

private data class PoolEntry(val item: Item, val count: Int, val weight: Int = 1)

object RewardManager {

    // ---------- helpers ----------

    private fun item(id: String): Item = Registries.ITEM.get(Identifier.of(id))

    private fun entries(ids: List<String>, count: Int = 1, weight: Int = 1): List<PoolEntry> =
        ids.map { PoolEntry(item(it), count, weight) }

    private val toolTypes = listOf("sword", "pickaxe", "axe", "shovel", "hoe")
    private val armorPieces = listOf("helmet", "chestplate", "leggings", "boots")

    private fun toolIds(materials: List<String>): List<String> =
        materials.flatMap { m -> toolTypes.map { t -> "minecraft:${m}_${t}" } }

    private fun armorIds(materials: List<String>): List<String> =
        materials.flatMap { m -> armorPieces.map { p -> "minecraft:${m}_${p}" } }

    private val dyeColors = listOf(
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
    )

    // ---------- SMALL pool: junk-tier / mild consolation ----------

    private val smallPool: List<PoolEntry> =
        entries(toolIds(listOf("wooden", "stone")), count = 1, weight = 3) +
                entries(armorIds(listOf("leather", "chainmail")), count = 1, weight = 3) +
                entries(dyeColors.map { "minecraft:${it}_dye" }, count = 3, weight = 2) +
                entries(dyeColors.map { "minecraft:${it}_wool" }, count = 4, weight = 1) +
                entries(
                    listOf(
                        "minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds",
                        "minecraft:pumpkin_seeds", "minecraft:oak_sapling", "minecraft:spruce_sapling",
                        "minecraft:birch_sapling", "minecraft:jungle_sapling", "minecraft:acacia_sapling",
                        "minecraft:dark_oak_sapling", "minecraft:mangrove_propagule", "minecraft:cherry_sapling",
                        "minecraft:bamboo", "minecraft:azalea"
                    ),
                    count = 2, weight = 2
                ) +
                entries(
                    listOf(
                        "minecraft:bread", "minecraft:apple", "minecraft:carrot", "minecraft:potato",
                        "minecraft:baked_potato", "minecraft:cooked_beef", "minecraft:cooked_chicken",
                        "minecraft:cooked_porkchop", "minecraft:cooked_mutton", "minecraft:cooked_rabbit",
                        "minecraft:cooked_cod", "minecraft:cooked_salmon", "minecraft:cookie",
                        "minecraft:pumpkin_pie", "minecraft:melon_slice"
                    ),
                    count = 3, weight = 2
                ) +
                entries(
                    listOf(
                        "minecraft:arrow", "minecraft:torch", "minecraft:leather", "minecraft:feather",
                        "minecraft:bone", "minecraft:string", "minecraft:flint", "minecraft:charcoal",
                        "minecraft:iron_nugget", "minecraft:gunpowder", "minecraft:coal", "minecraft:book"
                    ),
                    count = 4, weight = 3
                )

    // ---------- DECENT pool: solid, worthwhile items ----------

    private val decentPool: List<PoolEntry> =
        entries(toolIds(listOf("iron", "golden")), count = 1, weight = 3) +
                entries(armorIds(listOf("iron", "golden")), count = 1, weight = 3) +
                entries(
                    listOf(
                        "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:redstone",
                        "minecraft:lapis_lazuli", "minecraft:ender_pearl", "minecraft:emerald",
                        "minecraft:amethyst_shard", "minecraft:experience_bottle", "minecraft:golden_apple",
                        "minecraft:saddle", "minecraft:name_tag", "minecraft:crossbow", "minecraft:bow",
                        "minecraft:shield", "minecraft:fishing_rod", "minecraft:spyglass", "minecraft:compass",
                        "minecraft:clock", "minecraft:lead"
                    ),
                    count = 1, weight = 3
                ) +
                entries(
                    listOf(
                        "minecraft:music_disc_13", "minecraft:music_disc_cat", "minecraft:music_disc_blocks",
                        "minecraft:music_disc_chirp", "minecraft:music_disc_far", "minecraft:music_disc_mall",
                        "minecraft:music_disc_mellohi", "minecraft:music_disc_stal", "minecraft:music_disc_strad",
                        "minecraft:music_disc_ward", "minecraft:music_disc_wait"
                    ),
                    count = 1, weight = 1
                )

    // ---------- JACKPOT pool: rare / powerful items ----------

    private val jackpotPool: List<PoolEntry> =
        entries(toolIds(listOf("diamond", "netherite")), count = 1, weight = 4) +
                entries(armorIds(listOf("diamond", "netherite")), count = 1, weight = 4) +
                entries(
                    listOf(
                        "minecraft:diamond", "minecraft:emerald", "minecraft:netherite_scrap",
                        "minecraft:netherite_ingot", "minecraft:enchanted_golden_apple", "minecraft:nether_star",
                        "minecraft:totem_of_undying", "minecraft:trident", "minecraft:elytra",
                        "minecraft:shulker_box", "minecraft:beacon", "minecraft:dragon_egg",
                        "minecraft:heart_of_the_sea", "minecraft:conduit"
                    ),
                    count = 1, weight = 2
                )

    // ---------- rolling ----------

    fun roll(inputItem: Item, inputCount: Int, random: Random = Random.Default): RollResult {
        val category = pickCategory(random)
        val pool = when (category) {
            RewardCategory.LOSS -> return RollResult(category, null, 0)
            RewardCategory.SMALL -> smallPool
            RewardCategory.DECENT -> decentPool
            RewardCategory.JACKPOT -> jackpotPool
        }
        val entry = pickWeighted(pool, random) { it.weight }
        return RollResult(category, entry.item, entry.count)
    }

    private fun pickCategory(random: Random): RewardCategory =
        pickWeighted(RewardCategory.entries, random) { it.weight }

    private fun <T> pickWeighted(entries: List<T>, random: Random, weightOf: (T) -> Int): T {
        val total = entries.sumOf { weightOf(it) }
        var roll = random.nextInt(total)
        for (entry in entries) {
            roll -= weightOf(entry)
            if (roll < 0) return entry
        }
        return entries.last()
    }

    fun toStacks(item: Item, count: Int): List<ItemStack> {
        val stacks = mutableListOf<ItemStack>()
        var remaining = count
        val maxStack = item.defaultStack.maxCount
        while (remaining > 0) {
            val chunk = remaining.coerceAtMost(maxStack)
            stacks.add(ItemStack(item, chunk))
            remaining -= chunk
        }
        return stacks
    }
}