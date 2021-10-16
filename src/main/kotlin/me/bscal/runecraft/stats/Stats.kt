package me.bscal.runecraft.stats

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.md_5.bungee.api.ChatColor
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
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
		return "${Color}$sign ${dec.format(Value)}$operation $Name"
	}

	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Stat

		return Name != other.Name || Operation != other.Operation
	}

	override fun hashCode(): Int
	{
		var result = Name.hashCode()
		result = 31 * result + Operation.hashCode()
		return result
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

	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		if (!super.equals(other)) return false

		other as VanillaStat

		return Attribute != other.Attribute || Modifer.slot != other.Modifer.slot
	}

	override fun hashCode(): Int
	{
		var result = super.hashCode()
		result = 31 * result + Attribute.hashCode()
		result = 31 * result + Modifer.slot.hashCode()
		return result
	}

}

data class RuneStats(val StatsSet: ObjectOpenHashSet<Stat> = ObjectOpenHashSet())

fun ObjectOpenHashSet<Stat>.addStat(stat: Stat)
{
	if (this.contains(stat)) this.get(stat).Combine(stat)
	else this.add(stat)
}

fun ObjectOpenHashSet<Stat>.addAllStat(stat: Stat)
{
	if (this.contains(stat)) this.get(stat).Combine(stat)
	else this.add(stat)
}