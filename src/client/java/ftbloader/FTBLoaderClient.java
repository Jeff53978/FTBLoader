package ftbloader;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class FTBLoaderClient implements ClientModInitializer, ModMenuApi {
    public static final String MOD_ID = "ftbloader";

    public static ModConfig CONFIG;
    public static boolean IGNORED = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        // Register screen event to show missing mods screen
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen && areDependenciesMissing() && !IGNORED) {
                Minecraft.getInstance().setScreen(new MissingModsScreen());
            }
        });
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }

    private boolean areDependenciesMissing() {
        return (!FabricLoader.getInstance().isModLoaded("ftblibrary") && CONFIG.library)
            || (!FabricLoader.getInstance().isModLoaded("ftbchunks") && CONFIG.claims)
            || (!FabricLoader.getInstance().isModLoaded("ftbteams") && CONFIG.teams);
    }
}
