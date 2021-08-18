package nl.heikoribberink.hostgaming;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;

public class BotMain {
	public static void main(String[] args) {
		final String token = args[0];
		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();
		
		
		
	}
}
