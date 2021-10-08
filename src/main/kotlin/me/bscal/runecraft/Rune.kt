package me.bscal.runecraft

class Rune(val Type: RuneType)
{

}

@JvmRecord
data class RuneType(val Name: String)
{
	companion object
	{
		val Overworld = RuneType("Overworld")
		val Default = Overworld
	}
}