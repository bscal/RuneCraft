package me.bscal.runecraft

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import me.bscal.runecraft.custom_items.CustomItems
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

interface IBoardSlot
{
	fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)

	fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)

	fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean

	fun GetInstabilityLost(): Int
}

abstract class BoardSlot(val Item: GuiItem, val InstabilityLost: Int, val BreakLevel: BreakLevel) : IBoardSlot
{
	init
	{
		Item.setAction(::HandleClick)
	}

	private fun HandleClick(it: InventoryClickEvent)
	{
		it.result = Event.Result.DENY
		if (it.clickedInventory != null && it.clickedInventory?.type == InventoryType.CHEST && it.isLeftClick)
		{
			val x = 5.coerceAtMost(0.coerceAtLeast(it.slot % 9 - 2))
			val y = it.slot / 9
			val key = RuneBoard.PackCoord(x, y)

			val board = RuneBoardCache[it.whoClicked.uniqueId] ?: return
			if (!board.GetGuiTitle().equals(it.view.title)) return

			val tool: ItemStack = it.cursor ?: ItemStack(Material.AIR)
			val customItem = CustomItems.GetByItemstack(tool) ?: return
			if (customItem is RuneTool)
			{
				val slot = board.Slots[key]
				if (board.CanBreak(x, y, slot, tool, customItem, it))
				{
					slot.OnBreak(x, y, tool, customItem, it)
					board.OnBreak(x, y, slot, tool, customItem, it)
				}
			}
		}
	}

	override fun GetInstabilityLost(): Int = InstabilityLost
}

class LineSlot(material: Material) : BoardSlot(GuiItem(itemStack(material) {
	meta {
		name = "${KColors.LIGHTGRAY}Rune Line"
	}
}), 0, BreakLevel.UNBREAKABLE)
{
	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean =
		false

}

class DefaultSlot(material: Material, stabilityLost: Int, breakLevel: BreakLevel) :
	BoardSlot(GuiItem(ItemStack(material)), stabilityLost, breakLevel)
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

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}

class BedrockSlot : BoardSlot(GuiItem(ItemStack(Material.BEDROCK)), 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}Unbreakable"))
		Item.item.lore(lore)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean =
		false
}

class DirtSlot(val IsGrass: Boolean) :
	BoardSlot(GuiItem(ItemStack(if (IsGrass) Material.GRASS_BLOCK else Material.DIRT)), 0, BreakLevel.ANY)
{
	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true

}

abstract class GemSlot(material: Material) : BoardSlot(GuiItem(ItemStack(material)), 0, BreakLevel.UNBREAKABLE)
{

	abstract fun GetLoreStats() : String

}

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
		Item.setAction { }
	}

	override fun GetLoreStats(): String
	{
		return  ""
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true
}
