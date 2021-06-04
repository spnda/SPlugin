package de.sean.splugin

import de.sean.splugin.bukkit.events.*
import de.sean.splugin.bukkit.tasks.AfkChecker
import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.bukkit.tasks.DiscordActivityUpdater
import de.sean.splugin.discord.DefaultDiscordEventHandler
import de.sean.splugin.discord.Discord
import de.sean.splugin.util.PluginConfig
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager

class SPlugin : org.bukkit.plugin.java.JavaPlugin() {
    override fun onEnable() {
        /* Config */
        this.also { instance = it }.saveDefaultConfig()
        val config: FileConfiguration = instance.config
        PluginConfig(config)

        AfkPlayerManager.init(config)

        if (config.getBoolean("features.afk")) Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(this, AfkChecker(), 0L, 20L)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, DiscordActivityUpdater(config), 0L, 20L * 60L * 5L) // 20L * 60L * 5L, 20 ticks * 60 seconds * 5 => 5 minutes in ticks

        /* Events & Commands */
        registerEvents(Bukkit.getServer().pluginManager)
        registerCommands()

        /* Discord */
        Discord(config)
        Discord.instance.addEventListener(DefaultDiscordEventHandler())
    }

    override fun onDisable() {
        Discord.instance.close()
        Bukkit.getScheduler().cancelTasks(this)
        super.onDisable()
    }

    private fun registerEvents(pm: PluginManager) {
        registerEvent(pm, DeathEvent()) // Handles every player death event
        registerEvent(pm, DismountEvent()) // Handles every entity dismount
        registerEvent(pm, ExplodeEvent()) // Handles every explosion in the world
        registerEvent(pm, InteractEvent()) // Handles every block interaction by a player
        registerEvent(pm, JoinEvent()) // Handles every user join event
        registerEvent(pm, LeaveEvent()) // Handles every user leave event
        registerEvent(pm, MessageEvent()) // Handles every chat message event
        registerEvent(pm, MoveEvent()) // Handles every move of a player
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }

    private fun registerCommands() {
        // getCommand("lock")?.setExecutor(LockExecutor())
    }

    companion object {
        lateinit var instance: SPlugin
    }
}
