package com.tu.dnc;

import java.awt.Color;
import java.util.List;

/**
 * Xu ly noi suy gradient nhieu mau tren 1 chuoi ky tu bat ky (ten nguoi choi).
 * Neu so mau nhieu hon so ky tu, tu dong chi lay mau DAU va mau CUOI (theo yeu cau).
 */
public class ColorUtil {

    /**
     * Tra ve chuoi da duoc to mau bang ma legacy hex "&#RRGGBB" cho tung ky tu,
     * trai deu gradient tu mau dau den mau cuoi (qua cac mau trung gian neu co).
     *
     * @param text   chuoi can to mau (vi du ten nguoi choi)
     * @param colors danh sach hex "#RRGGBB", toi thieu 1 mau
     */
    public static String applyGradient(String text, List<String> colors) {
        if (text == null || text.isEmpty() || colors == null || colors.isEmpty()) {
            return text;
        }

        // Neu chi co 1 mau, to nguyen ca chuoi 1 mau, khong can noi suy.
        if (colors.size() == 1) {
            return colors.get(0) + text;
        }

        int len = text.length();

        // Yeu cau (4): neu so mau nhieu hon so ky tu, chi lay mau DAU va mau CUOI.
        List<String> effectiveColors = colors;
        if (colors.size() > len) {
            effectiveColors = List.of(colors.get(0), colors.get(colors.size() - 1));
        }

        StringBuilder result = new StringBuilder();
        int segments = effectiveColors.size() - 1;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);

            // Vi tri tuong doi (0.0 -> 1.0) cua ky tu nay trong toan bo chuoi
            double position = (len == 1) ? 0.0 : (double) i / (len - 1);

            // Xac dinh dang o doan gradient thu may
            double scaled = position * segments;
            int segIndex = Math.min((int) scaled, segments - 1);
            double localT = scaled - segIndex;

            Color c1 = hexToColor(effectiveColors.get(segIndex));
            Color c2 = hexToColor(effectiveColors.get(segIndex + 1));

            int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * localT);
            int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * localT);
            int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * localT);

            result.append(String.format("&#%02X%02X%02X", r, g, b)).append(c);
        }

        return result.toString();
    }

    /** Tra ve dang MiniMessage <#RRGGBB>c</#RRGGBB> cho tung ky tu (dung cho TAB/adventure). */
    public static String applyGradientMiniMessage(String text, List<String> colors) {
        String legacy = applyGradient(text, colors);
        // Legacy da co dang &#RRGGBBc&#RRGGBBc... du dung truc tiep cho hau het plugin (TAB, chat)
        return legacy;
    }

    private static Color hexToColor(String hex) {
        String clean = hex.replace("#", "").trim();
        if (clean.length() != 6) {
            return Color.WHITE;
        }
        int r = Integer.parseInt(clean.substring(0, 2), 16);
        int g = Integer.parseInt(clean.substring(2, 4), 16);
        int b = Integer.parseInt(clean.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    public static boolean isValidHex(String hex) {
        if (hex == null) return false;
        String clean = hex.replace("#", "").trim();
        return clean.matches("[0-9a-fA-F]{6}");
    }
}
