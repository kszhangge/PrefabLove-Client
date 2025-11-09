import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

import net.minecraft.client.main.Main;

public class Start
{
    public static void main(String[] args)
    {
        String[] defaults = new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.12", "--userProperties", "{}", "--width", "1980", "--height", "1080"};

        // If the launcher already provided certain flags (like --width/--height),
        // don't duplicate them — JOptSimple will fail when the same option is
        // supplied more than once. Merge defaults and incoming args, preferring
        // incoming values when a flag is present.
        java.util.List<String> incoming = java.util.Arrays.asList(args);
        java.util.Set<String> skipFlags = new java.util.HashSet<>();
        skipFlags.add("--width");
        skipFlags.add("--height");
        skipFlags.add("--version");
        skipFlags.add("--accessToken");
        skipFlags.add("--assetsDir");
        skipFlags.add("--assetIndex");
        skipFlags.add("--userProperties");

        java.util.List<String> finalArgsList = new java.util.ArrayList<>();
        for (int i = 0; i < defaults.length; i++) {
            String token = defaults[i];
            if (skipFlags.contains(token) && incoming.contains(token)) {
                // skip this flag and its value
                i++; // skip the value as well
                continue;
            }
            finalArgsList.add(token);
        }
        finalArgsList.addAll(incoming);
        Main.main(finalArgsList.toArray(new String[0]));
    }

    public static <T> T[] concat(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
