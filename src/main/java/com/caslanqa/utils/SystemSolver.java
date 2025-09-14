package com.caslanqa.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemSolver {

    public static String resolvePlaceholders(String path) {
        if (path == null) return null;

        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(path);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String propName = matcher.group(1);
            String replacement = System.getProperty(propName);
            if (replacement == null) {
                replacement = matcher.group(0); // bulunamazsa olduğu gibi bırak
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

}
