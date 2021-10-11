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

class RuneBoard(val Rune: Rune, val Size: Int)
{
	private lateinit var Slots: ObjectArrayList<BoardSlot>
	private lateinit var Gui: ChestGui
	private var Generator: BoardGenerator? = null

	constructor(rune: Rune, size: Int, slots: ObjectArrayList<BoardSlot>) : this(rune, size)
	{
		Slots = slots
	}

	fun Generate(player: Player) : Boolean
	{
		if (this::Slots.isInitialized) return false
		Generator = BoardRegistry.Registry[Rune.Type] ?: BoardRegistry.Default
		Slots = ObjectArrayList(Generator?.Generate(player, this, Rune.Rarity))
		return true
	}

	fun Open(player: Player)
	{
		if (this::Gui.isInitialized) Gui.show(player)

		Generate(player)
		Gui = ChestGui(6, "${KColors.RED}RuneCraft")

		val leftSeparator = StaticPane(1, 0, 1, 6)
		leftSeparator.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		Gui.addPane(leftSeparator)

		val rightSeparator = StaticPane(8, 0, 1, 6)
		rightSeparator.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		Gui.addPane(rightSeparator)

		val header = StaticPane(0, 0, 1, 6)
		header.fillWith(GuiItems.SeparatorIcon) {
			it.isCancelled = true
		}
		header.addItem(GuiItems.HelpItem, 0, 0)
		header.addItem(GuiItems.StatsItem, 0, 2)
		header.addItem(GuiItems.StabilityItem, 0, 3)
		header.addItem(GuiItems.BuildItem, 0, 5)
		Gui.addPane(header)

		val board = StaticPane(2, 0, 6, 6)
		Gui.addPane(board)

		var x = 0
		var y = 0
		for (slot in Slots)
		{
			board.addItem(slot.NewItem(player, this), x, y)
			if (x++ > 4)
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
	}) {

	}

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