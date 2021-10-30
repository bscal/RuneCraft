package me.bscal.runecraft.stats

import me.bscal.runecraft.utils.RomanNumber
import net.axay.kspigot.data.NBTData
import net.axay.kspigot.data.NBTDataType
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

enum class SpellType
{
	PASSIVE, CASTED, LEFT_CLICKED, RIGHT_CLICKED, DAMAGE_DONE, DAMAGE_RECIEVED
}

interface CastAction
{
	fun OnCast(caster: Entity, target: Entity, instance: SpellStat)
}

class SpellMechanic(val Type: SpellType, val MaxLevel: Int, val Cooldown: Float, action: CastAction)
{

}

class SpellStat(namespacedKey: NamespacedKey) : BaseStat(namespacedKey)
{
	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
	}

	override fun GetLocalName(instance: StatInstance): String
	{
		return "${instance.additionalData["spell_name", NBTDataType.STRING]} ${RomanNumber.toRoman(instance.Value.toInt())}"
	}

	fun NewStatInstance(spellName: String, level: Int): StatInstance
	{
		return super.NewStatInstance(value, operation, additionalData)
	}

	override fun NewStatInstance(value: Double, operation: AttributeModifier.Operation, additionalData: NBTData?): StatInstance
	{
		return super.NewStatInstance(value, operation, additionalData)
	}

	override fun GetLoreString(instance: StatInstance): String
	{
		return super.GetLoreString(instance)
	}
}

private fun IntToRomanNumeral(value: Int): String
{
	return when (value)
	{
		1 -> return "I"
		2 -> return "II"
		3 -> return "III"
		4 -> return "IV"
		5 -> return "V"
		6 -> return "VI"
		7 -> return "VII"
		8 -> return "VIII"
		9 -> return "IX"
		10 -> return "X"
		else -> return value.toString()
	}
}