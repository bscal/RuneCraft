package me.bscal.runecraft.stats

import me.bscal.runecraft.utils.setCustomStat
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class CustomStat(val Name: String, namespacedKey: NamespacedKey) : BaseStat(namespacedKey)
{
	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
		itemStack.itemMeta.setCustomStat(instance)
	}

	override fun GetLocalName(instance: StatInstance): String = Name
}