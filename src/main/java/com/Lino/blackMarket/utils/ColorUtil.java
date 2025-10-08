package com.Lino.blackMarket.utils;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.+?)</gradient>");

    public static String colorize(String message) {
        if (message == null) return "";
        String formattedMessage = applyGradients(message);
        return ChatColor.translateAlternateColorCodes('&', formattedMessage);
    }

    public static String decolorize(String message) {
        if (message == null) return "";
        return message.replace(ChatColor.COLOR_CHAR, '&');
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

        String formatting = "";
        String cleanText = text;
        while (cleanText.length() >= 2 && cleanText.charAt(0) == '&') {
            char code = cleanText.charAt(1);
            if ("lkomnr".indexOf(code) > -1) {
                formatting += "&" + code;
                cleanText = cleanText.substring(2);
            } else {
                break;
            }
        }

        String textForGradient = ChatColor.stripColor(cleanText);
        int length = textForGradient.length();

        for (int i = 0; i < length; i++) {
            char c = textForGradient.charAt(i);
            if (c == ' ') {
                result.append(' ');
                continue;
            }

            double ratio = length == 1 ?
                    0 : (double) i / (length - 1);
            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * ratio);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * ratio);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * ratio);

            String hex = String.format("#%02x%02x%02x", r, g, b);
            try {
                result.append(ChatColor.of(hex));
            } catch (NoSuchMethodError | IllegalArgumentException e) {
                return text;
            }

            result.append(formatting).append(c);
        }
        return result.toString();
    }

    private static int[] hexToRGB(String hex) {
        hex = hex.replace("#", "");
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }
}