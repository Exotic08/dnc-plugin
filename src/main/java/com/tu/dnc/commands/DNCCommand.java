package com.tu.dnc.commands;

import com.tu.dnc.ColorUtil;
import com.tu.dnc.DNCEntry;
import com.tu.dnc.DisplayNameColorPlugin;
import com.tu.dnc.gui.DNCMenu;
import com.tu.dnc.storage.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DNCCommand implements CommandExecutor, TabCompleter {

    private final DisplayNameColorPlugin plugin;

    public DNCCommand(DisplayNameColorPlugin plugin) {
        this.plugin = plugin;
    }

    private DataManager dm() {
        return plugin.getDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /dnc khong kem gi -> mo GUI (chi player moi mo duoc)
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§cLenh nay chi dung duoc trong game.");
                return true;
            }
            new DNCMenu(plugin, p, 0).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "test" -> handleTest(sender, args);
            case "setpermission" -> handleSetPermission(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "edit" -> handleEdit(sender, args);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(usage());
        }
        return true;
    }

    private String usage() {
        return """
                §e/dnc create <ten> <material> <mau1,mau2,...>
                §e/dnc remove <ten>
                §e/dnc test <ten>
                §e/dnc setpermission <ten> <rank_lp> <true/false>
                §e/dnc list
                §e/dnc info <ten>
                §e/dnc edit name/material/color <ten> <gia_tri_moi>
                §e/dnc reload
                §e/dnc §7- mo menu chon mau""";
    }

    private boolean isAdmin(CommandSender s) {
        if (s.hasPermission("dnc.admin")) return true;
        s.sendMessage("§cBan khong co quyen dnc.admin de dung lenh nay.");
        return false;
    }

    // /dnc create <ten> <material> <mau1,mau2,...>
    private void handleCreate(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) return;
        if (args.length < 4) {
            sender.sendMessage("§cCu phap: /dnc create <ten> <material> <mau1,mau2,...>");
            return;
        }
        String name = args[1];
        String matName = args[2];
        String colorArg = args[3];

        Material material;
        try {
            material = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cMaterial khong hop le: " + matName);
            return;
        }

        List<String> colors = Arrays.stream(colorArg.split(","))
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toList());

        if (colors.isEmpty()) {
            sender.sendMessage("§cCan it nhat 1 ma mau hex, vi du: #FA1505");
            return;
        }
        for (String c : colors) {
            if (!ColorUtil.isValidHex(c)) {
                sender.sendMessage("§cMa mau khong hop le: " + c + " §7(dung dang #RRGGBB)");
                return;
            }
        }

        if (dm().getEntry(name) != null) {
            sender.sendMessage("§cDNC ten '" + name + "' da ton tai. Dung /dnc edit de sua.");
            return;
        }

        dm().createEntry(name, material, colors);
        sender.sendMessage("§aDa tao DNC '" + name + "' voi " + colors.size() + " mau. Permission mac dinh: false cho moi rank.");
    }

    // /dnc remove <ten>
    private void handleRemove(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) return;
        if (args.length < 2) {
            sender.sendMessage("§cCu phap: /dnc remove <ten>");
            return;
        }
        boolean ok = dm().removeEntry(args[1]);
        sender.sendMessage(ok ? "§aDa xoa DNC '" + args[1] + "'." : "§cKhong tim thay DNC '" + args[1] + "'.");
    }

    // /dnc test <ten>
    private void handleTest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cLenh nay chi dung duoc trong game.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§cCu phap: /dnc test <ten>");
            return;
        }
        DNCEntry entry = dm().getEntry(args[1]);
        if (entry == null) {
            sender.sendMessage("§cKhong tim thay DNC '" + args[1] + "'.");
            return;
        }
        String colored = ColorUtil.applyGradient(p.getName(), entry.getColors());
        p.sendMessage(translateHex(colored) + " §7(preview cua DNC '" + entry.getName() + "')");
    }

    // /dnc setpermission <ten> <rank_lp> <true/false>
    private void handleSetPermission(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) return;
        if (args.length < 4) {
            sender.sendMessage("§cCu phap: /dnc setpermission <ten> <rank_lp> <true/false>");
            return;
        }
        String name = args[1];
        String rank = args[2];
        String value = args[3].toLowerCase();

        if (!value.equals("true") && !value.equals("false")) {
            sender.sendMessage("§cGia tri phai la true hoac false.");
            return;
        }

        DNCEntry entry = dm().getEntry(name);
        if (entry == null) {
            sender.sendMessage("§cKhong tim thay DNC '" + name + "'.");
            return;
        }

        // Goi lenh LuckPerms qua console de gan permission - khong can LuckPerms API lam dependency
        String node = entry.getPermissionNode();
        String cmd = "lp group " + rank + " permission set " + node + " " + value;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        sender.sendMessage("§aDa chay: §7/" + cmd);

        // Neu tat quyen -> tu dong tat DNC dang bat cua nhung player thuoc rank do dang online
        if (value.equals("false")) {
            for (UUID uuid : new ArrayList<>(dm().getAllActiveOfEntry(name))) {
                Player online = Bukkit.getPlayer(uuid);
                if (online != null && !online.hasPermission(node)) {
                    dm().setActive(uuid, null);
                    online.sendMessage("§cMau ten '" + name + "' da bi tat quyen su dung, tu dong chuyen ve mac dinh.");
                }
            }
        }
    }

    // /dnc list
    private void handleList(CommandSender sender) {
        Collection<DNCEntry> all = dm().getAllEntries();
        if (all.isEmpty()) {
            sender.sendMessage("§7Chua co DNC nao duoc tao.");
            return;
        }
        sender.sendMessage("§e=== Danh sach DNC (" + all.size() + ") ===");
        for (DNCEntry e : all) {
            String preview = translateHex(ColorUtil.applyGradient(e.getName(), e.getColors()));
            sender.sendMessage("§7- " + preview + " §7(" + e.getMaterial().name() + ", " + e.getColors().size() + " mau)");
        }
    }

    // /dnc info <ten>
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCu phap: /dnc info <ten>");
            return;
        }
        DNCEntry e = dm().getEntry(args[1]);
        if (e == null) {
            sender.sendMessage("§cKhong tim thay DNC '" + args[1] + "'.");
            return;
        }
        sender.sendMessage("§e=== DNC: " + e.getName() + " ===");
        sender.sendMessage("§7Material: §f" + e.getMaterial().name());
        sender.sendMessage("§7Permission node: §f" + e.getPermissionNode());
        sender.sendMessage("§7Mau: §f" + String.join(", ", e.getColors()));
        sender.sendMessage("§7Preview: " + translateHex(ColorUtil.applyGradient("PreviewName", e.getColors())));
    }

    // /dnc edit name/material/color <ten> <gia_tri_moi>
    private void handleEdit(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) return;
        if (args.length < 4) {
            sender.sendMessage("§cCu phap: /dnc edit name/material/color <ten> <gia_tri_moi>");
            return;
        }
        String field = args[1].toLowerCase();
        String name = args[2];
        String newValue = args[3];

        DNCEntry e = dm().getEntry(name);
        if (e == null) {
            sender.sendMessage("§cKhong tim thay DNC '" + name + "'.");
            return;
        }

        switch (field) {
            case "name" -> {
                dm().renameEntry(name, newValue);
                sender.sendMessage("§aDa doi ten '" + name + "' -> '" + newValue + "'.");
            }
            case "material" -> {
                try {
                    Material m = Material.valueOf(newValue.toUpperCase());
                    e.setMaterial(m);
                    dm().saveConfig();
                    sender.sendMessage("§aDa doi material cua '" + name + "' -> " + m.name());
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage("§cMaterial khong hop le: " + newValue);
                }
            }
            case "color" -> {
                List<String> colors = Arrays.stream(newValue.split(","))
                        .map(String::trim).filter(c -> !c.isEmpty()).collect(Collectors.toList());
                for (String c : colors) {
                    if (!ColorUtil.isValidHex(c)) {
                        sender.sendMessage("§cMa mau khong hop le: " + c);
                        return;
                    }
                }
                e.setColors(colors);
                dm().saveConfig();
                sender.sendMessage("§aDa cap nhat mau cua '" + name + "'.");
            }
            default -> sender.sendMessage("§cField phai la: name, material, hoac color");
        }
    }

    private void handleReload(CommandSender sender) {
        if (!isAdmin(sender)) return;
        dm().reload();
        sender.sendMessage("§aDa reload DisplayNameColor config.");
    }

    /** Chuyen &#RRGGBB thanh ma mau that su hien thi duoc trong chat (Bukkit legacy hex). */
    public static String translateHex(String input) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
                input.replaceAll("&#([0-9a-fA-F]{6})", "&x&$1")
                        .replaceAll("&x&([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])",
                                "&x&$1&$2&$3&$4&$5&$6"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("create", "remove", "test", "setpermission", "list", "info", "edit", "reload"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("test")
                || args[0].equalsIgnoreCase("setpermission") || args[0].equalsIgnoreCase("info"))) {
            return filter(dm().getAllEntries().stream().map(DNCEntry::getName).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            return filter(List.of("name", "material", "color"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            return filter(dm().getAllEntries().stream().map(DNCEntry::getName).collect(Collectors.toList()), args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("setpermission")) {
            return filter(List.of("true", "false"), args[3]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream().filter(o -> o.toLowerCase().startsWith(input.toLowerCase())).collect(Collectors.toList());
    }
}
