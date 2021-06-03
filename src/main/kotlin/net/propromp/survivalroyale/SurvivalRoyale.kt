package net.propromp.survivalroyale

import dev.jorel.commandapi.CommandAPI
import net.propromp.survivalroyale.command.SurvivalRoyaleCommand
import net.propromp.survivalroyale.listener.EventListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class SurvivalRoyale : JavaPlugin() {
	companion object {
		lateinit var plugin:SurvivalRoyale
	}
	lateinit var game:SurvivalRoyaleGame
	override fun onLoad() {
		CommandAPI.onLoad(false)
	}
	override fun onEnable() {
		plugin=this
		game=SurvivalRoyaleGame(this)
		CommandAPI.onEnable(this)
		SurvivalRoyaleCommand(this)
		EventListener(this)
	}
	override fun onDisable() {
		//停止処理
	}
}