package me.bscal.runecraft.gui.runeboard.slots

import me.bscal.runecraft.stats.StatRegistry
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack

abstract class RuneSlot(itemStack: ItemStack) : GemSlot(itemStack)

class HealthRuneSlot(itemStack: ItemStack) : RuneSlot(itemStack)
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_MAX_HEALTH, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}
}

class DamageRuneSlot(itemStack: ItemStack) : RuneSlot(itemStack)
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}
}