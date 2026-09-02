package com.tu.dnc.listeners;

import com.tu.dnc.ColorUtil;
import com.tu.dnc.DNCEntry;
import com.tu.dnc.DisplayNameColorPlugin;
import com.tu.dnc.commands.DNCCommand;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * To mau ten nguoi choi ngay trong chat, dua theo DNC dang bat.
 * Dung Paper AsyncChatEvent (Adventure) de tuong thich hex/gradient tot nhat.
 */
public class ChatListener implements Listener {

    private final DisplayNameColorPlugin plugin;

    public ChatListener(DisplayNameColorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String active = plugin.getDataManager().getActive(player.getUniqueId());
        if (active == null) return;

        DNCEntry entry = plugin.getDataManager().getEntry(active);
        if (entry == null) return;
        if (!player.hasPermission(entry.getPermissionNode()) && !player.hasPermission("dnc.admin")) return;

        String coloredLegacy = DNCCommand.translateHex(ColorUtil.applyGradient(player.getName(), entry.getColors()));
        Component coloredName = LegacyComponentSerializer.legacySection().deserialize(coloredLegacy);

        // Ghi de renderer de thay ten mac dinh bang ten da to mau, giu nguyen phan noi dung tin nhan
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component prefix = coloredName;
            return prefix.append(Component.text(": ")).append(message);
        });
    }
}
