package me.bscal.runecraft

import net.axay.kspigot.main.KSpigot
import java.util.*
import java.util.logging.Level

class RuneCraft : KSpigot()
{
	companion object
	{
		lateinit var INSTANCE: RuneCraft; private set
		lateinit var DEBUG_MODE: DebugMode private set

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