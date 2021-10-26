package me.bscal.runecraft.items.runeitems

import me.bscal.runecraft.Rune
import me.bscal.runecraft.RuneCraft
import me.bscal.runecraft.items.customitems.CustomItem
import me.bscal.runecraft.items.customitems.CustomItems
import me.bscal.runecraft.stats.Stat
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.items.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.function.Consumer

object RuneCraftItems
{
	private const val CHISEL_LORE_USE = "Used in the art of rune crafting"

	val UsesKey = NamespacedKey(RuneCraft.INSTANCE, "uses")

	val UNCARVED_RUNE = UncarvedRuneItem()
	val CARVED_RUNE = CarvedRuneItem()
	val IRON_CHISEL = RuneTool("iron_chisel", BreakLevel.LEVEL_1, CreateChisel("Iron Chisel", 11000, 16))
	val GOLD_CHISEL = RuneTool("gold_chisel", BreakLevel.LEVEL_2, CreateChisel("Gold Chisel", 11001, 16))
	val DIAMOND_CHISEL = RuneTool("diamond_chisel", BreakLevel.LEVEL_3, CreateChisel("Diamond Chisel", 11002, 32))

	fun RegisterRuneCustomItems()
	{
		CustomItems.Register("rc_uncarved_rune", UNCARVED_RUNE)
		CustomItems.Register("rc_carved_rune", CARVED_RUNE)
		IRON_CHISEL.Register()
		GOLD_CHISEL.Register()
		DIAMOND_CHISEL.Register()
	}

	class UncarvedRuneItem() : CustomItem(itemStack(Material.END_STONE_BRICKS) {
		meta {
			name = "${KColors.ROSYBROWN}Uncarved Rune"
			customModel = 11000
			addLore {
				+"${KColors.LIGHTSLATEGRAY}A rune which has not been engraved."
				+"${KColors.LIGHTSLATEGRAY}To begin engraving right click in hand."
			}
		}
	}, true)
	{
		init
		{
			InteractCallback = Consumer<PlayerInteractEvent> {
				val rune = Rune.Deserialize(it.item!!)
				rune?.Open(it.player, it.item!!)
			}
		}

		fun NewStack(rune: Rune): ItemStack
		{
			val itemStack = super.NewStack()
			rune.Serialize(itemStack)
			return itemStack
		}
	}

	class CarvedRuneItem() : CustomItem(itemStack(Material.END_STONE_BRICKS) {
		meta {
			name = "${KColors.ROSYBROWN}Engraved Rune"
			customModel = 11001
			addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1)
			flag(ItemFlag.HIDE_ENCHANTS)
			addLore {
				+"${KColors.LIGHTSLATEGRAY}A rune that is engraved with power."
				+"${KColors.LIGHTSLATEGRAY}These runes can be added to armor/tools."
				+" "

			}
		}
	}, true)
	{
		fun NewStack(stats: Collection<Stat>): ItemStack
		{
			val itemStack = super.NewStack()
			for (stat in stats)
			{
				stat.ApplyToItemStack(itemStack)
				itemStack.editMeta {
					it.addLore {
						+"${KColors.LIGHTSLATEGRAY}${stat.GetLoreString()}"
					}
				}
			}
			return itemStack
		}
	}

	fun CreateChisel(displayName: String, modelId: Int, uses: Int): ItemStack
	{
		val itemStack = itemStack(Material.WOODEN_HOE) {
			meta {
				name = "${KColors.WHITE}$displayName"
				addLore {
					+CHISEL_LORE_USE
					+"Uses: $uses"
				}
				customModel = modelId
				persistentDataContainer.set(UsesKey, PersistentDataType.INTEGER, uses)
			}
		}
		return itemStack
	}
}