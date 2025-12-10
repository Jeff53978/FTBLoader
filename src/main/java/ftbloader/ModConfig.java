package ftbloader;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "ftbloader")
public class ModConfig implements ConfigData {
    public boolean library = true;
    public boolean claims = true;
    public boolean teams = true;

    @ConfigEntry.Gui.CollapsibleObject
    Versions versions = new Versions();

    public static class Versions {
        public String libraryVersion = "ftb-library-fabric-2101.1.28.jar";
        public String claimsVersion = "ftb-chunks-fabric-2101.1.13.jar";
        public String teamsVersion = "ftb-teams-fabric-2101.1.7.jar";
    }
}
