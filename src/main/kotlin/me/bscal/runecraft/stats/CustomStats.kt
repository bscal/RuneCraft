package me.bscal.runecraft.stats

import net.md_5.bungee.api.ChatColor
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack

class CustomStats(name: String, value: Double, operation: AttributeModifier.Operation, color: ChatColor) :
	Stat(name, value, operation, color)
{

	override fun ApplyToItemStack(itemStack: ItemStack): Boolean
	{
		TODO("Not yet implemented")
	}

	override fun Combine(other: Stat)
	{
		TODO("Not yet implemented")
	}
}