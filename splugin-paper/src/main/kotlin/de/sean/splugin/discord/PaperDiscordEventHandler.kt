package de.sean.splugin.discord

import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.util.regex.Pattern

class PaperDiscordEventHandler : DefaultDiscordEventHandler() {
    companion object {
        private val discordMainColour = TextColor.color(0x72, 0x79, 0xDA)
    }

    override fun broadcastMessage(message: Message, channel: GuildChannel) {
        /* Iterate over all mentioned users, channels, roles and emotes and
         * replace their mentions with */
        val author = message.guild.getMember(message.author) ?: return
        var component: Component = Component
            .text(message.contentRaw)
            .color(NamedTextColor.WHITE)
        for (mentionedUser in message.mentionedUsers) {
            val member = message.guild.getMember(mentionedUser) ?: continue
            component = component.replaceText {
                it.match("<@!${Pattern.quote(mentionedUser.id)}>")
                it.replacement(
                    Component.text("@${member.effectiveName}").color(TextColor.color(member.colorRaw))
                )
            }
        }
        for (mentionedChannel in message.mentionedChannels) {
            component = component.replaceText {
                it.match("<#${mentionedChannel.id}>")
                it.replacement(
                    Component.text("#${mentionedChannel.name}").color(discordMainColour)
                )
            }
        }
        for (mentionedRole in message.mentionedRoles) {
            component = component.replaceText {
                it.match("<@&${mentionedRole.id}>")
                it.replacement(
                    Component.text("@${mentionedRole.name}").color(TextColor.color(mentionedRole.colorRaw))
                )
            }
        }
        Bukkit.getServer().sendMessage(
            Component
                .text(author.effectiveName)
                .color(TextColor.color(author.colorRaw))
                .append(Component.text(": ").color(NamedTextColor.WHITE))
                .append(component)
        )
    }
}
