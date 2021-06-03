package net.propromp.survivalroyale.listener

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.minecraft.server.v1_16_R3.Advancements
import net.propromp.survivalroyale.CharacterType
import net.propromp.survivalroyale.SurvivalRoyale
import net.propromp.survivalroyale.gui.CharacterSelectGUI
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


class EventListener(val plugin:SurvivalRoyale):Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this,plugin)
        Bukkit.getScheduler().runTaskTimer(plugin,
            Runnable {
                plugin.game.players.keys.forEach {
                    val component = TextComponent(when(plugin.game.isInRing(it.location)){
                        true->"${ChatColor.GREEN}リング内です。"
                        false->"${ChatColor.RED}リング外です。"
                    })
                    component.addExtra("\n${ChatColor.WHITE}ポイント：${plugin.game.point[it]}")
                    it.spigot().sendMessage(ChatMessageType.ACTION_BAR,component)
                }
            },0,20
        )
    }
    @EventHandler
    fun onDamage(e:EntityDamageEvent){
        val entity = e.entity
        if(entity is Player){
            if(plugin.game.players[entity]==CharacterType.BLASTMASTER) {
                if(e.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION||e.cause==EntityDamageEvent.DamageCause.ENTITY_EXPLOSION){
                    e.isCancelled=true
                    return
                }
            }
            if(plugin.game.players[entity]==CharacterType.FLAMEMAN) {
                if(e.cause == EntityDamageEvent.DamageCause.FIRE||e.cause==EntityDamageEvent.DamageCause.FIRE_TICK||e.cause==EntityDamageEvent.DamageCause.LAVA){
                    e.isCancelled=true
                    return
                }
            }
            if(plugin.game.players[entity]==CharacterType.SPY) {
                if(Math.random()<0.2){
                    e.isCancelled=true
                    return
                }
            }
            if(e.damage>=entity.health){
                e.isCancelled=true
                entity.health=20.0
                entity.gameMode=GameMode.SPECTATOR
                entity.teleport(plugin.game.center)
                plugin.game.players.remove(entity)
                Bukkit.broadcastMessage("${ChatColor.DARK_RED}${entity.name} がダウンしました。")
                if(plugin.game.players.size==1){
                    Bukkit.getOnlinePlayers().forEach {
                        it.teleport(plugin.game.players.keys.toList()[0])
                        it.sendTitle("${plugin.game.players.keys.toList()[0].name}の勝利！","",10,100,10)
                        it.playSound(it.location, Sound.UI_TOAST_CHALLENGE_COMPLETE,1f,1f)
                        plugin.game.stop()
                    }
                }
            }
        }
    }
    @EventHandler
    fun onFallingBlockLand(event: EntityChangeBlockEvent) {
        val entity = event.entity
        if (event.entityType == EntityType.FALLING_BLOCK) {
            if(entity.persistentDataContainer.has(NamespacedKey(SurvivalRoyale.plugin,"meteorite"), PersistentDataType.STRING)){
                entity.location.createExplosion(3f,true)
            }
        }
    }
    @EventHandler
    fun onInteract(e:PlayerInteractEvent){
        e.item?.let{item->
            if(item.itemMeta.persistentDataContainer.has(NamespacedKey(SurvivalRoyale.plugin,"ability"),
                    PersistentDataType.INTEGER)){
                val num = item.itemMeta.persistentDataContainer.get(NamespacedKey(SurvivalRoyale.plugin,"ability"),
                    PersistentDataType.INTEGER)
                var point =(SurvivalRoyale.plugin.game.point[e.player] ?: 0)
                when(num){
                    0->CharacterSelectGUI(e.player).show()
                    1->{
                        if(point>=1) {
                            SurvivalRoyale.plugin.game.players[e.player]?.ability1?.invoke(e.player)
                            point-=1
                        } else {
                            e.player.sendMessage("${ChatColor.DARK_RED}ポイントが足りません！")
                            e.player.playSound(e.player.location,Sound.ENTITY_ENDERMAN_TELEPORT,1f,0.5f)
                        }
                    }
                    2->{
                        if(point>=10){
                            SurvivalRoyale.plugin.game.players[e.player]?.ability2?.invoke(e.player)
                            point-=10
                        } else {
                            e.player.sendMessage("${ChatColor.DARK_RED}ポイントが足りません！")
                            e.player.playSound(e.player.location,Sound.ENTITY_ENDERMAN_TELEPORT,1f,0.5f)
                        }
                    }
                }
                SurvivalRoyale.plugin.game.point[e.player] = point
                e.isCancelled=true
            }
        }
    }
    @EventHandler
    fun onClick(e:InventoryClickEvent){
        if(CharacterSelectGUI.invs.contains(e.clickedInventory)){
            e.isCancelled=true
            plugin.game.players[e.whoClicked as Player]=when(e.slot){
                10->CharacterType.DR_TYPHOON
                11->CharacterType.BLASTMASTER
                12->CharacterType.FLAMEMAN
                14->CharacterType.ROCKETMAN
                15->CharacterType.WIZARD
                16->CharacterType.SPY
                else -> CharacterType.NONE
            }
            if(e.slot in 10..12||e.slot in 14..16){
                (e.whoClicked as Player).playSound(e.whoClicked.location,Sound.UI_BUTTON_CLICK,1f,1f)
                e.whoClicked.sendMessage("${e.currentItem!!.itemMeta.displayName} を選択しました")
                e.clickedInventory!!.close()
            }
        }
    }
    @EventHandler
    fun onBlockBreak(e:BlockBreakEvent){
        if(plugin.game.players[e.player]==CharacterType.FLAMEMAN) {
            val material = when(e.block.type){
                Material.IRON_ORE->Material.IRON_INGOT
                Material.GOLD_ORE->Material.GOLD_INGOT
                else->return
            }
            e.block.type = Material.AIR
            e.block.world.dropItemNaturally(e.block.location, ItemStack(material))
        }
    }
    @EventHandler
    fun onAdvancement(e:PlayerAdvancementDoneEvent){
        if(plugin.game.isStarted) {
            if(e.player.getAdvancementProgress(e.advancement).remainingCriteria.isEmpty()) {
                plugin.game.point[e.player] = (plugin.game.point[e.player] ?: 0) + 1
            }
        } else {
            e.advancement.criteria.forEach {
                e.player.getAdvancementProgress(e.advancement).revokeCriteria(it)
            }
        }
    }
}