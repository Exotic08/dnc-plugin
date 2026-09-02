package com.tu.dnc.listeners;

import com.tu.dnc.ColorUtil;
import com.tu.dnc.DNCEntry;
import com.tu.dnc.DisplayNameColorPlugin;
import com.tu.dnc.commands.DNCCommand;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * To mau ten nguoi choi ngay trong chat, dua theo DNC dang bat.
 * Dung Paper AsyncChatEvent (Adventure) de tuong thich hex/gradient tot nhat.
 *
 * Priority HIGHEST: chay SAU cac plugin khac (vd UltimateDonutSMP) cung set renderer chat,
 * de dam bao mau ten cua DNC luon la ban cuoi cung duoc ap dung, khong bi ghi de.
 */
public class ChatListener implements Listener {

    private final DisplayNameColorPlugin plugin;
    private final boolean papiEnabled;

    public ChatListener(DisplayNameColorPlugin plugin) {
        this.plugin = plugin;
        this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String active = plugin.getDataManager().getActive(player.getUniqueId());
        if (active == null) return;

        DNCEntry entry = plugin.getDataManager().getEntry(active);
        if (entry == null) return;
        if (!player.hasPermission(entry.getPermissionNode()) && !player.hasPermission("dnc.admin")) return;

        String coloredLegacy = DNCCommand.translateHex(ColorUtil.applyGradient(player.getName(), entry.getColors()));
        Component coloredName = LegacyComponentSerializer.legacySection().deserialize(coloredLegacy);

        // Giu lai rank prefix (vd [VIP]) bang LuckPerms qua PlaceholderAPI, neu co
        String rankPrefixLegacy = "";
        if (papiEnabled) {
            rankPrefixLegacy = PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
        }
        Component rankPrefix = rankPrefixLegacy.isEmpty()
                ? Component.empty()
                : LegacyComponentSerializer.legacyAmpersand().deserialize(
                        rankPrefixLegacy.replace("&#", "#")); // fallback don gian, uu tien mau ten hon

        // Ghi de renderer de thay ten mac dinh bang ten da to mau, giu nguyen phan noi dung tin nhan
        event.renderer((source, sourceDisplayName, message, viewer) ->
                rankPrefix.append(coloredName).append(Component.text(": ")).append(message));
    }
}
