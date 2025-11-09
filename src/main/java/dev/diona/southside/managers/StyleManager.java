package dev.diona.southside.managers;

import dev.diona.southside.gui.style.ClientStyle;
import dev.diona.southside.gui.style.styles.DarkStyle;

public class StyleManager {
//    private String clientStyle;
//    private final HashMap<String, ClientStyle> styles = new HashMap<>();
//
//    public StyleManager() {
//        this.register(new DarkStyle());
//
//        JsonObject clientInfo = PrefabLove.fileManager.readFileData(FileManager.CLIENT_INFO).getAsJsonObject();
//        if (clientInfo.get("style") == null) {
//            clientInfo.addProperty("style", "Dark");
//            PrefabLove.fileManager.writeData(FileManager.CLIENT_INFO, clientInfo);
//        }
//        clientStyle = clientInfo.get("style").getAsString();
//    }
//
//    private void register(ClientStyle clientStyle) {
//        styles.put(clientStyle.name, clientStyle);
//    }

    public static ClientStyle style = new DarkStyle();

    public ClientStyle getStyle() {
        return style;
    }

//    public void setStyle(String clientStyle) {
//        if (styles.containsKey(clientStyle)) {
//            this.clientStyle = clientStyle;
//        }
//    }
}