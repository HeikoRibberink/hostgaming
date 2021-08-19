package nl.heikoribberink.hostgaming;

import org.junit.Test;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;

public class ReactionTest {
	// Get emojis from https://emojipedia.org/thumbs-up/
	@Test
	public void reactionEmojiCreationTest() {
		final String token = "ODc3NDY3ODAzMzkxNzY2NTQ5.YRzDkg.lHu3MzOtFTN3peJdgGZ5astta9s";
		final long guildId = 750630471855112203l;
		final long channelId = 806105311344853063l;
		final ReactionEmoji emoji = ReactionEmoji.unicode("👍");
		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();
		Guild guild = gateway.getGuildById(Snowflake.of(guildId)).block();
		MessageChannel channel = (MessageChannel) guild.getChannelById(Snowflake.of(channelId)).block();
		Message msg = channel.createMessage("Reactions Here.").block();
		msg.addReaction(emoji).subscribe(ctx -> {
			System.out.println("Succesfully added emoji.");
			gateway.logout().block();
		}, error -> {
			error.printStackTrace();
			gateway.logout().block();
		});
		// msg.getReactions().stream().filter(ctx -> ctx.getEmoji())
		gateway.onDisconnect().subscribe(ctx -> {
			System.out.println("Disconnected.");
		}, error -> error.printStackTrace());
	}

	@Test
	public void reactionMessage() {
		final String token = "ODc3NDY3ODAzMzkxNzY2NTQ5.YRzDkg.lHu3MzOtFTN3peJdgGZ5astta9s";
		final long guildId = 750630471855112203l;
		final long channelId = 806105311344853063l;
		final String thumbsup = "👍";
		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();
		System.out.println("Logged in.");
		gateway.on(ReactionAddEvent.class).filter(event -> {
			return event.getGuildId().isPresent();
		}).filter(event -> {
			return event.getGuildId().get().asLong() == guildId;
		}).filter(event -> {
			return event.getChannelId().asLong() == channelId;
		}).filter(event -> {
			if (!event.getEmoji().asUnicodeEmoji().isPresent())
				return false;
			return event.getEmoji().asUnicodeEmoji().get().getRaw().equals("👍");
		}).filter(event -> {
			return event.getEmoji().asUnicodeEmoji().isPresent();
		}).subscribe(event -> {
			if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals(thumbsup)) {
				event.getChannel().subscribe(channel -> {
					channel.createMessage("Thanks <@" + event.getUserId().asString() + ">!").subscribe();
				});
			}
		}, error -> error.printStackTrace());

		gateway.onDisconnect().block();
	}

	@Test
	public void botTest() {
		BotMain.botTest(null);
	}
}
