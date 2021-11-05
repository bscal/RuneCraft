package me.bscal.runecraft

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.bscal.runecraft.gui.runeboard.RuneItems
import me.bscal.runecraft.gui.runecrafter.CrafterGUI
import me.bscal.runecraft.items.customitems.CustomItemListener
import me.bscal.runecraft.items.runeitems.RuneCraftItems
import me.bscal.runecraft.stats.StatInstance
import me.bscal.runecraft.stats.StatRegistry
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.getHandItem
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.extensions.pluginManager
import net.axay.kspigot.main.KSpigot
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import java.util.logging.Level

class RuneCraft : KSpigot()
{
	companion object
	{
		lateinit var INSTANCE: RuneCraft; private set
		var DEBUG_MODE: DebugMode = DebugMode.DEBUG; private set

		internal fun LogDebug(level: Level, msg: String)
		{
			if (DEBUG_MODE == DebugMode.DEBUG) INSTANCE.logger.log(level, msg)
		}

		internal fun Log(level: Level, msg: String)
		{
			if (DEBUG_MODE == DebugMode.DEBUG || DEBUG_MODE == DebugMode.RELEASE) INSTANCE.logger.log(level, msg)
		}
	}

	override fun load()
	{
		INSTANCE = this
	}

	override fun startup()
	{
		saveDefaultConfig()
		DEBUG_MODE = DebugMode.Match(config.getString("debug_mode"))
		LogDebug(Level.INFO, "Starting in DEBUG mode!")

		RuneCraftItems.RegisterRuneCustomItems()
		RuneItems.Register()

		pluginManager.registerEvents(CustomItemListener(), this)

		listen<PlayerJoinEvent> {
			it.player.setResourcePack("https://www.dropbox.com/sh/5edb0094fafinn8/AADwGI1clZVKyf78iAsETpB8a?dl=1")
		}

		command("runecraft_items_test") {
			runs {
				player.give(RuneCraftItems.IRON_CHISEL.NewStack(), RuneCraftItems.GOLD_CHISEL.NewStack(),
					RuneCraftItems.DIAMOND_CHISEL.NewStack())
			}
		}

		command("rc_testing") {
			runs {
				try {
					val hpRune = RuneItems.HealthRune.NewStack()
					val dmgRune = RuneItems.DamageRune.NewStack()
					val stat = StatRegistry.POTION_STAT.NewStatInstance(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5000, 1))
					val rune = Rune(RuneType.Default)
					rune.Stats = ObjectOpenHashSet(setOf(stat))
					val runeItem = RuneCraftItems.CARVED_RUNE.NewStack(rune)
					player.give(hpRune, dmgRune, runeItem)
				}
				catch (e: Exception)
				{
					e.printStackTrace()
				}

			}
		}

		command("rc_item_build") {
			runs {
				try
				{
					val handItem = player.getHandItem(EquipmentSlot.HAND)
					if (handItem != null)
					{
						val crafterGUI = CrafterGUI(handItem, 3)
						crafterGUI.Open(player)
					}
				}
				catch (e: Exception)
				{
					e.printStackTrace()
				}
			}
		}

		command("runecraft_test") {
			runs {
				try
				{
					player.give(RuneCraftItems.UNCARVED_RUNE.NewStack(Rune(RuneType.Overworld)))
				}
				catch (e: Exception)
				{
					e.printStackTrace()
				}
			}
		}
	}

	override fun shutdown()
	{
	}

}

enum class DebugMode(val Value: Int)
{

	DEBUG(1), RELEASE(2), DIST(4);

	companion object
	{
		fun Match(s: String?): DebugMode
		{
			try
			{
				val str = s ?: "DEBUG"
				return valueOf(str.uppercase(Locale.getDefault()))
			}
			catch (e: Exception)
			{
				System.err.println(e.stackTrace)
			}
			return DEBUG
		}
	}
}