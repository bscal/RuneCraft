package me.bscal.runecraft.gui.runeboard

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.*
import me.bscal.runecraft.gui.runeboard.slots.*
import me.bscal.runecraft.items.runeitems.BreakLevel
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.Serializable
import kotlin.random.Random

object BoardRegistry
{
	var Default: BoardGenerator = OverworldBoard()
	val Registry = Object2ObjectOpenHashMap<RuneType, BoardGenerator>()

	init
	{
		Registry[RuneType.Default] = Default
		Registry[RuneType.Overworld] = OverworldBoard()
	}

}

data class FeatureData(val rarity: Double, val min: Int, val max: Int)
{}

interface BoardGenerator : Serializable
{
	fun Generate(player: Player, board: RuneBoard, rarity: Int)
}

abstract class BaseBoardGenerator(val FinalValue: Int) : BoardGenerator
{
	var BaseSlot = DefaultSlot(ItemStack(Material.STONE), 1, BreakLevel.LEVEL_1)
	val Features: Object2ObjectOpenHashMap<BoardSlot, FeatureData> = Object2ObjectOpenHashMap()

	protected var CurrentValue = 0

	abstract fun GenerateBase(player: Player, board: RuneBoard, rarity: Int)
}

class OverworldBoard : BaseBoardGenerator(8)
{
	override fun GenerateBase(player: Player, board: RuneBoard, rarity: Int)
	{
		for (i in 0 until board.Size)
		{
			val y = i / 6
			if (y < 2 && Random.nextFloat() > .25f) board.Rune.BoardSlots.Slots.add(i, DirtSlot(y < 1))
			else board.Rune.BoardSlots.Slots.add(i, BaseSlot)
		}
	}

	override fun Generate(player: Player, board: RuneBoard, rarity: Int)
	{
		GenerateBase(player, board, rarity)

		val decided = IntOpenHashSet(board.Rune.BoardSlots.Slots.size, 1.0f)

		var bedrockCount = Random.nextInt(1, 3)
		while (bedrockCount > 0)
		{
			val key = RuneBoard.PackCoord(Random.nextInt(6), Random.nextInt(6))
			if (!decided.contains(key))
			{
				decided.add(key)
				board.Rune.BoardSlots.Slots[key] = BedrockSlot()
				bedrockCount--
			}
		}

		var gemCount = 1
		while (gemCount > 0)
		{
			val key = RuneBoard.PackCoord(Random.nextInt(6), Random.nextInt(6))
			if (!decided.contains(key))
			{
				decided.add(key)
				board.Rune.BoardSlots.Slots[key] = DiamondSlot()
				gemCount--
			}
		}

		var emptySlot = Random.nextInt(0, 2)
		while (emptySlot > 0)
		{
			val key = RuneBoard.PackCoord(Random.nextInt(6), Random.nextInt(6))
			if (!decided.contains(key))
			{
				decided.add(key)
				board.Rune.BoardSlots.Slots[key] = EmptySlot()
				emptySlot--
			}
		}
	}
}