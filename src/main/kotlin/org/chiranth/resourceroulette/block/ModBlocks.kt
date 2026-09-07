package org.chiranth.resourceroulette.block

import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.chiranth.resourceroulette.ResourceRoulette

object ModBlocks {
    val ROULETTE_HOPPER: RouletteHopperBlock = register(
        "roulette_hopper",
        RouletteHopperBlock(AbstractBlock.Settings.create().strength(3.0f, 6.0f).nonOpaque())
    )

    private fun <T : Block> register(name: String, block: T): T {
        val id = Identifier.of(ResourceRoulette.MOD_ID, name)
        val registered = Registry.register(Registries.BLOCK, id, block)
        Registry.register(Registries.ITEM, id, BlockItem(registered, Item.Settings()))
        return registered
    }

    fun register() {
        // triggers class init
    }
}