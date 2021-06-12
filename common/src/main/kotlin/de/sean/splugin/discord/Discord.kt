package de.sean.splugin.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URL
import javax.security.auth.login.LoginException

// Simple class to handle messages, events and activity for Discord
class Discord(config: FileConfiguration) {
    companion object {
        lateinit var instance: Discord

        fun isInitialized(): Boolean = ::instance.isInitialized
    }

    private var jda: JDA? = null

    private val token: String?
    val channels: List<Long>
    private val webhooks: MutableList<String>
    val joinMessage: Boolean
    val leaveMessage: Boolean
    val discordFormat: String?

    init {
        instance = this

        token = config.getString("discord.token")
        joinMessage = config.getBoolean("discord.joinMessage")
        leaveMessage = config.getBoolean("discord.leaveMessage")
        discordFormat = config.getString("chatFormat.discordFormat")
        webhooks = config.getStringList("discord.webhooks")
        channels = config.getLongList("discord.channels")
    }

    /**
     * Start this Discord instance, creating the JDA instance and waiting for it
     * to start properly and run. Also registers any slash commands that we want.
     */
    fun start() {
        if (token != null) {
            val builder = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
            try {
                builder.setMemberCachePolicy(MemberCachePolicy.ALL)
                builder.setActivity(Activity.playing("Minecraft"))
                jda = builder.build()
                jda!!.awaitReady()

                // Update the commands as interactions.
                // This might take up to an hour to be activated properly.
                val commands = jda!!.updateCommands()

                commands.addCommands(
                    CommandData("help", "Get some help regarding this bot.")
                )
                commands.addCommands(
                    CommandData("players", "Get a list of online players.")
                )
                commands.addCommands(
                    CommandData("msg", "Private message a player on the server")
                        .addOptions(OptionData(OptionType.STRING, "player-name", "The player's name to msg.")
                            .setRequired(true))
                        .addOptions(OptionData(OptionType.STRING, "message", "The message to send")
                            .setRequired(true))
                )

                commands.queue()
            } catch (e: LoginException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun close() {
        jda?.shutdownNow()
    }

    fun addEventListener(listener: DiscordEventHandler) {
        if (jda != null) jda!!.addEventListener(listener)
    }

    fun sendMessage(message: String, player: Player) {
        for (webhook in webhooks) {
            val query =
                "{\"username\": \"${player.name}\", \"content\": \"${message}\", \"avatar_url\": \"https://crafatar.com/avatars/${player.uniqueId}?overlay\"}"
            val queryBytes = query.toByteArray(Charsets.UTF_8)

            val mUrl = URL(webhook)
            val urlConn = mUrl.openConnection() as HttpURLConnection
            urlConn.doOutput = true
            urlConn.requestMethod = "POST"
            urlConn.setRequestProperty("Content-Type", "application/json")
            urlConn.setRequestProperty("Content-Length", queryBytes.size.toString())
            urlConn.outputStream.write(queryBytes)
            urlConn.outputStream.flush()
            urlConn.connect()

            // Connect and wait for input stream
            urlConn.inputStream
        }
    }

    fun updateActivity(activity: Activity) {
        if (jda != null) jda!!.presence.activity = activity
    }
}
