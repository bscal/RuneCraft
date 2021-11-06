package me.bscal.runecraft.gui.runeboard.slots

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import me.bscal.runecraft.gui.GuiItemWrapper
import me.bscal.runecraft.gui.runeboard.RuneBoard
import me.bscal.runecraft.gui.runeboard.RuneBoardCache
import me.bscal.runecraft.items.customitems.CustomItems
import me.bscal.runecraft.items.runeitems.BreakLevel
import me.bscal.runecraft.items.runeitems.RuneTool
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

abstract class BoardSlot(itemStack: ItemStack, val InstabilityLost: Int, val BreakLevel: BreakLevel) : IBoardSlot
{
	var GuiItemWrapper: GuiItemWrapper = GuiItemWrapper(itemStack);

	init
	{
		GuiItemWrapper.GuiItem.setAction(::OnClickAction)
	}

	override fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)
	{
	}

	override fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
	}

	override fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean = true

	override fun GetGuiItem(): GuiItem = GuiItemWrapper.GuiItem

	override fun GetInstabilityLost(): Int = InstabilityLost

	companion object
	{
		@JvmStatic val serialVersionID = 1L

		fun OnClickAction(it: InventoryClickEvent)
		{
			it.result = Event.Result.DENY
			if (it.clickedInventory != null && it.clickedInventory?.type == InventoryType.CHEST && it.isLeftClick)
			{
				val x = 5.coerceAtMost(0.coerceAtLeast(it.slot % 9 - 2))
				val y = it.slot / 9
				val key = RuneBoard.PackCoord(x, y)
				val board = RuneBoardCache[it.whoClicked.uniqueId]
				if (board != null && board.GetGuiTitle() == it.view.title)
				{
					val tool: ItemStack = it.cursor ?: ItemStack(Material.AIR)
					val customItem = CustomItems.GetByItemStack(tool)
					if (customItem is RuneTool)
					{
						val slot = board.Rune.BoardSlots.Slots[key]
						if (board.CanBreak(slot, customItem))
						{
							board.OnBreak(x, y, slot, tool, customItem)
						}
					}
				}
			}
		}
	}
}

class EmptySlot() : BoardSlot(ItemStack(Material.AIR), 0, BreakLevel.UNBREAKABLE)
{
	//	@Throws(IOException::class, ClassNotFoundException::class)
	//	private fun readObject(objIn: ObjectInputStream)
	//	{
	//		objIn.defaultReadObject()
	//		GuiItem = GuiItemWrapper(ItemStack(Material.AIR))
	//	}
}

class LineSlot(itemStack: ItemStack) : BoardSlot(itemStack, 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		GuiItemWrapper.GuiItem.item.editMeta {
			it.name = "${KColors.LIGHTGRAY}Rune Line"
		}
	}
}

class DefaultSlot(itemStack: ItemStack, stabilityLost: Int, breakLevel: BreakLevel) : BoardSlot(itemStack, stabilityLost, breakLevel)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		lore.add(Component.text("${KColors.RED}-1 Durability"))
		GuiItemWrapper.GuiItem.item.lore(lore)
	}
}

class BedrockSlot : BoardSlot(ItemStack(Material.BEDROCK), 0, BreakLevel.UNBREAKABLE)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}Unbreakable"))
		GuiItemWrapper.GuiItem.item.lore(lore)
	}
}

class DirtSlot(isGrass: Boolean) : BoardSlot(ItemStack(if (isGrass) Material.GRASS_BLOCK else Material.DIRT), 0, BreakLevel.ANY)
{
	init
	{
		val lore = ArrayList<Component>()
		lore.add(Component.text("${KColors.RED}-1 Stability"))
		GuiItemWrapper.GuiItem.item.lore(lore)
	}
}