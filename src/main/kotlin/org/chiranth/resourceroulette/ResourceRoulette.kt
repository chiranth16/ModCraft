package org.chiranth.resourceroulette

import net.fabricmc.api.ModInitializer
import org.chiranth.resourceroulette.block.ModBlocks
import org.chiranth.resourceroulette.block.entity.ModBlockEntities
import org.slf4j.LoggerFactory

object ResourceRoulette : ModInitializer {
    const val MOD_ID = "resourceroulette"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        ModBlocks.register()
        ModBlockEntities.register()
        LOGGER.info("Resource Roulette initialized")
    }
}