package org.chiranth.resourceroulette.block.entity

import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.chiranth.resourceroulette.reward.RewardCategory
import org.chiranth.resourceroulette.reward.RewardManager
import org.chiranth.resourceroulette.reward.RollResult
import kotlin.random.Random

private const val PROCESS_TICKS = 60 // 3 seconds

class RouletteHopperBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.ROULETTE_HOPPER, pos, state) {

    private var processing = false
    private var processTicks = 0
    private var currentInputItem: Item? = null
    private var currentInputCount = 0

    private var pendingItem: Item? = null
    private var pendingCount = 0

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, be: RouletteHopperBlockEntity) {
            if (world.isClient || world !is ServerWorld) return

            if (be.pendingItem != null) {
                be.tryDeliverPending(world, pos)
                if (be.pendingItem != null) return
            }

            if (be.processing) {
                be.processTicks++
                if (be.processTicks >= PROCESS_TICKS) {
                    be.finishRoll(world, pos)
                }
                be.markDirty()
                return
            }

            val inputInv = getInventoryAt(world, pos.up()) ?: return
            val slot = firstOccupiedSlot(inputInv) ?: return
            val stack = inputInv.getStack(slot)
            if (stack.isEmpty) return

            val taken = inputInv.removeStack(slot, stack.count)
            if (taken.isEmpty) return

            be.currentInputItem = taken.item
            be.currentInputCount = taken.count
            be.processing = true
            be.processTicks = 0
            inputInv.markDirty()
            be.markDirty()

            world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 0.6f, 1.0f)
        }

        private fun firstOccupiedSlot(inv: Inventory): Int? {
            for (i in 0 until inv.size()) {
                if (!inv.getStack(i).isEmpty) return i
            }
            return null
        }

        fun getInventoryAt(world: World, pos: BlockPos): Inventory? {
            val state = world.getBlockState(pos)
            val block = state.block
            val be = world.getBlockEntity(pos)
            if (be is ChestBlockEntity && block is ChestBlock) {
                return ChestBlock.getInventory(block, state, world, pos, true)
            }
            return be as? Inventory
        }

        fun insertItems(inv: Inventory, item: Item, count: Int): Int {
            var remaining = count
            val maxStack = item.defaultStack.maxCount

            for (i in 0 until inv.size()) {
                if (remaining <= 0) break
                val existing = inv.getStack(i)
                if (!existing.isEmpty && existing.item == item && existing.count < maxStack) {
                    val space = maxStack - existing.count
                    val add = space.coerceAtMost(remaining)
                    existing.count += add
                    remaining -= add
                }
            }
            for (i in 0 until inv.size()) {
                if (remaining <= 0) break
                if (inv.getStack(i).isEmpty) {
                    val add = remaining.coerceAtMost(maxStack)
                    inv.setStack(i, ItemStack(item, add))
                    remaining -= add
                }
            }
            inv.markDirty()
            return remaining
        }
    }

    private fun finishRoll(world: ServerWorld, pos: BlockPos) {
        val item = currentInputItem ?: return
        val count = currentInputCount
        val result = RewardManager.roll(item, count, Random.Default)

        processing = false
        currentInputItem = null
        currentInputCount = 0

        if (result.item == null) {
            // LOSS: input is already consumed, nothing to deliver
            announce(world, pos, result)
            markDirty()
            return
        }

        pendingItem = result.item
        pendingCount = result.count

        announce(world, pos, result)
        tryDeliverPending(world, pos)
        markDirty()
    }

    private fun tryDeliverPending(world: World, pos: BlockPos) {
        val item = pendingItem ?: return
        val outputPositions = listOf(pos.down(), pos.north(), pos.south(), pos.east(), pos.west())

        var remaining = pendingCount
        for (outPos in outputPositions) {
            if (remaining <= 0) break
            val outputInv = getInventoryAt(world, outPos) ?: continue
            remaining = insertItems(outputInv, item, remaining)
        }

        if (remaining <= 0) {
            pendingItem = null
            pendingCount = 0
        } else {
            pendingCount = remaining
        }
    }

    private fun announce(world: ServerWorld, pos: BlockPos, result: RollResult) {
        val center = Vec3d.ofCenter(pos)

        when (result.category) {
            RewardCategory.JACKPOT -> {
                val name = Registries.ITEM.getId(result.item!!)
                world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 1.0f, 1.0f)
                world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, center.x, center.y + 0.5, center.z, 40, 0.4, 0.4, 0.4, 0.3)
                broadcastNearby(world, pos, "\u00a76\u00a7l\u2666 JACKPOT! \u00a7r\u00a7e${result.count} ${name.path}")
            }
            RewardCategory.DECENT -> {
                val name = Registries.ITEM.getId(result.item!!)
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.7f, 1.4f)
                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y + 0.5, center.z, 12, 0.3, 0.3, 0.3, 0.0)
                broadcastNearby(world, pos, "\u00a7a\u2728 Nice! \u00a7r${result.count} ${name.path}")
            }
            RewardCategory.SMALL -> {
                val name = Registries.ITEM.getId(result.item!!)
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.6f, 1.0f)
                broadcastNearby(world, pos, "\u00a7f${result.count} ${name.path}")
            }
            RewardCategory.LOSS -> {
                world.playSound(null, pos, SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.BLOCKS, 0.6f, 1.0f)
                world.spawnParticles(ParticleTypes.SMOKE, center.x, center.y + 0.5, center.z, 10, 0.3, 0.3, 0.3, 0.02)
                broadcastNearby(world, pos, "\u00a7c\u2716 You lost this bet...")
            }
        }
    }

    private fun broadcastNearby(world: ServerWorld, pos: BlockPos, message: String) {
        world.getPlayers { it.blockPos.isWithinDistance(pos, 12.0) }.forEach { player ->
            player.sendMessage(Text.literal(message), true)
        }
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)
        nbt.putBoolean("Processing", processing)
        nbt.putInt("ProcessTicks", processTicks)
        currentInputItem?.let { nbt.putString("InputItem", Registries.ITEM.getId(it).toString()) }
        nbt.putInt("InputCount", currentInputCount)
        pendingItem?.let { nbt.putString("PendingItem", Registries.ITEM.getId(it).toString()) }
        nbt.putInt("PendingCount", pendingCount)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        processing = nbt.getBoolean("Processing")
        processTicks = nbt.getInt("ProcessTicks")
        currentInputCount = nbt.getInt("InputCount")
        pendingCount = nbt.getInt("PendingCount")
        currentInputItem = nbt.getString("InputItem").takeIf { it.isNotEmpty() }
            ?.let { Registries.ITEM.get(Identifier.of(it)) }
        pendingItem = nbt.getString("PendingItem").takeIf { it.isNotEmpty() }
            ?.let { Registries.ITEM.get(Identifier.of(it)) }
    }
}