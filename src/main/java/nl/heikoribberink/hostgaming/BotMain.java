package nl.heikoribberink.hostgaming;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import nl.heikoribberink.hostgaming.configloader.BotConfigs;

public class BotMain {
	public static void main(String[] args) {
		final String token = args[0], guildId = args[1], channelId = args[2];
		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();
		
		
	}

	public static void start(BotConfigs botConfigs) {
		final String token = botConfigs.getToken(); 
		final long guildId = botConfigs.getGuildId(), channelId = botConfigs.getChannelId();
		DiscordClient client = DiscordClient.create(token);

	}
}
