package de.sean.splugin.paper.events

import de.sean.splugin.bukkit.tasks.AfkPlayerManager
import de.sean.splugin.discord.Discord
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PaperMessageEvent : Listener {
    @EventHandler
    fun chatEvent(event: AsyncChatEvent) {
        val player = event.player
        
        event.renderer { messageAuthor, sourceDisplayName, message, _ ->
            Component.text()
                .append(
                    sourceDisplayName
                        .color(NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg ${messageAuthor.name}"))
                )
                .append(Component.text(" > ").color(NamedTextColor.GRAY))
                .append(message)
                .build()
        }

        /* AFK: We unmark the player AFK when they write a message. */
        AfkPlayerManager.setLastActivity(player.uniqueId, System.currentTimeMillis())
        if (AfkPlayerManager.isAfk(player.uniqueId)) {
            AfkPlayerManager.setAfk(player.uniqueId, false)
            AfkPlayerManager.unmarkPlayerAfk(player)
        }

        val messageString = PlainComponentSerializer.plain().serialize(event.message())
        Discord.instance.sendMessage(messageString, player)
    }
}
