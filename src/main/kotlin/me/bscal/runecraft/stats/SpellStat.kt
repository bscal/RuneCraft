package me.bscal.runecraft.stats

import me.bscal.runecraft.utils.RomanNumber
import me.bscal.runecraft.utils.setSpell
import net.axay.kspigot.data.NBTData
import net.axay.kspigot.data.NBTDataType
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

enum class SpellType
{
	PASSIVE, CASTED, LEFT_CLICKED, RIGHT_CLICKED, DAMAGE_DONE, DAMAGE_RECEIVED
}

interface SpellCastAction
{
	fun OnCast(caster: Entity, target: Entity?, instance: StatInstance)
}

interface SpellCondition
{
	fun CanCast(caster: Entity, target: Entity?, instance: StatInstance): Boolean
}

class SpellStat(namespacedKey: NamespacedKey, val Name: String, val MaxLevel: Int, val Type: SpellType, val Condition: SpellCondition?,
	val CastAction: SpellCastAction) : BaseStat(namespacedKey)
{
	companion object
	{
		const val LEVEL_KEY = "spell_level"
	}

	fun Process(caster: Entity, target: Entity?, instance: StatInstance)
	{
		val canCast: Boolean = Condition?.CanCast(caster, target, instance) ?: true
		if (canCast) CastAction.OnCast(caster, target, instance)
	}

	override fun ApplyToItemStack(instance: StatInstance, itemStack: ItemStack)
	{
		itemStack.itemMeta.setSpell(instance)
	}

	override fun GetLocalName(instance: StatInstance): String
	{
		return "$Name ${RomanNumber.toRoman(instance.Value.toInt())}"
	}

	override fun CombineInstance(instance: StatInstance, other: StatInstance): StatInstance
	{
		if (!IsSame(instance, other)) return instance
		instance.Value = floor((instance.Value + other.Value).coerceAtMost(MaxLevel.toDouble()))
		return instance
	}

	fun NewStatInstance(level: Int): StatInstance
	{
		val data = NBTData()
		data[LEVEL_KEY, NBTDataType.INT] = level
		return super.NewStatInstance(level.toDouble(), AttributeModifier.Operation.ADD_NUMBER, data)
	}

	inline fun IncrementLevel(instance: StatInstance) = SetLevel(instance, GetLevel(instance) + 1)

	fun SetLevel(instance: StatInstance, level: Int)
	{
		instance.additionalData[LEVEL_KEY, NBTDataType.INT] = level.coerceAtMost(MaxLevel)
	}

	fun GetLevel(instance: StatInstance): Int = instance.additionalData[LEVEL_KEY, NBTDataType.INT] ?: 0
}