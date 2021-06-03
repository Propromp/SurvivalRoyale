package net.propromp.survivalroyale

import net.kyori.adventure.text.Component
import net.minecraft.server.v1_16_R3.Item
import net.minecraft.server.v1_16_R3.ItemWorldMap
import net.minecraft.server.v1_16_R3.ItemWorldMapBase
import net.minecraft.server.v1_16_R3.WorldMap
import net.propromp.survivalroyale.map.SurvivalRoyaleMapRenderer
import net.propromp.survivalroyale.map.TargetBorderMapRenderer
import net.propromp.survivalroyale.map.WorldBorderMapRenderer
import org.bukkit.*
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R3.map.CraftMapCanvas
import org.bukkit.craftbukkit.v1_16_R3.map.CraftMapRenderer
import org.bukkit.craftbukkit.v1_16_R3.map.CraftMapView
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.concurrent.CompletionStage
import kotlin.math.roundToInt


class SurvivalRoyaleGame(val plugin: SurvivalRoyale) {
    var world: World = Bukkit.getWorld("world")!!
    var center: Location = Location(world, 0.0, 200.0, 0.0)
    val players = mutableMapOf<Player, CharacterType>()
    val point = mutableMapOf<Player,Int>()
    var targetSize = 2000
    var isStarted = false
    lateinit var br: BukkitRunnable
    init {


        Bukkit.getScheduler().runTaskTimer(plugin,
            Runnable {
                players.filter{it.value==CharacterType.ROCKETMAN}.forEach {
                    it.key.inventory.addItem(ItemStack(Material.FIREWORK_ROCKET))
                }
            },0,300
        )

        Bukkit.getScheduler().runTaskTimer(plugin,
            Runnable {
                players.filter{it.value==CharacterType.WIZARD}.forEach {
                    val item = ItemStack(Material.POTION)
                    val meta = item.itemMeta as PotionMeta
                    meta.basePotionData= PotionData(PotionType.values().random())
                    item.itemMeta=meta
                    it.key.inventory.addItem(item)
                }
            },0,1200
        )
        Bukkit.getScheduler().runTaskTimer(plugin,
            Runnable {
                players.filter{it.value==CharacterType.SPY}.forEach {
                    if(it.key.isSneaking&& it.key.getTargetBlock(1)?.type?.isAir == false) {
                        it.key.velocity.y = 0.1
                    }
                }
            },0,1
        )
    }
    fun reset() {
        world = Bukkit.getWorld("world")!!
        center = Location(world, /*0Math.random() * 100000*/0.0, 200.0, /*Math.random() * 100000*/0.0)
//        Bukkit.broadcastMessage("${ChatColor.GOLD}チャンクを生成中...")
//        for (x in -32..32) {
//            for (z in -32..32) {
//                center.world.getChunkAtAsync(center.chunk.x + x, center.chunk.z + z).whenComplete { t, _ ->
//                    SurvivalRoyaleMapRenderer.chunks[Pair(center.chunk.x+x,center.chunk.z+z)]=t
//                }
//            }
//        }
//        Bukkit.broadcastMessage("${ChatColor.GOLD}チャンクの生成が完了しました")
        world.worldBorder.center = center
        world.worldBorder.size = 2000.0
        world.time=0
        Bukkit.getOnlinePlayers().forEach { it ->
            it.gameMode = GameMode.SURVIVAL
            it.health = 20.0
            it.foodLevel = 20
            it.teleport(center)
            it.allowFlight = true
            Bukkit.getOnlinePlayers().forEach {it2->
                it2.hidePlayer(plugin,it)
            }
            it.isFlying = true
            it.flySpeed = 0f
            it.inventory.clear()
            it.inventory.setItem(EquipmentSlot.CHEST, ItemStack(Material.ELYTRA))
            it.sendTitle("発射したい方向へ向いてください", "キャラクター選択をしてください", 5, 60, 5)

            Bukkit.advancementIterator().forEach {adv->
                val progress=it.getAdvancementProgress(adv)
                for (criteria in progress.awardedCriteria) progress.revokeCriteria(criteria)
            }
            
            val mapItem = ItemStack(Material.FILLED_MAP)
            mapItem.itemMeta=(mapItem.itemMeta as MapMeta).also{
                it.mapView=Bukkit.createMap(world)
                it.mapView!!.scale=MapView.Scale.FARTHEST
                it.mapView!!.centerX= 0
                it.mapView!!.centerZ= 0
                it.mapView!!.isTrackingPosition=true
                it.mapView!!.addRenderer(WorldBorderMapRenderer())
                it.mapView!!.addRenderer(TargetBorderMapRenderer())
            }
            it.inventory.setItem(0,mapItem)
            val item = ItemStack(Material.WRITABLE_BOOK)
            val meta = item.itemMeta
            meta.displayName(Component.text("キャラクター選択"))
            meta.persistentDataContainer.set(NamespacedKey(SurvivalRoyale.plugin,"ability"),
                PersistentDataType.INTEGER,0)
            item.itemMeta=meta
            it.inventory.setItem(1,item)

            players[it]=CharacterType.NONE
        }
        isStarted=false
    }

