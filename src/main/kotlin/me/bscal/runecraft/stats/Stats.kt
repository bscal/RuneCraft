package me.bscal.runecraft.stats

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import me.bscal.runecraft.RuneCraft
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.data.NBTData
import net.axay.kspigot.data.NBTDataType
import net.md_5.bungee.api.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat
import java.util.*

object StatRegistry
{
	val Registry = Object2ObjectOpenHashMap<NamespacedKey, BaseStat>()

	val VANILLA_STAT: VanillaStat = Register(VanillaStat(NamespacedKey(RuneCraft.INSTANCE, "vanilla_stat"))) as VanillaStat
	val POTION_STAT: PotionStat = Register(PotionStat(NamespacedKey(RuneCraft.INSTANCE, "potion_stat"))) as PotionStat

	fun Register(stat: BaseStat): BaseStat
	{
		Registry[stat.Id] = stat
		return stat
	}
}

abstract class BaseStat(val Id: NamespacedKey)
{
	var PositiveColor: ChatColor = KColors.GREEN
	var NegativeColor: ChatColor = KColors.RED
	var PositiveIsGood: Boolean = true

	abstract fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)

	abstract fun GetLocalName(instance: StatInstance): String

	open fun IsSame(instance: StatInstance, other: StatInstance): Boolean
	{
		return instance.Id == other.Id && instance.Operation == other.Operation
	}

	open fun CombineInstance(instance: StatInstance, other: StatInstance): StatInstance
	{
		if (!IsSame(instance, other)) return instance
		instance.Value += other.Value
		return instance
	}

	open fun NewStatInstance(value: Double, operation: AttributeModifier.Operation, additionalData: NBTData?): StatInstance
	{
		return StatInstance(Id, value, operation, additionalData ?: NBTData())
	}

	open fun GetLoreString(instance: StatInstance): String
	{
		val sign: String
		val color: ChatColor
		if (instance.Value < 0)
		{
			if (PositiveIsGood)
			{
				sign = "-"
				color = NegativeColor
			}
			else
			{
				sign = "+"
				color = PositiveColor
			}
		}
		else
		{
			if (PositiveIsGood)
			{
				sign = "+"
				color = PositiveColor
			}
			else
			{
				sign = "-"
				color = NegativeColor
			}
		}
		val operator: String = if (instance.Operation != AttributeModifier.Operation.ADD_NUMBER) "%" else ""
		val dec = DecimalFormat("#.##")
		return "$color$sign ${dec.format(instance.Value)}$operator ${GetLocalName(instance)}"
	}

	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as BaseStat
		return (Id == other.Id)
	}

	override fun hashCode(): Int
	{
		return Id.hashCode()
	}

}

class VanillaStat(id: NamespacedKey) : BaseStat(id)
{
	companion object
	{
		val ATTRIBUTE_KEY = "attribute"
		val MAX_KEY = "max"
	}

	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
		val meta = itemStack.itemMeta
		val attr = Attribute.valueOf(instance.additionalData[ATTRIBUTE_KEY, NBTDataType.STRING] ?: return)
		meta.addAttributeModifier(attr, AttributeModifier(Id.key, instance.Value, instance.Operation))
		itemStack.itemMeta = meta
	}

	override fun GetLocalName(instance: StatInstance): String
	{
		var attribute = instance.additionalData[ATTRIBUTE_KEY, NBTDataType.STRING] ?: return "NULL"
		attribute = attribute.replaceFirst("GENERIC_", "")
		val split = attribute.split("_")
		val sb = StringBuilder()
		for (str in split)
		{
			sb.append(str[0])
			sb.append(str.substring(1).lowercase(Locale.getDefault()))
		}
		return sb.toString()
	}

	override fun CombineInstance(instance: StatInstance, other: StatInstance): StatInstance
	{
		if (!IsSame(instance, other)) return instance
		instance.Value += other.Value
		return instance
	}

	fun NewStatInstance(attribute: Attribute, value: Double, operation: AttributeModifier.Operation): StatInstance
	{
		val nbt = NBTData()
		nbt[ATTRIBUTE_KEY, NBTDataType.STRING] = attribute.name
		return super.NewStatInstance(value, operation, nbt)
	}
}