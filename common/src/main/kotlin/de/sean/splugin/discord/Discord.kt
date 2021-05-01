package de.sean.splugin.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
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

    val channels: MutableMap<Guild, TextChannel>
    val webhooks: MutableList<String>
    val joinMessage: Boolean
    val leaveMessage: Boolean
    val discordFormat: String?

    init {
        instance = this

        val token = config.getString("discord.token")
        joinMessage = config.getBoolean("discord.joinMessage")
        leaveMessage = config.getBoolean("discord.leaveMessage")
        discordFormat = config.getString("chatFormat.discordFormat")
        webhooks = config.getStringList("discord.webhooks")

        // Only initialize discord stuff if a guild, channel and token are present.
        channels = mutableMapOf() // Channels will always be initialized
        if (token != null) {
            val builder = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
            try {
                builder.setMemberCachePolicy(MemberCachePolicy.ALL)
                builder.setActivity(Activity.playing("Minecraft"))
                jda = builder.build()
                jda!!.awaitReady()
            } catch (e: LoginException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val inChannels = config.getConfigurationSection("discord.channels")!!.getValues(true)
            for ((key, value) in inChannels) {
                if (key == null) break
                val guild = jda!!.getGuildById(key)
                if (guild != null) {
                    val channel = guild.getTextChannelById(value.toString())
                    if (channel != null) channels[guild] = channel
                }
            }
        }
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

        for ((_, value) in channels) {
            value.sendMessage("**" + player.name + "**: " + message).queue()
        }
    }

    fun updateActivity(activity: Activity) {
        if (jda != null) jda!!.presence.activity = activity
    }
}
