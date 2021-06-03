package net.propromp.survivalroyale

import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEffect
import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.metadata.MetadataValueAdapter
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.lang.Math.cos
import java.util.*
import kotlin.math.roundToInt

enum class CharacterType(val ability1: ((Player) -> Unit), val ability2: (Player) -> Unit,val a1Name:String,val a2Name:String, vararg description: String) {

    NONE(fun(player: Player) {}, fun(player: Player) {},"",""),
    DR_TYPHOON(
        fun(player: Player) {
            var direction = player.location.direction.multiply(5)
            direction.y = 0.0
            player.velocity = direction
            player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 5f, 1f)
            object : BukkitRunnable() {
                var tick = 0
                override fun run() {
                    if (tick < 15) {
                        player.world.spawnParticle(Particle.SWEEP_ATTACK, player.location.add(0.0, 1.5, 0.0), 1)
                        val entity = player.location.getNearbyEntities(2.0, 2.0, 2.0)
                        entity.forEach {
                            if (it is LivingEntity && it != player) {
                                it.damage(10.0, player)
                                it.killer = player
                            }
                        }
                    } else {
                        cancel()
                    }
                    tick++
                }
            }.runTaskTimer(SurvivalRoyale.plugin, 0, 1)
        },
        fun(player: Player) {
            player.world.setStorm(true)
            player.world.isThundering = true
            player.world.thunderDuration = 600
            Bukkit.broadcastMessage("${player.name}が 台風の目 を発動した！")
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
            }
            object : BukkitRunnable() {
                var tick = 0
                override fun run() {
                    if (tick < 600) {
                        for (i in 0..120) {
                            for (j in 0..100) {
                                val r = j.toDouble() / 10.0
                                player.world.spawnParticle(
                                    Particle.ENCHANTMENT_TABLE,
                                    player.location.add(
                                        kotlin.math.cos(Math.toRadians((i * 3).toDouble())) * r,
                                        5.0,
                                        kotlin.math.sin(Math.toRadians((i * 3).toDouble())) * r
                                    ),
                                    1
                                )
                                player.getNearbyEntities(20.0, 20.0, 20.0).forEach {
                                    if (it is LivingEntity) {
                                        it.damage(5.0)
                                    }
                                }
                            }
                        }
                    } else {
                        player.world.setStorm(false)
                        player.world.isThundering = false
                        cancel()
                    }
                    tick += 10
                }
            }.runTaskTimer(SurvivalRoyale.plugin, 0, 10)//秒間10ダメージ
        },"ジェット","台風の目",
        "${ChatColor.WHITE}移動速度上昇・ジャンプ力上昇","",
        "${ChatColor.WHITE}通常技",
        "   ${ChatColor.WHITE}ジェット 高速で前に進みぶつかったものにダメージを与える",
        "${ChatColor.WHITE}必殺技",
        "   ${ChatColor.WHITE}台風の目 自分を中心に台風を発生させる"
    ),
    BLASTMASTER(
        fun(player: Player) {
            player.world.playSound(player.location, Sound.ENTITY_TNT_PRIMED, 1f, 1f)
            val tnt = player.world.spawnEntity(player.location, EntityType.PRIMED_TNT) as TNTPrimed
            tnt.fuseTicks = 40
            tnt.velocity = player.location.direction
        },
        fun(player: Player) {
            Bukkit.broadcastMessage("${player.name}が グレネードランチャー を発動した！")
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
            }
            object : BukkitRunnable() {
                var tick = 0
                override fun run() {
                    player.world.playSound(player.location, Sound.ENTITY_TNT_PRIMED, 1f, 1f)
                    val tnt = player.world.spawnEntity(player.location, EntityType.PRIMED_TNT) as TNTPrimed
                    tnt.fuseTicks = 40
                    tnt.velocity = Vector(Math.random() - 0.5, Math.random(), Math.random() - 0.5)
                    tick += 3

                    if (tick > 200 ) {
                        cancel()
                    }
                }
            }.runTaskTimer(SurvivalRoyale.plugin, 0, 3)
        },"グレネード","グレネードランチャー",
        "${ChatColor.WHITE}攻撃力上昇・爆発ダメージを受けない","",
        "${ChatColor.WHITE}通常技",
        "   ${ChatColor.WHITE}グレネード TNTを投げる",
        "${ChatColor.WHITE}必殺技",
        "   ${ChatColor.WHITE}グレネードランチャー 周りに大量のTNTをまき散らす"
    ),
    FLAMEMAN(
        fun(player: Player) {
            val location = player.location.add(player.location.direction.multiply(5)).add(0.0, 1.5, 0.0)
            player.world.spawnParticle(Particle.FLAME, location, 100, 0.5, 0.5, 0.5)
            for (x in -2..2) {
                for (y in -2..2) {
                    for (z in -2..2) {
                        val blockPosition = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                        if (blockPosition.block.type == Material.AIR) {
                            blockPosition.block.type = Material.FIRE
                        }
                    }
                }
            }
        },
        fun(player: Player) {
            Bukkit.broadcastMessage("${player.name}が 地獄の業火 を発動した！")
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
            }
            val location = player.location.add(0.0, 1.5, 0.0)
            player.world.spawnParticle(Particle.FLAME, location, 100, 0.5, 0.5, 0.5)
            for (x in -20..20) {
                for (y in -20..20) {
                    for (z in -20..20) {
                        if(Math.random()<0.3) {
                            val blockPosition = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                            if (blockPosition.block.type == Material.AIR) {
                                blockPosition.block.type = Material.FIRE
                            }
                        }
                    }
                }
            }
            for (x in -20..20) {
                for (y in -20..20) {
                    for (z in -20..20) {
                        val blockPosition = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                        if (blockPosition.block.type == Material.FIRE) {
                            blockPosition.clone().add(0.0, -1.0, 0.0).block.type = Material.NETHERRACK
                        }
                    }
                }
            }
        },"フレーム","地獄の業火",
        "${ChatColor.WHITE}炎ダメージを受けない・鉱石を掘ると自動でインゴットになる","",
        "${ChatColor.WHITE}通常技",
        "   ${ChatColor.WHITE}フレーム 目の前に火の玉を出現させる",
        "${ChatColor.WHITE}必殺技",
        "   ${ChatColor.WHITE}地獄の業火 自分の周りを焼け野原にする"
    ),
    ROCKETMAN(
        fun(player: Player) {
            player.getNearbyEntities(6.0, 6.0, 6.0).forEach {
                if (it is LivingEntity) {
                    it.velocity = Vector(0.0, 1.5, 0.0)
                    it.world.playSound(it.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f)
                }
            }
        },
        fun(player: Player) {
            Bukkit.broadcastMessage("${player.name}が メテオラッシュ を発動した！")
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
            }
            object : BukkitRunnable() {
                var tick = 0
                override fun run() {
                    val location = player.location.add(
                        (Math.random() * 40.0 - 20.0).roundToInt().toDouble() + 0.5,
                        20.0,
                        (Math.random() * 40.0 - 20.0).roundToInt().toDouble() + 0.5
                    )
                    val fallingBlock =
                        player.world.spawnFallingBlock(location, Material.MAGMA_BLOCK.createBlockData()) as FallingBlock
                    fallingBlock.persistentDataContainer.set(
                        NamespacedKey(SurvivalRoyale.plugin, "meteorite"),
                        PersistentDataType.STRING, "a"
                    )
                    tick += 5
                    if (tick > 300) {
                        cancel()
                    }
                }
            }.runTaskTimer(SurvivalRoyale.plugin, 0, 5)
        },"ロケット","メテオラッシュ",
        "${ChatColor.WHITE}エリトラが消えない・15秒ごとにインベントリにロケット花火が追加される","",
        "${ChatColor.WHITE}通常技",
        "   ${ChatColor.WHITE}ロケット 半径3m以内のエンティティを上に飛ばす",
        "${ChatColor.WHITE}必殺技",
        "   ${ChatColor.WHITE}メテオラッシュ 空から大量の隕石を落とす"
    ),
    WIZARD(
        fun(player: Player) {
            player.health = 20.0
            player.sendMessage("${ChatColor.AQUA}回復しました。")
        },
        fun(player: Player) {
            Bukkit.broadcastMessage("${player.name}が マジカルウェーブ を発動した！")
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
            }
            object : BukkitRunnable() {
                var tick = 0
                override fun run() {
                    val r = (tick % 40)
                    if (tick < 600) {
                        for (i in 0..360) {
                            val location = player.location.add(
                                kotlin.math.cos(Math.toRadians((i).toDouble())) * r,
                                1.5,
                                kotlin.math.sin(Math.toRadians((i).toDouble())) * r
                            )
                            player.world.spawnParticle(Particle.CRIT_MAGIC, location, 1, 0.0, 0.0, 0.0, 0.0, null, false)
                            location.getNearbyEntities(1.0, 1.0, 1.0).forEach {
                                if (it is LivingEntity && it != player) {
                                    it.damage(10.0)
                                    it.velocity.add(Vector(0.0, 0.5, 0.0))
                                }
                            }
                        }
                    } else {
                        player.world.setStorm(false)
                        player.world.isThundering = false
                        cancel()
                    }
                    tick += 1
                }
            }.runTaskTimer(SurvivalRoyale.plugin, 0, 1)
        },"ヒール","マジカルウェーブ",
        "${ChatColor.WHITE}物理ダメージを2割カット・1分毎にポーションがランダムで配られる","",
        "${ChatColor.WHITE}通常技",
        "   ${ChatColor.WHITE}ヒール 自分を回復",
        "${ChatColor.WHITE}必殺技",
        "   ${ChatColor.WHITE}マジカルウェーブ 魔法の波動を発生させる"
    ),
    SPY(
        fun(player: Player) {
            player.location.getNearbyEntities(20.0, 20.0, 20.0).filter { it.type == EntityType.PLAYER }.forEach {
                val packet = PacketPlayOutEntityEffect(
                    (it as org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer).handle.id,
                    net.minecraft.server.v1_16_R3.MobEffect(
                        net.minecraft.server.v1_16_R3.MobEffectList.fromId(24)
                    )
                )//発光
                it.handle.playerConnection.sendPacket(packet)
            }
        },
        fun(player: Player) {
            Bukkit.broadcastMessage("${player.name}が 暗殺 を発動した！")
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
            }
            SurvivalRoyale.plugin.game.players.forEach { key, value ->
                key.hidePlayer(SurvivalRoyale.plugin, player)
            }
            Bukkit.getScheduler().runTaskLater(
                SurvivalRoyale.plugin,
                Runnable {
                    Bukkit.getOnlinePlayers().forEach {
                        it.showPlayer(SurvivalRoyale.plugin, player)
                    }
                }, 200
            )
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 200, 5, true, true, true))
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 5, true, true, true))
        },"諜報","暗殺",
        "${ChatColor.WHITE}スニークで壁を上ることができる・二割の攻撃を回避","",
        "${ChatColor.WHITE}通常技",
        "   ${ChatColor.WHITE}諜報 半径10m以内の敵を10秒間発光させる",
        "${ChatColor.WHITE}必殺技",
        "   ${ChatColor.WHITE}暗殺 10秒間姿を透明にし、移動速度とジャンプ力を大幅に上昇させる"
    );

    val lore = description.toMutableList()
}