    fun start() {
        if (isStarted) {
            return
        }
        isStarted = true
        br = object : BukkitRunnable() {
            var second = 0
            override fun run() {
                    when (second) {
                        1 -> {
                            Bukkit.broadcastMessage("ゲームを開始します。")
                            players.keys.forEach {
                                it.velocity = Vector()
                                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.8f)
                                it.sendTitle("発射したい方向へ向いてください", "開始まで:${ChatColor.UNDERLINE}3", 0, 30, 0)
                                when (players[it]) {
                                    CharacterType.DR_TYPHOON -> {
                                        it.addPotionEffect(
                                            PotionEffect(
                                                PotionEffectType.JUMP,
                                                1920,
                                                2,
                                                true,
                                                true,
                                                true
                                            )
                                        )
                                        it.addPotionEffect(
                                            PotionEffect(
                                                PotionEffectType.SPEED,
                                                1920,
                                                2,
                                                true,
                                                true,
                                                true
                                            )
                                        )
                                    }
                                    CharacterType.BLASTMASTER->{
                                        it.addPotionEffect(
                                            PotionEffect(
                                                PotionEffectType.INCREASE_DAMAGE,
                                                1920,
                                                2,
                                                true,
                                                true,
                                                true
                                            )
                                        )
                                    }
                                }

                                val item1 = ItemStack(Material.BOOK)
                                val meta1 = item1.itemMeta
                                meta1.persistentDataContainer.set(NamespacedKey(SurvivalRoyale.plugin,"ability"),
                                    PersistentDataType.INTEGER,1)
                                meta1.displayName(Component.text(players[it]!!.a1Name))
                                item1.itemMeta=meta1
                                it.inventory.setItem(1,item1)

                                val item2 = ItemStack(Material.ENCHANTED_BOOK)
                                val meta2 = item2.itemMeta
                                meta2.persistentDataContainer.set(NamespacedKey(SurvivalRoyale.plugin,"ability"),
                                    PersistentDataType.INTEGER,2)
                                meta2.displayName(Component.text(players[it]!!.a2Name))
                                item2.itemMeta=meta2
                                it.inventory.setItem(2,item2)
                            }
                        }
                        2 -> {
                            players.keys.forEach {
                                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
                                it.sendTitle("発射したい方向へ向いてください", "開始まで:${ChatColor.UNDERLINE}2", 0, 30, 0)
                            }
                        }
                        3 -> {
                            players.keys.forEach {
                                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1.2f)
                                it.sendTitle("発射したい方向へ向いてください", "開始まで:${ChatColor.UNDERLINE}1", 0, 30, 0)
                            }
                        }
                        4 -> {
                            players.keys.forEach {
                                it.flySpeed = 0.3f
                                it.isInvisible = false
                                it.isFlying = false
                                it.allowFlight = false
                                (it as CraftPlayer).handle.setFlag(7, true)
                                it.velocity = it.location.direction.multiply(10)
                                it.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 10f, 1f)
                                it.sendTitle("SURVIVAL ROYALE START", "最後まで生き残れ", 0, 40, 10)
                            }
                        }
                        60 -> {
                            world.worldBorder.damageAmount = 0.01
                            world.worldBorder.setSize(1024.0, 480)
                            targetSize = 1024
                            val oldCenter = center.clone()
                            center = Location(world, Math.random() * 1024 - 512, 200.0, Math.random() * 1024 - 512)
                            moveCenterSlowly(oldCenter, center, 9600)
                            players.keys.forEach {
                                it.sendTitle("ラウンド1 リングの収縮を開始", "コンパスが向く方向へ向かってください", 10, 40, 10)
                                if(players[it]!=CharacterType.ROCKETMAN)
                                    it.inventory.setItem(EquipmentSlot.CHEST, null)
                                it.compassTarget = center
                                Bukkit.getOnlinePlayers().forEach {it2->
                                    it2.showPlayer(plugin,it)
                                }
                            }
                        }
                        540 -> {
                            players.keys.forEach {
                                it.sendTitle("リングの収縮が完了しました。", "", 10, 40, 10)
                            }
                        }
                        600 -> {
                            world.worldBorder.damageAmount = 0.1
                            world.worldBorder.setSize(512.0, 200)
                            targetSize = 512
                            val oldCenter = center.clone()
                            center = Location(world, Math.random() * 512 - 256, 200.0, Math.random() * 512 - 256)
                            moveCenterSlowly(oldCenter, center, 4000)
                            players.keys.forEach {
                                it.sendTitle("ラウンド2 リングの収縮を開始", "コンパスが向く方向へ向かってください", 10, 40, 10)
                                it.compassTarget = center
                            }
                        }
                        800 -> {
                            players.keys.forEach {
                                it.sendTitle("リングの収縮が完了しました。", "", 10, 40, 10)
                            }
                        }
                        860 -> {
                            world.worldBorder.damageAmount = 1.0
                            world.worldBorder.setSize(256.0, 100)
                            targetSize = 256
                            val oldCenter = center.clone()
                            center = Location(world, Math.random() * 256 - 128, 200.0, Math.random() * 256 - 128)
                            moveCenterSlowly(oldCenter, center, 2000)
                            players.keys.forEach {
                                it.sendTitle("ラウンド3 リングの収縮を開始", "コンパスが向く方向へ向かってください", 10, 40, 10)
                                it.compassTarget = center
                            }
                        }
                        960 -> {
                            players.keys.forEach {
                                it.sendTitle("リングの収縮が完了しました。", "", 10, 40, 10)
                            }
                        }
                        1020 -> {
                            world.worldBorder.setSize(16.0, 900)
                            targetSize = 16
                            val oldCenter = center.clone()
                            center = oldCenter.add(Vector.getRandom().multiply(500))
                            moveCenterSlowly(oldCenter, center, 18000)
                            players.keys.forEach {
                                it.sendTitle("ラウンド4 最終ラウンドです", "コンパスが向く方向へ向かってください", 10, 40, 10)
                                it.compassTarget = center
                            }
                        }
                        1920 -> {
                            players.keys.forEach {
                                it.sendTitle("リングの収縮が完了しました。", "", 10, 40, 10)
                            }
                        }
                }
                second++
            }
        }
        br.runTaskTimer(plugin, 0, 20)
    }

    fun stop() {
        isStarted = false
        br.cancel()
        players.clear()
        Bukkit.broadcastMessage("ゲームを停止しました")
    }

    fun isInRing(location: Location): Boolean {
        return location.x in center.x - targetSize / 2..center.x + targetSize / 2 && location.z in center.z - targetSize / 2..center.z + targetSize / 2
    }

    companion object {
        var targetCenterLocation:Location? = null
        var isMoving = false
        internal fun moveCenterSlowly(from: Location, to: Location, time: Int) {
            targetCenterLocation=to.clone()
            var tick = 0
            object : BukkitRunnable() {
                override fun run() {
                    tick += 1
                    SurvivalRoyale.plugin.game.world.worldBorder.center =
                        from.clone()
                            .add(to.toVector().subtract(from.toVector()).multiply(tick.toDouble() / time.toDouble()))
                    if (tick > time) {
                        targetCenterLocation=null
                        cancel()
                    }
                }
            }.runTaskTimer(SurvivalRoyale.plugin, 0, 1)
        }
    }
}