package net.propromp.survivalroyale.command

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.CustomArgument.*
import net.propromp.survivalroyale.CharacterType
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender


class CommandUtil {
    companion object {
        //Function that returns our custom argument
        fun characterTypeArgument(nodeName: String?): Argument {

            //Construct our CustomArgument that takes in a String input and returns a World object
            return CustomArgument(nodeName, label@ CustomArgumentParser { input: String ->
                //Parse the world from our input
                try {
                    val type = CharacterType.valueOf(input)
                    return@CustomArgumentParser type
                } catch (e:Exception){
                    throw CustomArgumentException(e.message)
                }
            }).overrideSuggestions { _: CommandSender? ->
                CharacterType.values().map{it.name}.toTypedArray()
            }
        }
    }
}