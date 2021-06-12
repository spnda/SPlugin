package de.sean.splugin.discord

import de.sean.splugin.util.ColorUtil
import de.sean.splugin.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.ArrayList
import java.util.logging.Logger

open class DefaultDiscordEventHandler : DiscordEventHandler() {
    override fun onReady(event: ReadyEvent) {
        Logger.getLogger(this.javaClass.simpleName).info("Discord has started! ${event.jda.selfUser.name}")
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.guild == null) {
            // Private message
        } else {
            when (event.name) {
                "help" -> {
                    val eb = EmbedBuilder()
                    eb.setColor(ColorUtil.randomColor())
                    eb.setTitle("Server Help", null)
                    eb.setDescription("This bot links a minecraft server with a discord bot. https://github.com/spnda/SPlugin")
                    eb.addField("Private Messages", "Using `?msg` you can whisper to any player currently online on the server.", false)
                    eb.addField("Online Players", "`?players` will give you a neat list of all online players.", false)
                    event.replyEmbeds(eb.build()).queue()
                }
                "players" -> {
                    val eb = EmbedBuilder()
                    eb.setColor(ColorUtil.randomColor())
                    eb.setTitle("Online Players", null)
                    val players: List<Player> = ArrayList(Bukkit.getOnlinePlayers())
                    eb.setDescription("There are " + players.size + " / " + Bukkit.getMaxPlayers() + " players online.")
                    val playerList = StringBuilder()
                    for (player in players) {
                        playerList.append(player.displayName.replace("§[a-z]".toRegex(), "")).append("\n")
                    }
                    if (playerList.isNotEmpty()) eb.addField("Players online", playerList.toString(), false)
                    event.replyEmbeds(eb.build()).queue()
                }
                "msg" -> {
                    if (event.options.size < 2) {
                        event.reply("An issue occured").setEphemeral(true).queue()
                    } else {
                        val playerName = event.getOption("player-name")?.asString!!
                        val user = event.user
                        val player = Bukkit.getPlayer(playerName)
                        if (player == null) {
                            event.reply("$playerName could not be found.").setEphemeral(true).queue()
                        } else {
                            val message = event.options[1].asString
                            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + user.name + " whispers to you: " + message)
                            event.reply("Sent!").setEphemeral(true).queue()
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!Discord.instance.channels.contains(event.textChannel.idLong)) return
        broadcastMessage(event.message, event.textChannel)
    }

    open fun broadcastMessage(message: Message, channel: GuildChannel) {
        var msg = message.contentStripped
        if (message.attachments.size > 0) msg += if (msg.isNotEmpty()) " " else "" + "[file]" // Show a nice indicator that the person has sent a image.
        val format = Discord.instance.discordFormat ?: "[user]: [message]" // If there is no format specified, we'll use a default one similar to minecraft's messages
        Bukkit.broadcastMessage(format.replace("[user]", message.author.name).replace("[message]", msg))
    }
}
