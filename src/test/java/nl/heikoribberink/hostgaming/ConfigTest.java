package nl.heikoribberink.hostgaming;

import org.junit.Test;

import nl.heikoribberink.hostgaming.configloader.BotConfigs;

public class ConfigTest {
    
    BotConfigs config = new BotConfigs("src\\test\\java\\nl\\heikoribberink\\hostgaming\\Minecraft.hg.conf");

    @Test
    public void test() {
        String id = config.getChannelId();
        System.out.println(id);
    }
}