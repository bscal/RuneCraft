package me.bscal.runecraft

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

const val SMALL_RUNE_SIZE = 4 * 4
const val LARGE_RUNE_SIZE = 6 * 6

val RuneBoardCache = Object2ObjectOpenHashMap<UUID, RuneBoard>()

class RuneBoard(val Rune: Rune, val Size: Int)
{
	lateinit var Slots: Int2ObjectOpenHashMap<BoardSlot>
	private lateinit var Gui: ChestGui
	private lateinit var RunePanel: StaticPane
	private var Generator: BoardGenerator? = null

	constructor(rune: Rune, size: Int, slots: Int2ObjectOpenHashMap<BoardSlot>) : this(rune, size)
	{
		Slots = slots
	}

	fun Generate(player: Player) : Boolean
	{
		if (this::Slots.isInitialized) return false
		Generator = BoardRegistry.Registry[Rune.Type] ?: BoardRegistry.Default
		Slots = Int2ObjectOpenHashMap(Size, 1.0f)
		Generator?.Generate(player, this, Rune.Rarity)
		return true
	}

	fun Open(player: Player)
	{
		if (this::Gui.isInitialized) Gui.show(player)

		Generate(player)
		Gui = ChestGui(6, "${KColors.RED}RuneCraft")
		Gui.setOnClose {
			RuneBoardCache.remove(player.uniqueId, this)
		}

		CreateHeaderPanel()
		CreateSeparatorPanel()
		CreateRunePanel()

		RuneBoardCache[player.uniqueId] = this
		Gui.show(player)
	}


	fun CanBreak(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent): Boolean
	{
		return tool.AllowUse(itemStack)
	}

	fun OnBreak(x: Int, y: Int, slot: BoardSlot, itemStack: ItemStack, tool: RuneTool, event: InventoryClickEvent)
	{
		tool.Deincremeant(itemStack)
		Slots.remove(x or (y shl 16))
		RunePanel.removeItem(x, y)
		Gui.update()
	}

	fun OnBuild(event: InventoryClickEvent)
	{
		event.isCancelled = true
	}

	fun Update()
	{

	}

	fun Serialize()
	{

	}

	fun Deserialize()
	{

	}

	fun GetGuiTitle() : String = Gui.title

	private fun CreateRunePanel()
	{
		RunePanel = StaticPane(2, 0, 6, 6)
		for (slot in Slots.int2ObjectEntrySet())
		{
			val x = slot.intKey and 0xff
			val y = slot.intKey shr 16
			RunePanel.addItem(slot.value.Item.copy(), x, y)
		}
		Gui.addPane(RunePanel)
	}

	private fun CreateSeparatorPanel()
	{
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
	}

	private fun CreateHeaderPanel()
	{
		val header = StaticPane(0, 0, 1, 6)
		header.addItem(GuiItems.HelpItem, 0, 0)
		header.addItem(GuiItem(GuiItems.SeparatorIcon) { it.isCancelled = true }, 0, 1)
		header.addItem(GuiItems.StatsItem.copy(), 0, 2)
		header.addItem(CreateStabilityItem(), 0, 3)
		header.addItem(GuiItem(GuiItems.SeparatorIcon) { it.isCancelled = true }, 0, 4)
		header.addItem(CreateBuildItem(), 0, 5)
		Gui.addPane(header)
	}

	private fun CreateStabilityItem() : GuiItem
	{
		val buildSlot = GuiItems.StabilityItem.copy()
		buildSlot.item.amount = Rune.Stability
		return buildSlot
	}

	private fun CreateBuildItem() : GuiItem
	{
		val buildSlot = GuiItems.BuildItem.copy()
		buildSlot.setAction(::OnBuild)
		return buildSlot
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
	}) {
		it.isCancelled = true
	}

	val StatsItem = GuiItem(itemStack(Material.LECTERN) {
		meta {
			name = "${KColors.ALICEBLUE}Stats"
		}
	}) {
		it.isCancelled = true
	}

	val StabilityItem = GuiItem(itemStack(Material.TNT) {
		meta {
			name = "${KColors.DARKRED}Stability"
		}
	}) {
		it.isCancelled = true
	}

	val BuildItem = GuiItem(itemStack(Material.ANVIL) {
		meta {
			name = "${KColors.DARKGREEN}Build"
		}
	})
}