package nl.heikoribberink.hostgaming;

import org.junit.Test;

import nl.heikoribberink.hostgaming.configloader.BotConfigs;

public class ConfigTest {
    
    BotConfigs config = new BotConfigs("src\\test\\java\\nl\\heikoribberink\\hostgaming\\Minecraft.hg.conf");

    @Test
    public void test() {
        System.out.println("token: " + config.getToken());
        System.out.println("channel id: " + config.getChannelId());
        System.out.println("input delay: " + config.getInputDelay());
        System.out.println("max inputs: " + config.getMaxInputs());
        System.out.println("min votes: " + config.getMinVotes());
        System.out.println("host: " + config.getHost());
        System.out.println("event title:" + config.getEventTitle());
        System.out.println("mode: " + config.getMode());
    }
}