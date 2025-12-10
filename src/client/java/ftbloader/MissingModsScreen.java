package ftbloader;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MissingModsScreen extends Screen {
    private Component titleText = Component.literal("You're missing FTB mods!");
    private Component subtitleText = Component.literal("Click to download:");
    private boolean isDownloading = false;
    private String downloadStatus = "";
    private boolean awaitingConfirmation = false;

    public MissingModsScreen() {
        super(Component.literal("Missing Mods"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topTextY = 10;
        int buttonWidth = 200;
        int buttonHeight = 20;

        // Buttons for individual mods
        int modButtonY = topTextY + 45;
        int modButtonSpacing = 25;

        int buttons = 0;

        for (Map.Entry<String, String> entry : getMods().entrySet()) {
            addButtonForMod(entry.getKey(), entry.getValue(), centerX - buttonWidth / 2, modButtonY + modButtonSpacing * buttons);
            buttons++;
        }

        // Button for downloading all mods
        int downloadAllButtonY = modButtonY + modButtonSpacing * buttons + 10;
        int gap = 8;

        addRenderableWidget(
                Button.builder(
                        Component.literal("Auto Download & Restart"),
                        button -> downloadAllMods()
                ).bounds(
                        centerX - buttonWidth / 2,
                        downloadAllButtonY,
                        buttonWidth / 2 - gap / 2,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.literal("Ignore"),
                        button -> exit()
                ).bounds(
                        centerX + gap / 2,
                        downloadAllButtonY,
                        buttonWidth / 2 - gap / 2,
                        buttonHeight
                ).build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        super.render(graphics, mouseX, mouseY, delta);

        // Render missing mods text
        int centerX = this.width / 2;
        int titleY = 10;
        int subtitleY = titleY + 15;

        graphics.drawCenteredString(font, titleText, centerX, titleY, 16777215);
        graphics.drawCenteredString(font, subtitleText, centerX, subtitleY, 16777215);

        // Show download status
        if (isDownloading && !downloadStatus.isEmpty()) {
            graphics.drawCenteredString(font, Component.literal(downloadStatus), centerX, this.height - 30, 0x00FF00);
        }
    }

    private Map<String, String> getMods() {
        // This map has to be generated at runtime, otherwise the config won't work.
        Map<String, String> mods = new HashMap<>();

        if (FTBLoaderClient.CONFIG.library && !FabricLoader.getInstance().isModLoaded("ftblibrary"))
            mods.put("FTB Library", "https://mediafilez.forgecdn.net/files/7260/615/" + FTBLoaderClient.CONFIG.versions.libraryVersion);

        if (FTBLoaderClient.CONFIG.claims && !FabricLoader.getInstance().isModLoaded("ftbchunks"))
            mods.put("FTB Chunks", "https://mediafilez.forgecdn.net/files/7157/144/" + FTBLoaderClient.CONFIG.versions.claimsVersion);

        if (FTBLoaderClient.CONFIG.teams && !FabricLoader.getInstance().isModLoaded("ftbteams"))
            mods.put("FTB Teams", "https://mediafilez.forgecdn.net/files/7209/617/" + FTBLoaderClient.CONFIG.versions.teamsVersion);

        return mods;
    }

    private void addButtonForMod(String modName, String downloadLink, int x, int y) {
        int buttonWidth = 200;
        int buttonHeight = 20;

        addRenderableWidget(new Button.Builder(Component.literal(modName), button -> openWebPage(downloadLink)).bounds(x, y, buttonWidth, buttonHeight).build());
    }

    private void exit() {
        FTBLoaderClient.IGNORED = true;

        Minecraft.getInstance().setScreen(new TitleScreen());
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void openWebPage(String url) {
        try {
            Util.getPlatform().openUri(new URI(url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void downloadAllMods() {
        if (isDownloading || awaitingConfirmation) return;

        // Ask for confirmation before starting downloads
        awaitingConfirmation = true;
        Component title = Component.literal("Download missing mods");
        Component message = Component.literal("This will download mods into your instance mods folder and restart Minecraft. Continue?");

        Minecraft.getInstance().setScreen(new ConfirmScreen(confirmed -> {
            // Return to this screen
            Minecraft.getInstance().setScreen(this);
            awaitingConfirmation = false;

            if (confirmed) {
                // Kick off the download thread
                startDownloadsThread();
            }
        }, title, message));
    }

    private void startDownloadsThread() {
        isDownloading = true;
        titleText = Component.literal("Downloading mods...");
        subtitleText = Component.literal("Please wait, do not close the game");

        new Thread(() -> {
            try {
                Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
                modsDir.toFile().mkdirs();

                int total = getMods().size();
                int current = 0;

                for (Map.Entry<String, String> entry : getMods().entrySet()) {
                    current++;
                    downloadStatus = "Downloading " + entry.getKey() + " (" + current + "/" + total + ")";

                    try {
                        downloadMod(entry.getValue(), modsDir.toFile(), entry.getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                        downloadStatus = "Failed to download " + entry.getKey();
                        Thread.sleep(2000);
                    }
                }

                downloadStatus = "Download complete! Restarting...";
                Thread.sleep(1000);

                // Restart the game
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().stop());

            } catch (Exception e) {
                e.printStackTrace();
                downloadStatus = "Download failed: " + e.getMessage();
                isDownloading = false;
            }
        }).start();
    }

    private void downloadMod(String downloadUrl, File modsDir, String modName) throws Exception {
        // Resolve final download URL and filename (follow redirects using HEAD first)
        System.err.println(downloadUrl);
        URI initialUri = new URI(downloadUrl);
        URL initialUrl = initialUri.toURL();
        HttpURLConnection headConn = (HttpURLConnection) initialUrl.openConnection();
        headConn.setInstanceFollowRedirects(true);
        headConn.setRequestMethod("HEAD");
        headConn.setRequestProperty("User-Agent", "Mozilla/5.0");
        headConn.connect();

        // After following redirects, get the final URL and headers
        URL finalUrl = headConn.getURL();
        String contentDisposition = headConn.getHeaderField("Content-Disposition");

        // Determine filename: prefer Content-Disposition, otherwise derive from final URL
        String fileName = modName.replace(" ", "-") + ".jar";
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            String cd = contentDisposition;
            int idx = cd.indexOf("filename=");
            fileName = cd.substring(idx + 9).replace("\"", "");
            // strip path parts if any
            if (fileName.contains("/")) fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        } else {
            String path = finalUrl.getPath();
            if (path != null && path.length() > 0) {
                String last = path.substring(path.lastIndexOf('/') + 1);
                if (!last.isEmpty()) {
                    // remove query params if present
                    int q = last.indexOf('?');
                    if (q > 0) last = last.substring(0, q);
                    fileName = last;
                }
            }
        }

        headConn.disconnect();

        // Now open connection to the final URL for actual download (GET)
        HttpURLConnection connection = (HttpURLConnection) finalUrl.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.connect();

        File outputFile = new File(modsDir, fileName);

        // Download the file
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        connection.disconnect();
    }
}
