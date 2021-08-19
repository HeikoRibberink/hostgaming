package nl.heikoribberink.hostgaming;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import nl.heikoribberink.hostgaming.configloader.BotConfigs;
import nl.heikoribberink.hostgaming.utils.ConsoleWindow;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Main class for the HostGaming bot.
 * 
 * @author <a href="https://github.com/HeikoRibberink">Heiko Ribberink</a>
 */
public class BotMain {
	public static void main(String[] args) {
		start(null);
	}

	private static void start(BotConfigs botConfigs) {
		final String token = /*botConfigs.getToken()*/ "ODc3NDY3ODAzMzkxNzY2NTQ5.YRzDkg.lHu3MzOtFTN3peJdgGZ5astta9s";
		final long channelId = /*botConfigs.getChannelId()*/ 806105311344853063l;
		final List<Long> whitelist = /*botConfigs.getWhitelistedUsers()*/ List.of(465810891997315083l, 538659433014886412l, 621609207766188042l);
		final Map<String, Integer> keybinds = /*botConfigs.getKeyMappings()*/ Map.of("👍", KeyEvent.VK_W);
		final int maxInputs = /*botConfigs.getMaxInputs()*/ 1, minVotes = /*botConfigs.getMinVotes()*/ 1;
		final ConsoleWindow console = new ConsoleWindow("Hosted Gaming Bot Console", 24, 120);
		System.setOut(console.getOut());

		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();

		Thread consoleHandler = new Thread(() -> {
			BufferedReader reader = new BufferedReader(new InputStreamReader(console.getIn()));
			boolean running = true;
			String str;
			while(running) {
				try {
					switch (((str = reader.readLine()) != null ? str : "").toLowerCase()) {
						case "stop":
							gateway.logout().subscribe();
							running = false;
							break;
					
						default:
							break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		consoleHandler.start();

		gateway.onDisconnect().block();
		try {
			consoleHandler.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		console.dispose();
	}

	/**
	 * 
	 * @param botConfigs - The {@link BotConfigs} object needed to initialize the
	 *                   bot.
	 */
	public static void botTest(BotConfigs botConfigs) {
		// Initialize the client and gateway

		// final String token = botConfigs.getToken();
		// final long channelId = botConfigs.getChannelId();
		// final List<Long> whitelist = botConfigs.getWhitelistedUsers();
		// final Map<String, Integer> keybinds = botConfigs.getKeyMappings();
		final String token = "ODc3NDY3ODAzMzkxNzY2NTQ5.YRzDkg.lHu3MzOtFTN3peJdgGZ5astta9s";
		final long channelId = 806105311344853063l;
		final List<Long> whitelist = List.of(465810891997315083l, 538659433014886412l, 621609207766188042l);
		final Map<String, Integer> keybinds = Map.of("👍", KeyEvent.VK_W);
		final int maxInputs = 1, minVotes = 1;
		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();

		// Send a "Inputs" message to the specified channel.
		MessageChannel msgChannel = (MessageChannel) gateway.getChannelById(Snowflake.of(channelId)).block();
		Message msg = msgChannel.createMessage("Inputs").block();
		msg.addReaction(ReactionEmoji.unicode("👍")).block();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		msg = msg.getClient().getMessageById(msg.getChannelId(), msg.getId()).block();

		countVotes(msg, keybinds, whitelist)
				.subscribe(map -> {
					System.out.println(map.toString());
					issueInputs(chooseVotes(map, maxInputs, minVotes), keybinds, 100);
					gateway.logout().block();
				});

		gateway.onDisconnect().block();
	}

	/**
	 * Counts all reactions per bounded emoji, filtering for whitelisted users.
	 * 
	 * @param msg       - The {@link Message} of which the reactions must be
	 *                  counted.
	 * @param keybinds  - The bounded emoji's to filter for.
	 * @param whitelist - The id's whitelisted users.
	 * @return A map containing the number of reactions per bounded emoji, filtered
	 *         for whitelisted users.
	 */

	public static Mono<Map<String, Long>> countVotes(Message msg, Map<String, Integer> keybinds, List<Long> whitelist) {

		// Retrieve all messages, count and store them per emoji filtering the
		// whitelisted users.
		Mono<Map<String, Long>> counts = Flux.fromStream(msg.getReactions().stream()).filter(reaction -> {
			return reaction.getEmoji().asUnicodeEmoji().isPresent(); // All emoji's without a Unicode type aren't
																		// supported.
		}).filter(reaction -> {
			return keybinds.containsKey(reaction.getEmoji().asUnicodeEmoji().get().getRaw()); // Filter for all bounded
																								// emoji's.
		}).collectMap(reaction -> {
			return reaction.getEmoji().asUnicodeEmoji().get().getRaw(); // Key is unicode name.
		}, reaction -> {
			return msg.getReactors(reaction.getEmoji()).filter(user -> { // Filter for whitelisted users.
				return whitelist.contains(user.getId().asLong());
			}).count().block(); // Value is count.
		});
		return counts;
	}

	public static List<String> chooseVotes(Map<String, Long> counts, int maxInputs, int minVotes) {
		List<String> out = new ArrayList<String>();
		ArrayList<Entry<String, Long>> entries = new ArrayList<Entry<String, Long>>(counts.entrySet());
		Collections.sort(entries, (a, b) -> {
			if (a.getValue() < b.getValue())
				return 1;
			if (a.getValue() == b.getValue())
				return 0;
			return -1;
		});
		for (int i = 0; i < maxInputs && i < entries.size(); i++) {
			if (entries.get(i).getValue() < minVotes)
				break;
			out.add(entries.get(i).getKey());
		}
		return out;
	}

	private static void issueInputs(List<String> inputs, Map<String, Integer> keybinds, long length) {
		Thread thread = new Thread(() -> {
			Robot robot;
			try {
				robot = new Robot();
				for (String key : inputs) {
					robot.keyPress(keybinds.get(key));
				}
				Thread.sleep(length);
				for (String key : inputs) {
					robot.keyRelease(keybinds.get(key));
				}
			} catch (AWTException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}
}
