package me.bscal.runecraft.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import me.bscal.runecraft.gui.runeboard.slots.BoardSlot
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class GuiItemWrapper() : Externalizable
{
	@Transient lateinit var GuiItem: GuiItem

	constructor(itemStack: ItemStack) : this()
	{
		GuiItem = GuiItem(itemStack)
	}

	override fun writeExternal(out: ObjectOutput?)
	{
		if (GuiItem.item.type.isAir) return
		out?.write(GuiItem.item.serializeAsBytes())
	}

	override fun readExternal(input: ObjectInput?)
	{
		try
		{
			val bytes = ByteArray(input?.available() ?: 16)
			input?.read(bytes)
			GuiItem = if (bytes.isEmpty())
			{
				GuiItem(ItemStack(Material.AIR))
			}
			else
			{
				val itemStack = ItemStack.deserializeBytes(bytes)
				GuiItem(itemStack, BoardSlot::OnClickAction)
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}
}