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


interface BoardGenerator : Serializable
{
	fun Generate(player: Player, board: RuneBoard) : Array<BoardSlot>
}

class OverworldBoard : BoardGenerator
{

	val BaseSlot = DefaultSlot(Material.STONE)

	override fun Generate(player: Player, board: RuneBoard) : Array<BoardSlot>
	{
		val slots = Array<BoardSlot>(board.Size) { BaseSlot }
		slots[Random.nextInt(0, 6 * 6)] = DiamondSlot()
		slots[Random.nextInt(0, 6 * 6)] = DiamondSlot()
		return slots
	}

}