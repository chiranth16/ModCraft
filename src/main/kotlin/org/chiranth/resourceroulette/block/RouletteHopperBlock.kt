package org.chiranth.resourceroulette.block

import com.mojang.serialization.MapCodec
import net.minecraft.block.AbstractBlock.Settings
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import org.chiranth.resourceroulette.block.entity.ModBlockEntities
import org.chiranth.resourceroulette.block.entity.RouletteHopperBlockEntity

class RouletteHopperBlock(settings: Settings) : BlockWithEntity(settings) {

    companion object {
        // Approximates the real hopper shape: a wide top rim + a narrow spout,
        // rather than a solid cube — this is what lets a chest below still open.
        private val TOP_PLATE: VoxelShape = Block.createCuboidShape(0.0, 10.0, 0.0, 16.0, 11.0, 16.0)
        private val WEST_WALL: VoxelShape = Block.createCuboidShape(0.0, 11.0, 0.0, 2.0, 16.0, 16.0)
        private val EAST_WALL: VoxelShape = Block.createCuboidShape(14.0, 11.0, 0.0, 16.0, 16.0, 16.0)
        private val NORTH_WALL: VoxelShape = Block.createCuboidShape(2.0, 11.0, 0.0, 14.0, 16.0, 2.0)
        private val SOUTH_WALL: VoxelShape = Block.createCuboidShape(2.0, 11.0, 14.0, 14.0, 16.0, 16.0)
        private val SPOUT: VoxelShape = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 10.0, 12.0)

        private val SHAPE: VoxelShape = VoxelShapes.union(TOP_PLATE, WEST_WALL, EAST_WALL, NORTH_WALL, SOUTH_WALL, SPOUT)
    }

    override fun getCodec(): MapCodec<RouletteHopperBlock> = createCodec(::RouletteHopperBlock)

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        RouletteHopperBlockEntity(pos, state)

    override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE
    override fun getCollisionShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE
    override fun getRaycastShape(state: BlockState, world: BlockView, pos: BlockPos): VoxelShape = SHAPE

    override fun <T : BlockEntity> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (world.isClient) return null
        return validateTicker(type, ModBlockEntities.ROULETTE_HOPPER, RouletteHopperBlockEntity::tick)
    }
}