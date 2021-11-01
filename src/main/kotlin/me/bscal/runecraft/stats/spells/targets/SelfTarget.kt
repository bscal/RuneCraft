package me.bscal.runecraft.stats.spells.targets

import me.bscal.runecraft.stats.SpellTarget
import me.bscal.runecraft.stats.StatInstance
import org.bukkit.entity.Entity

class SelfTarget : SpellTarget
{
	override fun GetTargets(caster: Entity, instance: StatInstance): List<Entity>?
	{
		return listOf(caster)
	}
}