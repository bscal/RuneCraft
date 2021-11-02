package me.bscal.runecraft.stats.spells.targets

import me.bscal.runecraft.stats.SpellTarget
import me.bscal.runecraft.stats.StatInstance
import net.axay.kspigot.data.NBTDataType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

class RaycastTarget : SpellTarget
{
	override fun GetTargets(caster: Entity, instance: StatInstance): List<Entity>?
	{
		if (caster is LivingEntity)
		{
			val distance = instance.AdditionalData["maxDistance", NBTDataType.INT] ?: 0
			if (distance == 0) return null
			val ignoreBlocks: Byte = instance.AdditionalData["ignoreBlock", NBTDataType.BYTE] ?: 0
			caster.getTargetEntity(distance, ignoreBlocks > 0)
		}
		return null
	}
}