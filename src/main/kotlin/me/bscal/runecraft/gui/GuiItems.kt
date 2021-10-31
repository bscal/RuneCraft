package me.bscal.runecraft.gui

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material

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
			name = "${KColors.DARKRED}Instability"
			addLore {
				+"${KColors.RED}Instability is number that shows how"
				+"${KColors.RED}close the rune is to being destroyed."
				+"${KColors.RED}Most moves will increase instability."
				+"${KColors.RED}Instability above 9 will have increased"
				+"${KColors.RED}odds of being destroyed."
				+" "
				+"${KColors.RED}0-9 = 0%"
				+"${KColors.RED}10  = 10%"
				+"${KColors.RED}11  = 25%"
				+"${KColors.RED}12  = 60%"
			}
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