package me.bscal.runecraft.gui.runeboard.slots

import me.bscal.runecraft.items.runeitems.BreakLevel
import me.bscal.runecraft.stats.StatInstance
import me.bscal.runecraft.stats.StatRegistry
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack

abstract class GemSlot(itemStack: ItemStack) : BoardSlot(itemStack, 0, BreakLevel.UNBREAKABLE)
{
	val Stats = ArrayList<StatInstance>()

	init
	{
		GuiItemWrapper.GuiItem.item.meta {
			this.addLore {
				Stats.forEach { +it.GetStat().GetLoreString(it) } // TODO better way? thinking removing stat if deserialized
			}
		}
	}

	constructor() : this(ItemStack(Material.AIR))
}

class DiamondSlot : GemSlot(itemStack(Material.DIAMOND_BLOCK) {
	meta {
		name = "${KColors.LIGHTSKYBLUE}Diamond Gem"
	}
})
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}
}

class EmeraldSlot : GemSlot(itemStack(Material.EMERALD_BLOCK) {
	meta {
		name = "${KColors.MEDIUMSPRINGGREEN}Emerald Gem"
	}
})
{
	init
	{
		Stats.add(StatRegistry.VANILLA_STAT.NewStatInstance(Attribute.GENERIC_MAX_HEALTH, 1.0, AttributeModifier.Operation.ADD_NUMBER))
	}
}

class RedstoneSlot : GemSlot(itemStack(Material.REDSTONE_BLOCK) {
	meta {
		name = "${KColors.DARKRED}Ruby Gem"
	}
})
{}

class LapisSlot : GemSlot(itemStack(Material.LAPIS_BLOCK) {
	meta {
		name = "${KColors.DARKBLUE}Sapphire Gem"
	}
})
{}

class AmethystSlot : GemSlot(itemStack(Material.AMETHYST_BLOCK) {
	meta {
		name = "${KColors.DARKPURPLE}Amethyst Gem"
	}
})
{}