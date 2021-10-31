package me.bscal.runecraft.utils

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.bscal.runecraft.RuneCraft
import me.bscal.runecraft.stats.*
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta

fun ObjectOpenHashSet<StatInstance>.addStat(stat: StatInstance)
{
	val current = this.get(stat)
	if (current != null) current.GetStat()?.CombineInstance(stat, current)
	else this.add(stat)
}

// ***********************************************
// ItemStack Stat

val CustomStatKey: NamespacedKey = NamespacedKey(RuneCraft.INSTANCE, "custom_stats")
val SpellStatKey: NamespacedKey = NamespacedKey(RuneCraft.INSTANCE, "spell_stat")

fun ItemMeta.getCustomStats(): MutableList<StatInstance>
{
	return (this.persistentDataContainer.get(CustomStatKey, StatInstanceListTagType()) ?: return ArrayList()) as MutableList<StatInstance>
}

fun ItemMeta.getCustomStat(stat: BaseStat): StatInstance?
{
	for (instance in this.getCustomStats()) if (instance.Id == stat.Id) return instance
	return null
}

fun ItemMeta.setCustomStat(instance: StatInstance)
{
	if (instance.GetStat() is CustomStat) return
	val list = this.getCustomStats()
	list.add(instance)
	this.persistentDataContainer[CustomStatKey, StatInstanceListTagType()] = list
}

fun ItemMeta.addAllCustomStats(instances: List<StatInstance>)
{
	val list = this.getCustomStats()
	list.addAll(instances.filter {
		it.GetStat() is CustomStat
	})
	this.persistentDataContainer[CustomStatKey, StatInstanceListTagType()] = list
}

// *********************************

fun ItemMeta.getSpells(): MutableList<StatInstance>
{
	return (this.persistentDataContainer.get(SpellStatKey, StatInstanceListTagType()) ?: return ArrayList()) as MutableList<StatInstance>
}

fun ItemMeta.getSpell(stat: SpellStat): StatInstance?
{
	for (instance in this.getCustomStats()) if (instance.Id == stat.Id) return instance
	return null
}

fun ItemMeta.setSpell(instance: StatInstance)
{
	if (instance.GetStat() is SpellStat) return
	val list = this.getCustomStats()
	list.add(instance)
	this.persistentDataContainer[SpellStatKey, StatInstanceListTagType()] = list
}

fun ItemMeta.setAllSpells(instances: List<StatInstance>)
{
	val list = this.getCustomStats()
	list.addAll(instances.filter {
		it.GetStat() is SpellStat
	})
	this.persistentDataContainer[SpellStatKey, StatInstanceListTagType()] = list
}