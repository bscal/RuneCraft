package me.bscal.runecraft.stats

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.utils.RomanNumber
import me.bscal.runecraft.utils.setSpell
import net.axay.kspigot.data.NBTData
import net.axay.kspigot.data.NBTDataType
import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

object SpellRegistry
{
	val Registry = Object2ObjectOpenHashMap<NamespacedKey, SpellStat>()

	fun Register(spell: SpellStat): SpellStat
	{
		Registry[spell.Id] = spell
		return spell
	}
}

enum class SpellType
{
	PASSIVE, CASTED, LEFT_CLICKED, RIGHT_CLICKED, DAMAGE_DONE, DAMAGE_RECEIVED
}

interface SpellCastAction
{
	fun OnCast(caster: Entity, instance: StatInstance, targets: List<Entity>?)
}

interface SpellTarget
{
	fun GetTargets(caster: Entity, instance: StatInstance): List<Entity>?
}

interface SpellCondition
{
	fun CanCast(caster: Entity, instance: StatInstance): Boolean
}

open class SpellStat(namespacedKey: NamespacedKey, val Name: String, val MaxLevel: Int, val Type: SpellType, val Condition: SpellCondition?,
	val Target: SpellTarget, val CastAction: SpellCastAction) : BaseStat(namespacedKey)
{
	companion object
	{
		const val LEVEL_KEY = "spell_level"
	}

	fun Process(caster: Entity, instance: StatInstance)
	{
		val canCast: Boolean = Condition?.CanCast(caster, instance) ?: true
		if (canCast) CastAction.OnCast(caster, instance, Target.GetTargets(caster, instance))
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
		instance.AdditionalData[LEVEL_KEY, NBTDataType.INT] = level.coerceAtMost(MaxLevel)
	}

	fun GetLevel(instance: StatInstance): Int = instance.AdditionalData[LEVEL_KEY, NBTDataType.INT] ?: 0
}