package de.sean.splugin

import de.sean.splugin.bukkit.events.*
import de.sean.splugin.bukkit.tasks.AfkChecker
import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.bukkit.tasks.DiscordActivityUpdater
import de.sean.splugin.discord.Discord
import de.sean.splugin.discord.PaperDiscordEventHandler
import de.sean.splugin.paper.events.*
import de.sean.splugin.util.PluginConfig
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class SPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: SPlugin
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()
        /* Config */
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
        Discord.instance.addEventListener(PaperDiscordEventHandler())

        super.onEnable()
    }

    override fun onDisable() {
        Discord.instance.close()
        Bukkit.getScheduler().cancelTasks(this)
        super.onDisable()
    }

    private fun registerEvents(pm: PluginManager) {
        // register all events
        registerEvent(pm, DeathEvent())
        registerEvent(pm, DismountEvent())
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, PaperInteractEvent())
        registerEvent(pm, PaperJoinEvent(this))
        registerEvent(pm, LeaveEvent())
        registerEvent(pm, PaperMessageEvent())
        registerEvent(pm, MoveEvent())
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }

    private fun registerCommands() {
        // register any commands we want to use
    }
}
