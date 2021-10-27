package me.bscal.runecraft.stats

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class CustomStats(namespacedKey: NamespacedKey) : BaseStat(namespacedKey)
{
	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
	}

	override fun GetLocalName(instance: StatInstance): String
	{
		return ""
	}
}