package me.bscal.runecraft

import me.bscal.runecraft.custom_items.CustomItemsListener
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.give
import net.axay.kspigot.extensions.pluginManager
import net.axay.kspigot.main.KSpigot
import org.bukkit.event.player.PlayerJoinEvent
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

		RuneTool.RegisterTools()
		RegisterRuneCustomItems()

		pluginManager.registerEvents(CustomItemsListener(), this)

		listen<PlayerJoinEvent> {
			it.player.setResourcePack("https://www.dropbox.com/sh/5edb0094fafinn8/AADwGI1clZVKyf78iAsETpB8a?dl=1")
		}

		command("runecraft_items_test") {
			runs {
				player.give(RuneTool.IRON_CHISEL.NewStack(), RuneTool.GOLD_CHISEL.NewStack(), RuneTool.DIAMOND_CHISEL.NewStack())
			}
		}

		command("runecraft_test") {
			runs {
				try
				{
					player.give(UncarvedRune.NewStack(Rune(RuneType.Overworld)))
				}
				catch(e: Exception) {
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