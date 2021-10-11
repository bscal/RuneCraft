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
import org.bukkit.inventory.ItemStack

interface IBoardSlot
{
	fun NewItem(player: Player, runeBoard: RuneBoard): GuiItem

	fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)

	fun OnBreak(player: Player, tool: RuneTool, runeBoard: RuneBoard)

	fun CanPlace(player: Player, runeBoard: RuneBoard): Boolean
}

abstract class BoardSlot(val Item: GuiItem) : IBoardSlot
{

	override fun NewItem(player: Player, runeBoard: RuneBoard): GuiItem
	{
		val item = Item.copy()
		Update(item, player, runeBoard)
		return item
	}
}

class DefaultSlot(material: Material) : BoardSlot(GuiItem(ItemStack(material)))
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		lore.add(Component.text("${KColors.RED}-1 Durability"))
		Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(player: Player, tool: RuneTool, runeBoard: RuneBoard)
	{
	}

	override fun CanPlace(player: Player, runeBoard: RuneBoard): Boolean = true
}

abstract class GemSlot(material: Material) : BoardSlot(GuiItem(ItemStack(material)))
{}

class DiamondSlot : GemSlot(Material.DIAMOND)
{
	init
	{
		Item.item.meta {
			name = "${KColors.LIGHTSKYBLUE}Diamond Gem"
			addLore {
				Component.text("${KColors.GREEN}+ 5% Durability")
				Component.text("${KColors.GREEN}+ .25 Attack Damage")
			}
		}

		val id = CustomItemIdentifier(1, Material.WOODEN_HOE)

		Item.setAction { }
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
		TODO("Not yet implemented")
	}

	override fun OnBreak(player: Player, tool: RuneTool, runeBoard: RuneBoard)
	{
		TODO("Not yet implemented")
	}

	override fun CanPlace(player: Player, runeBoard: RuneBoard): Boolean
	{
		TODO("Not yet implemented")
	}
}
