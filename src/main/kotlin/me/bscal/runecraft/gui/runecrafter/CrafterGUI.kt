package me.bscal.runecraft.gui.runecrafter

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import me.bscal.runecraft.Rune
import me.bscal.runecraft.RuneCraft
import me.bscal.runecraft.gui.GuiItems
import me.bscal.runecraft.items.runeitems.RuneCraftItems
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.itemStack
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.logging.Level

class CrafterGUI(val ItemStack: ItemStack, val Size: Int)
{
	companion object
	{
		private val RUNE_KEY = NamespacedKey(RuneCraft.INSTANCE, "_RUNES")

		fun SlotToIndex(slot: Int): Int
		{
			return when (slot)
			{
				13 -> 0
				22 -> 1
				31 -> 2
				40 -> 3
				else -> -1
			}
		}
	}

	private val m_Gui: ChestGui = ChestGui(6, "${KColors.DARKSLATEGRAY}Item's Runes")
	lateinit var m_Runes: ArrayList<Rune>; private set

	fun Open(player: Player)
	{
		Deserialize()
		if (!this::m_Runes.isInitialized || m_Runes.isEmpty()) m_Runes = ArrayList(4)

		RuneCraft.LogDebug(Level.INFO, "$m_Runes")

		val background = StaticPane(9, 6)
		background.fillWith(GuiItems.SeparatorIcon)
		background.addItem(CreateHelpIcon(), 0, 0)

		for (i in 0..Size.coerceAtMost(4))
		{
			if (i < m_Runes.size) background.addItem(LoadRuneIcon(m_Runes[i]), 4, i + 1)
			else background.removeItem(4, i + 1)
		}

		background.setOnClick {
			it.isCancelled = true
			if (it.currentItem == null && it.cursor != null)
			{
				val cursor: ItemStack = it.cursor!!
				AddRuneToStack(player, cursor.clone(), it.slot)
				if (cursor.amount < 2) it.cursor = null
				else cursor.amount = cursor.amount - 1
				Serialize()
			}
		}

		m_Gui.addPane(background)

		m_Gui.setOnClose {
			Serialize()
		}
		m_Gui.show(player)
	}

	fun Serialize()
	{
		val meta = ItemStack.itemMeta
		meta.persistentDataContainer.set(RUNE_KEY, PersistentDataType.BYTE_ARRAY, ProtoBuf.encodeToByteArray(m_Runes))
		ItemStack.itemMeta = meta
	}

	fun Deserialize(): Boolean
	{
		if (ItemStack.hasItemMeta())
		{
			val bytes = ItemStack.itemMeta.persistentDataContainer.get(RUNE_KEY, PersistentDataType.BYTE_ARRAY)
			if (bytes == null || bytes.isEmpty()) return false
			m_Runes = ProtoBuf.decodeFromByteArray(bytes)
			return true
		}
		return false
	}

	private fun CreateHelpIcon(): GuiItem
	{
		return GuiItem(itemStack(Material.BOOK) {

		})
	}

	private fun LoadRuneIcon(rune: Rune): GuiItem
	{
		return GuiItem(RuneCraftItems.CARVED_RUNE.NewStack(rune))
	}

	private fun AddRuneToStack(player: Player, itemStack: ItemStack, slot: Int)
	{
		if (RuneCraftItems.CARVED_RUNE.Is(itemStack))
		{
			val x: Int = slot % 9
			val y: Int = slot / 9
			(m_Gui.panes[0] as StaticPane).addItem(GuiItem(itemStack), x, y)
			m_Gui.update()

			val rune = Rune.Deserialize(itemStack)
			if (rune != null)
			{
				m_Runes.add(rune)
				rune.AddRuneToItem(player, ItemStack)
			}
		}
	}
}