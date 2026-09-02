package com.tu.dnc.gui;

import com.tu.dnc.DNCEntry;
import com.tu.dnc.DisplayNameColorPlugin;
import com.tu.dnc.commands.DNCCommand;
import com.tu.dnc.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI chest phan trang, hien tat ca DNC nguoi choi co quyen (hoac tat ca neu la admin xem preview).
 * Bam vao 1 item -> bat DNC do (tu tat cai dang bat truoc, chi 1 cai active cung luc).
 */
public class DNCMenu {

    public static final String TITLE_PREFIX = "§8Display Name Color - Trang ";
    private static final int PAGE_SIZE = 45; // 5 hang dau, hang cuoi danh cho dieu huong
    public static final NamespacedKey KEY_DNC_NAME;

    static {
        // key duoc gan trong onEnable qua plugin instance, tam thoi de null-safe khoi tao khi load class
        KEY_DNC_NAME = new NamespacedKey("displaynamecolor", "dnc_name");
    }

    private final DisplayNameColorPlugin plugin;
    private final Player player;
    private final int page;

    public DNCMenu(DisplayNameColorPlugin plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
    }

    public void open() {
        List<DNCEntry> visible = new ArrayList<>();
        for (DNCEntry e : plugin.getDataManager().getAllEntries()) {
            if (player.hasPermission(e.getPermissionNode()) || player.hasPermission("dnc.admin")) {
                visible.add(e);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(visible.size() / (double) PAGE_SIZE));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + (safePage + 1) + "/" + totalPages);

        int start = safePage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, visible.size());

        String activeName = plugin.getDataManager().getActive(player.getUniqueId());

        for (int i = start; i < end; i++) {
            DNCEntry entry = visible.get(i);
            boolean isActive = activeName != null && activeName.equalsIgnoreCase(entry.getName());
            inv.setItem(i - start, buildItem(entry, isActive));
        }

        // Nut dieu huong o hang cuoi (slot 45-53)
        if (safePage > 0) {
            inv.setItem(45, navItem(Material.ARROW, "§aTrang truoc"));
        }
        if (safePage < totalPages - 1) {
            inv.setItem(53, navItem(Material.ARROW, "§aTrang sau"));
        }
        inv.setItem(49, navItem(Material.BARRIER, "§cTat mau ten (ve mac dinh)"));

        player.openInventory(inv);
    }

    private ItemStack buildItem(DNCEntry entry, boolean isActive) {
        ItemStack item = new ItemStack(entry.getMaterial());
        ItemMeta meta = item.getItemMeta();

        String coloredPreview = DNCCommand.translateHex(ColorUtil.applyGradient(entry.getName(), entry.getColors()));
        meta.setDisplayName(coloredPreview);

        List<String> lore = new ArrayList<>();
        lore.add(isActive ? "§a✔ Dang bat" : "§7✘ Dang tat");
        lore.add("§7Bam de " + (isActive ? "§ctat" : "§abat") + " §7mau nay");
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(KEY_DNC_NAME, PersistentDataType.STRING, entry.getName());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
