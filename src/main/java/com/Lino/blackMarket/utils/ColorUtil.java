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
        message = translateAlternateColorCodes(message);

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
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradient));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        StringBuilder result = new StringBuilder();

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);

        int length = text.length();
        boolean lastWasBold = false;
        boolean lastWasItalic = false;
        boolean lastWasUnderline = false;
        boolean lastWasStrike = false;

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (c == '&' && i + 1 < length) {
                char code = text.charAt(i + 1);
                if (code == 'l') {
                    lastWasBold = true;
                    i++;
                    continue;
                } else if (code == 'o') {
                    lastWasItalic = true;
                    i++;
                    continue;
                } else if (code == 'n') {
                    lastWasUnderline = true;
                    i++;
                    continue;
                } else if (code == 'm') {
                    lastWasStrike = true;
                    i++;
                    continue;
                }
            }

            if (c == ' ') {
                result.append(' ');
                continue;
            }

            double ratio = length == 1 ? 0 : (double) i / (length - 1);

            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * ratio);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * ratio);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * ratio);

            String hex = String.format("#%02x%02x%02x", r, g, b);

            try {
                ChatColor color = ChatColor.of(hex);
                result.append(color);
            } catch (NoSuchMethodError e) {
                result.append(translateHexColorCode(hex));
            }

            if (lastWasBold) result.append(ChatColor.BOLD);
            if (lastWasItalic) result.append(ChatColor.ITALIC);
            if (lastWasUnderline) result.append(ChatColor.UNDERLINE);
            if (lastWasStrike) result.append(ChatColor.STRIKETHROUGH);

            result.append(c);
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
            String replacement;

            try {
                ChatColor color = ChatColor.of("#" + hex);
                replacement = color.toString();
            } catch (NoSuchMethodError e) {
                replacement = translateHexColorCode("#" + hex);
            }

            matcher.appendReplacement(buffer, replacement);
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String translateHexColorCode(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        hex = hex.replace("#", "");

        for (char c : hex.toCharArray()) {
            builder.append("§").append(c);
        }

        return builder.toString();
    }

    private static String translateAlternateColorCodes(String message) {
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&') {
                char code = chars[i + 1];
                if ("0123456789abcdefklmnor".indexOf(code) > -1) {
                    chars[i] = '§';
                    chars[i + 1] = Character.toLowerCase(code);
                }
            }
        }

        return new String(chars);
    }
}