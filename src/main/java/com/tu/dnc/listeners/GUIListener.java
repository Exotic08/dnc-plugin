package com.tu.dnc.listeners;

import com.tu.dnc.DisplayNameColorPlugin;
import com.tu.dnc.gui.DNCMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {

    private final DisplayNameColorPlugin plugin;

    public GUIListener(DisplayNameColorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title == null || !title.startsWith(DNCMenu.TITLE_PREFIX)) return;

        event.setCancelled(true); // khong cho lay item ra khoi GUI

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        int currentPage = parsePage(title);

        switch (clicked.getType()) {
            case ARROW -> {
                ItemMeta meta = clicked.getItemMeta();
                boolean next = meta != null && meta.getDisplayName().contains("sau");
                new DNCMenu(plugin, player, next ? currentPage + 1 : currentPage - 1).open();
                return;
            }
            case BARRIER -> {
                plugin.getDataManager().setActive(player.getUniqueId(), null);
                player.sendMessage("§eDa tat mau ten, ve mac dinh.");
                new DNCMenu(plugin, player, currentPage).open();
                return;
            }
            default -> {
            }
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;
        String dncName = meta.getPersistentDataContainer().get(DNCMenu.KEY_DNC_NAME, PersistentDataType.STRING);
        if (dncName == null) return;

        var entry = plugin.getDataManager().getEntry(dncName);
        if (entry == null) return;

        if (!player.hasPermission(entry.getPermissionNode()) && !player.hasPermission("dnc.admin")) {
            player.sendMessage("§cBan khong co quyen dung mau nay.");
            return;
        }

        String active = plugin.getDataManager().getActive(player.getUniqueId());
        if (active != null && active.equalsIgnoreCase(dncName)) {
            // dang bat -> tat
            plugin.getDataManager().setActive(player.getUniqueId(), null);
            player.sendMessage("§eDa tat mau ten '" + dncName + "'.");
        } else {
            // bat cai moi, tu dong tat cai cu (setActive ghi de, khong can lam gi them)
            plugin.getDataManager().setActive(player.getUniqueId(), dncName);
            player.sendMessage("§aDa bat mau ten '" + dncName + "'.");
        }

        new DNCMenu(plugin, player, currentPage).open();
    }

    private int parsePage(String title) {
        try {
            String afterPrefix = title.substring(DNCMenu.TITLE_PREFIX.length());
            String pageStr = afterPrefix.split("/")[0].trim();
            return Integer.parseInt(pageStr) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
