package com.Lino.blackMarket.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("&gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>([^&]+)");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String colorize(String message) {
        if (message == null) return "";

        message = applyGradients(message);
        message = applyHexColors(message);
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    private static String applyGradients(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);

            String gradient = createGradient(text, startHex, endHex);
            matcher.appendReplacement(buffer, gradient);
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        StringBuilder result = new StringBuilder();

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);

        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                result.append(' ');
                continue;
            }

            double ratio = (double) i / (length - 1);

            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * ratio);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * ratio);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * ratio);

            String hex = String.format("#%02x%02x%02x", r, g, b);
            result.append(ChatColor.of(hex)).append(c);
        }

        return result.toString();
    }

    private static int[] hexToRGB(String hex) {
        hex = hex.replace("#", "");

        return new int[] {
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static String applyHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            ChatColor color = ChatColor.of("#" + hex);
            matcher.appendReplacement(buffer, color.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}