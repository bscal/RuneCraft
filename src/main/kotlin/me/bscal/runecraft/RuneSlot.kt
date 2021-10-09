package me.bscal.runecraft

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.CustomItemIdentifier
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

interface IBoardSlot
{
	fun NewItem(player: Player, runeBoard: RuneBoard) : GuiItem

	fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)

	//fun OnClick(event: InventoryClickEvent, runeBoard: RuneBoard)
}

open class BoardSlot(val Item: GuiItem) : IBoardSlot
{

	override fun NewItem(player: Player, runeBoard: RuneBoard) : GuiItem
	{
		val item = Item.copy()
		Update(item, player, runeBoard)
		return item
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{

	}

}

class DefaultSlot(val material: Material) : BoardSlot(GuiItem(ItemStack(material)))
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}- stability"))
		lore.add(Component.text("${KColors.RED}- durability"))
		Item.item.lore(lore)
	}
}

open class GemSlot(val material: Material) : BoardSlot(GuiItem(ItemStack(material)))
{
}

class DiamondSlot : GemSlot(Material.DIAMOND)
{
	init
	{
		Item.item.meta {
			name = "${KColors.LIGHTSKYBLUE}Diamond Gem"
			addLore {
				Component.text("${KColors.GREEN}+ 5% durability")
				Component.text("${KColors.GREEN}Bonus: + .25 attack damage")
			}
		}

		val id = CustomItemIdentifier(1, Material.WOODEN_HOE)

		Item.setAction {
		}
	}
}
