package me.bscal.runecraft.stats

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.bscal.runecraft.RuneCraft
import me.bscal.runecraft.stats.spells.actions.DamageAction
import me.bscal.runecraft.stats.spells.conditions.ChanceCondition
import me.bscal.runecraft.stats.spells.targets.AreaTarget
import org.bukkit.NamespacedKey

/**
 * Registry for all base RuneCraft stats and 3rd party instances.
 */
object StatRegistry
{
	val Registry = Object2ObjectOpenHashMap<NamespacedKey, BaseStat>()

	val VANILLA_STAT: VanillaStat = Register(VanillaStat(NamespacedKey(RuneCraft.INSTANCE, "vanilla_stat"))) as VanillaStat
	val POTION_STAT: PotionStat = Register(PotionStat(NamespacedKey(RuneCraft.INSTANCE, "potion_stat"))) as PotionStat
	val EARTH_SHOCK_STAT: SpellStat = Register(
		SpellStat(NamespacedKey(RuneCraft.INSTANCE, "earthshock_stat"), "Earth Shock", 3, SpellType.DAMAGE_DONE, ChanceCondition(),
			AreaTarget(), DamageAction())) as SpellStat

	fun Register(stat: BaseStat): BaseStat
	{
		Registry[stat.Id] = stat
		return stat
	}
}