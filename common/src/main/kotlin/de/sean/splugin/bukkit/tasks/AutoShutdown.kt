package de.sean.splugin.bukkit.tasks

import de.sean.splugin.util.Messages
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.util.*

object AutoShutdown {
    /* Message delays in seconds */
    private val messageDelays = listOf<Long>(30 * 60, 10 * 60, 5 * 60, 60, 30, 10)

    fun registerTasks(plugin: JavaPlugin) {
        val c: Calendar = Calendar.getInstance()
        // We won't add any hours/minutes to this, as we want to shut down at midnight
        c.add(Calendar.DAY_OF_MONTH, 1)

        val delay = c.timeInMillis - System.currentTimeMillis()
        val delayInTicks: Long = (delay / 1000) * 20

        val initialDelayIndex = messageDelays.indexOfFirst { it < (delay / 1000) }
        if (initialDelayIndex != -1) {
            val initialDelay: Long = messageDelays[initialDelayIndex] * 20
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, AutoShutdownMessager(plugin, initialDelayIndex), delayInTicks - initialDelay)

            // We won't add any hours/minutes to this, as we want to shut down at midnight
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, AutoShutdownTrigger(), delayInTicks);
        }
    }

    class AutoShutdownMessager(private val plugin: JavaPlugin, private val delayIndex: Int) : Runnable {
        private fun timeString(): String {
            val config = plugin.config
            val message = Messages.getRandomMessage(config.getList("autoshutdown.message"))

            val remainingTime = messageDelays[delayIndex].toFloat()
            return message.replace("[time]",
                if (remainingTime <= 60) "${remainingTime}s" else "${"%.0f".format(remainingTime / 60)}min")
        }

        override fun run() {
            Bukkit.getServer().broadcastMessage(timeString())
            if (delayIndex < messageDelays.size - 1) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, AutoShutdownMessager(plugin, delayIndex + 1), messageDelays[delayIndex + 1] * 20)
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, AutoShutdownTrigger(), messageDelays[delayIndex] * 20);
            }
        }
    }

    class AutoShutdownTrigger : Runnable {
        override fun run() {
            val pb = ProcessBuilder("shutdown", "s", "t", "60") /* Shutdown takes parameters in seconds */
            try {
                pb.start()
            } catch (_: IOException) {}
            Bukkit.shutdown();
        }
    }
}
