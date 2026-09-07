package org.chiranth.resourceroulette.block.entity

import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.chiranth.resourceroulette.ResourceRoulette
import org.chiranth.resourceroulette.block.ModBlocks

object ModBlockEntities {
    val ROULETTE_HOPPER: BlockEntityType<RouletteHopperBlockEntity> = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(ResourceRoulette.MOD_ID, "roulette_hopper"),
        BlockEntityType.Builder.create(::RouletteHopperBlockEntity, ModBlocks.ROULETTE_HOPPER).build(null)
    )

    fun register() {
        // triggers class init
    }
}