package me.bscal.runecraft.stats

import io.papermc.paper.event.entity.EntityDamageItemEvent
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.md_5.bungee.api.ChatColor
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat

abstract class Stat(val Name: String, var Value: Double, val Operation: AttributeModifier.Operation, val Color: ChatColor)
{

	abstract fun ApplyToItemStack(itemStack: ItemStack): Boolean

	abstract fun Combine(other: Stat)

	fun GetLoreString(): String
	{
		val dec = DecimalFormat("#.##")
		val sign = if (Value < 0) "-" else "+"
		val operation = if (Operation != AttributeModifier.Operation.ADD_NUMBER) "%" else ""
		return "${Color}$sign $dec$operation $Name"
	}

}

class VanillaStat(name: String, color: ChatColor, val Attribute: Attribute, var Modifer: AttributeModifier) :
	Stat(name, Modifer.amount, Modifer.operation, color)
{
	override fun ApplyToItemStack(itemStack: ItemStack): Boolean
	{
		val meta = itemStack.itemMeta
		val success: Boolean = meta.addAttributeModifier(Attribute, Modifer)
		itemStack.itemMeta = meta
		return success
	}

	override fun Combine(other: Stat)
	{
		if (other is VanillaStat)
		{
			Modifer = AttributeModifier(other.Modifer.name, Modifer.amount + other.Modifer.amount, other.Operation)
			Value = Modifer.amount
		}

	}
}

class RuneStats(val StatMap: Object2ObjectOpenHashMap<String, Stat> = Object2ObjectOpenHashMap())
{
	fun Add(id: String, stat: Stat)
	{
		if (StatMap.containsKey(id)) StatMap[id]?.Combine(stat)
		else StatMap[id] = stat
	}
}