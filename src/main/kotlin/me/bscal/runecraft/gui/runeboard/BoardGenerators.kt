package me.bscal.runecraft.gui.runeboard

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.*
import me.bscal.runecraft.items.runeitems.BreakLevel
import org.bukkit.Material
import org.bukkit.entity.Player
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
	var BaseSlot = DefaultSlot(Material.STONE, 1, BreakLevel.LEVEL_1)
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
			if (y < 2 && Random.nextFloat() > .25f) board.Slots.add(i, DirtSlot(y < 1))
			else board.Slots.add(i, BaseSlot)
		}
	}

	override fun Generate(player: Player, board: RuneBoard, rarity: Int)
	{
		GenerateBase(player, board, rarity)

		val decided = IntOpenHashSet(board.Slots.size, 1.0f)

		var bedrockCount = Random.nextInt(2, 4)
		while (bedrockCount > 0)
		{
			val key = RuneBoard.PackCoord(Random.nextInt(5), Random.nextInt(5))
			if (!decided.contains(key))
			{
				decided.add(key)
				board.Slots[key] = BedrockSlot()
				bedrockCount--
			}
		}

		var gemCount = Random.nextInt(2, 3)
		while (gemCount > 0)
		{
			val key = RuneBoard.PackCoord(Random.nextInt(5), Random.nextInt(5))
			if (!decided.contains(key))
			{
				decided.add(key)
				board.Slots[key] = DiamondSlot()
				gemCount--
			}
		}
	}
}