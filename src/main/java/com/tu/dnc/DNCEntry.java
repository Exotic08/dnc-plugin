package com.tu.dnc;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Dai dien cho 1 "Display Name Color" (DNC) - vi du: vip, admin, rainbow...
 * Chua danh sach ma mau hex de tao gradient trai deu tren ten nguoi choi.
 */
public class DNCEntry {

    private String name;
    private Material material;
    private final List<String> colors; // danh sach hex, vi du "#FA1505"

    public DNCEntry(String name, Material material, List<String> colors) {
        this.name = name;
        this.material = material;
        this.colors = new ArrayList<>(colors);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> newColors) {
        colors.clear();
        colors.addAll(newColors);
    }

    /** Permission node rieng cho DNC nay, dung de kiem tra quyen su dung. */
    public String getPermissionNode() {
        return "dnc.use." + name.toLowerCase();
    }
}
