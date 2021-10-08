package me.bscal.runecraft

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


interface BoardGenerator : Serializable
{
	fun Generate(player: Player, board: RuneBoard)
}

class OverworldBoard : BoardGenerator
{

	val BaseSlot = DefaultSlot(Material.STONE)

	override fun Generate(player: Player, board: RuneBoard)
	{
		for (i in 0 until board.Size)
		{
			board.Slots[i] = BaseSlot
		}

		board.Slots[Random.nextInt(0, 5) + Random.nextInt(0, 5)] = DiamondSlot()
		board.Slots[Random.nextInt(0, 5) + Random.nextInt(0, 5)] = DiamondSlot()
	}

}