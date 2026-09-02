package com.tu.dnc.placeholder;

import com.tu.dnc.ColorUtil;
import com.tu.dnc.DNCEntry;
import com.tu.dnc.DisplayNameColorPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * Cung cap placeholder cho TAB va cac plugin khac doc mau ten dang bat cua player:
 *   %dnc_color%   -> ten player da duoc to mau gradient (dang &#RRGGBB), rong neu khong bat gi
 *   %dnc_name%    -> ten cua DNC dang bat (vi du "vip"), rong neu khong bat gi
 *
 * Cach dung trong TAB config.yml:
 *   tablist-name-formatting: '%luckperms_prefix%%dnc_color%'
 * (Neu %dnc_color% rong, TAB nen fallback ve %player% - xem huong dan kem theo)
 */
public class DNCExpansion extends PlaceholderExpansion {

    private final DisplayNameColorPlugin plugin;

    public DNCExpansion(DisplayNameColorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "dnc";
    }

    @Override
    public String getAuthor() {
        return "Tu";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || player.getName() == null) return "";

        String active = plugin.getDataManager().getActive(player.getUniqueId());
        DNCEntry entry = (active != null) ? plugin.getDataManager().getEntry(active) : null;

        if (params.equalsIgnoreCase("name")) {
            return entry != null ? entry.getName() : "";
        }

        if (params.equalsIgnoreCase("color")) {
            if (entry == null) {
                return player.getName(); // fallback: khong bat gi -> tra ve ten thuong
            }
            // Kiem tra quyen (phong khi bi revoke nhung data.yml chua kip cap nhat)
            if (player.isOnline() && player.getPlayer() != null) {
                if (!player.getPlayer().hasPermission(entry.getPermissionNode())
                        && !player.getPlayer().hasPermission("dnc.admin")) {
                    return player.getName();
                }
            }
            return ColorUtil.applyGradientMiniMessage(player.getName(), entry.getColors());
        }

        return null;
    }
}
