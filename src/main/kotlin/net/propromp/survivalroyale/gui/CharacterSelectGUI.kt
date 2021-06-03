package net.propromp.survivalroyale.gui

import net.kyori.adventure.text.Component
import net.propromp.survivalroyale.CharacterType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class CharacterSelectGUI(val player:Player) {
    val inv = Bukkit.createInventory(player,27,"キャラクターを選択してください。")
    companion object {
        val invs = mutableListOf<Inventory>()
    }
    init {
        for( i in 0..9) {
            inv.setItem(i, ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        }
        inv.setItem(10,ItemStack(Material.FEATHER).also{
            it.itemMeta=it.itemMeta.also{it.displayName(Component.text("${ChatColor.BLUE}Dr. Typhoon"));it.lore = CharacterType.DR_TYPHOON.lore}
        })
        inv.setItem(11,ItemStack(Material.TNT).also{
            it.itemMeta=it.itemMeta.also{it.displayName(Component.text("${ChatColor.BLUE}Blast Master"));it.lore = CharacterType.BLASTMASTER.lore}
        })
        inv.setItem(12,ItemStack(Material.FLINT_AND_STEEL).also{
            it.itemMeta=it.itemMeta.also{it.displayName(Component.text("${ChatColor.BLUE}Flame Man"));it.lore = CharacterType.FLAMEMAN.lore}
        })
        inv.setItem(13,ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        inv.setItem(14,ItemStack(Material.FIREWORK_ROCKET).also{
            it.itemMeta=it.itemMeta.also{it.displayName(Component.text("${ChatColor.BLUE}Rocket Man"));it.lore = CharacterType.ROCKETMAN.lore}
        })
        inv.setItem(15,ItemStack(Material.TOTEM_OF_UNDYING).also{
            it.itemMeta=it.itemMeta.also{it.displayName(Component.text("${ChatColor.BLUE}Wizard"));it.lore = CharacterType.WIZARD.lore}
        })
        inv.setItem(16,ItemStack(Material.ENDER_EYE).also{
            it.itemMeta=it.itemMeta.also{it.displayName(Component.text("${ChatColor.BLUE}Spy"));it.lore = CharacterType.SPY.lore}
        })
        for( i in 17..26) {
            inv.setItem(i, ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        }
    }
    fun show(){
        player.openInventory(inv)
        invs.add(inv)
    }
}