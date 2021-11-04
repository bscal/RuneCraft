package me.bscal.runecraft.gui.runeboard.slots

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import me.bscal.runecraft.gui.runeboard.RuneBoard
import me.bscal.runecraft.items.runeitems.RuneTool
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.io.Serializable

interface IBoardSlot : Serializable
{
	fun GetGuiItem(): GuiItem

	fun Update(item: GuiItem, player: Player, runeBoard: RuneBoard)

	fun OnBreak(x: Int, y: Int, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)

	fun CanPlace(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean

	fun GetInstabilityLost(): Int
}