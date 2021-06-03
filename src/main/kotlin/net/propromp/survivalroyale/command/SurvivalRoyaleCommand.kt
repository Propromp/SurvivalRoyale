package net.propromp.survivalroyale.command

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.LocationArgument
import dev.jorel.commandapi.arguments.TimeArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.propromp.survivalroyale.CharacterType
import net.propromp.survivalroyale.SurvivalRoyale
import net.propromp.survivalroyale.SurvivalRoyaleGame
import org.bukkit.Location

class SurvivalRoyaleCommand(plugin:SurvivalRoyale) {
    init {
        val reset = CommandAPICommand("reset")
            .withPermission("survivalroyale.reset")
            .executes(CommandExecutor { sender, args ->
                plugin.game.reset()
            })
        val start = CommandAPICommand("start")
            .withPermission("survivalroyale.start")
            .executes(CommandExecutor { sender, args ->
                plugin.game.start()
            })
        val stop = CommandAPICommand("stop")
            .withPermission("survivalroyale.stop")
            .executes(CommandExecutor { sender, args ->
                plugin.game.stop()
            })
        val moveCenter = CommandAPICommand("movecenter")
            .withPermission("survivalroyale.reset")
            .withArguments(LocationArgument("from"),LocationArgument("to"),TimeArgument("time"))
            .executes(CommandExecutor { sender, args ->
                SurvivalRoyaleGame.moveCenterSlowly(args[0] as Location,args[1] as Location,args[2] as Int)
            })
        val selectCharacter = CommandAPICommand("selectCharacter")
            .withPermission("survivalroyale.selectcharacter")
            .withArguments(CommandUtil.characterTypeArgument("type"))
            .executesPlayer(PlayerCommandExecutor { sender, args ->
                plugin.game.players[sender]=args[0] as CharacterType
            })
        val ability = CommandAPICommand("ability")
            .withPermission("survivalroyale.ability")
            .withArguments(IntegerArgument("number",0,1))
            .executesPlayer(PlayerCommandExecutor { sender, args ->
                val function = when(args[0] as Int){
                    0->plugin.game.players[sender]?.ability1
                    1->plugin.game.players[sender]?.ability2
                    else -> null
                }
                function?.invoke(sender)
            })
        CommandAPICommand("survivalroyale")
            .withAliases("sr")
            .withSubcommand(reset)
            .withSubcommand(start)
            .withSubcommand(stop)
            .withSubcommand(moveCenter)
            .withSubcommand(selectCharacter)
            .withSubcommand(ability)
            .register()
    }
}