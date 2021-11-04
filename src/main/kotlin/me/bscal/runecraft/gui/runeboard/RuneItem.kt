package me.bscal.runecraft.gui.runeboard

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.bscal.runecraft.gui.runeboard.slots.BoardSlot
import me.bscal.runecraft.gui.runeboard.slots.DamageRuneSlot
import me.bscal.runecraft.gui.runeboard.slots.HealthRuneSlot
import me.bscal.runecraft.items.customitems.CustomId
import me.bscal.runecraft.items.customitems.CustomItem
import me.bscal.runecraft.items.customitems.CustomItems
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object RuneItems
{
	val RuneItemsSet = ObjectOpenHashSet<CustomId>()

	lateinit var HealthRune: RuneItem; private set
	lateinit var DamageRune: RuneItem; private set

	fun Register()
	{
		val hItem = itemStack(Material.END_STONE_BRICKS) {
			this.meta {
				name = "${KColors.DARKGREEN}Health Rune"
				setCustomModelData(12000)
			}
		}
		HealthRune = Register("health_rune", RuneItem(hItem, HealthRuneSlot(hItem)))

		val dRune = itemStack(Material.END_STONE_BRICKS) {
			this.meta {
				name = "${KColors.DARKRED}Damage Rune"
				setCustomModelData(12001)
			}
		}
		DamageRune = Register("damage_rune", RuneItem(dRune, DamageRuneSlot(dRune)))
	}

	fun Register(internalName: String, runeItem: RuneItem) : RuneItem
	{
		val id = CustomId.FromItemStack(runeItem.DefaultStack)
		RuneItemsSet.add(id)
		CustomItems.Register(internalName, id, runeItem)
		return runeItem
	}

}

class RuneItem(defaultStack: ItemStack, val BoardSlot: BoardSlot) : CustomItem(defaultStack, true)
{
}
