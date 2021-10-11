package me.bscal.runecraft

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
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
{
}


interface BoardGenerator : Serializable
{
	fun Generate(player: Player, board: RuneBoard, rarity: Int) : Array<BoardSlot>
}

abstract class BaseBoardGenerator(val FinalValue: Int) : BoardGenerator
{
	var BaseSlot = DefaultSlot(Material.STONE)
	val Features: Object2ObjectOpenHashMap<BoardSlot, FeatureData> = Object2ObjectOpenHashMap()

	protected var CurrentValue = 0

	abstract fun CommonPass(player: Player, board: RuneBoard, rarity: Int)

	abstract fun UniquePass(player: Player, board: RuneBoard, rarity: Int)
}

class OverworldBoard : BaseBoardGenerator(8)
{

	override fun Generate(player: Player, board: RuneBoard, rarity: Int) : Array<BoardSlot>
	{
		val slots = Array<BoardSlot>(board.Size) { BaseSlot }
		slots[Random.nextInt(0, 6 * 6)] = DiamondSlot()
		slots[Random.nextInt(0, 6 * 6)] = DiamondSlot()
		return slots
	}

	override fun CommonPass(player: Player, board: RuneBoard, rarity: Int)
	{
		TODO("Not yet implemented")
	}

	override fun UniquePass(player: Player, board: RuneBoard, rarity: Int)
	{
		TODO("Not yet implemented")
	}

}