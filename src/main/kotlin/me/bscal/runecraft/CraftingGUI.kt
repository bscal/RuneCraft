package me.bscal.runecraft

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.entity.Player

const val SMALL_RUNE_SIZE = 4 * 4
const val LARGE_RUNE_SIZE = 6 * 6

class RuneBoard(val Type: RuneType, val Size: Int)
{
	val Slots: ObjectArrayList<BoardSlot> = ObjectArrayList(LARGE_RUNE_SIZE)

	private lateinit var Generator: BoardGenerator
	private lateinit var Gui: ChestGui

	fun Generate(player: Player)
	{
		Generator = BoardRegistry.Registry[Type] ?: BoardRegistry.Default
		Generator.Generate(player, this)
	}

	fun Open(player: Player)
	{
		//if (Gui.viewers.contains(player)) return

		Gui = ChestGui(6, "${KColors.RED}RuneCraft")

		val leftSeparator = StaticPane(1, 0, 1, 6)
		leftSeparator.fillWith(GuiItems.SeparatorIcon)
		val rightSeparator = StaticPane(9, 0, 1, 6)
		rightSeparator.fillWith(GuiItems.SeparatorIcon)

		val header = StaticPane(0, 0, 1, 6)
		rightSeparator.fillWith(GuiItems.SeparatorIcon)
		header.addItem(GuiItems.HelpItem, 0, 0)
		header.addItem(GuiItems.StatsItem, 0, 2)
		header.addItem(GuiItems.StabilityItem, 0, 3)
		header.addItem(GuiItems.BuildItem, 0, 5)

		val board = StaticPane(2, 0, 6, 6)

		var x = 0
		var y = 0
		for (slot in Slots)
		{
			board.addItem(slot.NewItem(player, this), x, y)
			if (x++ > 8)
			{
				x = 0
				y++
			}
		}

		Gui.show(player)
	}

	fun Update(player: Player)
	{

	}

	fun Serialize()
	{

	}

	fun Deserialize()
	{

	}
}

object GuiItems
{
	private const val BLANK = ""

	val SeparatorIcon = itemStack(Material.BLACK_STAINED_GLASS_PANE) {
		meta {
			name = BLANK
		}
	}

	val HelpItem = GuiItem(itemStack(Material.BOOK) {
		meta {
			name = "${KColors.GREENYELLOW}Help"
			addLore {

			}
		}
	})

	val StatsItem = GuiItem(itemStack(Material.LECTERN) {
		meta {
			name = "${KColors.ALICEBLUE}Stats"
		}
	})

	val StabilityItem = GuiItem(itemStack(Material.TNT) {
		meta {
			name = "${KColors.DARKRED}Stability"
		}
	})

	val BuildItem = GuiItem(itemStack(Material.ANVIL) {
		meta {
			name = "${KColors.DARKGREEN}Build"
		}
	})

}