package me.bscal.runecraft

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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
		Registry.put(RuneType.Default, Default)
		Registry.put(RuneType.Overworld, OverworldBoard())
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
		var x = 0
		var y = 0
		for (i in 0 until board.Size)
		{
			val key = x or (y shl 16)

			if (y < 2 && Random.nextInt(1, 6) > 2) board.Slots[key] = DirtSlot(y < 1)
			else board.Slots[key] = BaseSlot

			x++
			if (x > 5)
			{
				x = 0
				y++
			}
		}
	}

	override fun Generate(player: Player, board: RuneBoard, rarity: Int)
	{
		GenerateBase(player, board, rarity)

		val decided = IntOpenHashSet(board.Slots.size, 1.0f)

		var bedrockCount = Random.nextInt(2, 5)
		while (bedrockCount > 0)
		{
			val key = Random.nextInt(6) or (Random.nextInt(6) shl 16)
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
			val key = Random.nextInt(6) or (Random.nextInt(6) shl 16)
			if (!decided.contains(key))
			{
				decided.add(key)
				board.Slots[key] = DiamondSlot()
				gemCount--
			}
		}
	}
}