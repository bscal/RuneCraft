package me.bscal.runecraft.stats

import me.bscal.runecraft.RuneCraft
import net.axay.kspigot.data.NBTData
import net.axay.kspigot.data.NBTDataType
import net.axay.kspigot.data.nbtData
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class CustomStat(namespacedKey: NamespacedKey) : BaseStat(namespacedKey)
{
	companion object
	{
		const val NAME_KEY = "stat_name"
		val NAMESPACE =
	}

	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
		val name = GetStatName(instance)
		itemStack.nbtData[name, NBTDataType.COMPOUND] = instance.additionalData

		val key = NamespacedKey(RuneCraft.INSTANCE, name)
		val meta = itemStack.itemMeta
		val data = meta.persistentDataContainer.get(key, PersistentDataType.STRING)
		if (data == null) meta.persistentDataContainer.set(key, PersistentDataType.STRING, name)
		instance.additionalData


	}

	override fun GetLocalName(instance: StatInstance): String
	{
		return "${GetStatName(instance)} ${instance.Value.toInt()}"
	}

	fun NewStatInstance(statName: String, value: Double, operation: AttributeModifier.Operation): StatInstance
	{
		val nbt = NBTData()
		nbt[NAME_KEY, NBTDataType.STRING] = statName
		return super.NewStatInstance(value, operation, nbt)
	}

	override fun GetLoreString(instance: StatInstance): String
	{
		return super.GetLoreString(instance)
	}

	override fun IsSame(instance: StatInstance, other: StatInstance): Boolean
	{
		return super.IsSame(instance, other) && GetStatName(instance) == GetStatName(other)
	}

	fun GetStatName(instance: StatInstance) : String
	{
		instance.additionalData[NAME_KEY, NBTDataType.STRING]
	}

	fun GetItemDataContainer(itemStack: ItemStack) : String
	{

	}
}