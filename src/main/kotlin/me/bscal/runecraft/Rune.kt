package me.bscal.runecraft

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Rune(val Type: RuneType, val Rarity: Int, val Color: Int, var Instability : Int)
{
	var Power: Float = 0f
	var IsGenerated: Boolean = false
	var IsBuilt: Boolean = false

	private var Board: RuneBoard? = null

	companion object {
	}

	fun Generate(player: Player) : Boolean
	{
		return Board?.Generate(player) ?: false
	}

	fun Open(player: Player)
	{
		Board?.Open(player)
	}

	fun AddRuneToItem(player: Player, itemStack: ItemStack) : Boolean
	{
		return true
	}

	fun WriteToItemstack(itemStack: ItemStack)
	{

	}

	fun Serialize()
	{

	}

	fun Deserialize()
	{

	}

}

@JvmRecord
data class RuneType(val Name: String)
{
	companion object
	{
		val Overworld = RuneType("Overworld")
		val Default = Overworld
	}
}