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
import java.util.concurrent.ConcurrentHashMap;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
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
		try {
			start(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static volatile boolean RUNNING = false;
	private static volatile boolean IN_EVENT = false;

	private static Runnable eventRunnable;
	private static Thread eventThread;

	private static void start(BotConfigs configs) throws Exception {
		final String token = /* configs.getToken() */ "ODc3NDY3ODAzMzkxNzY2NTQ5.YRzDkg.lHu3MzOtFTN3peJdgGZ5astta9s";
		final ConsoleWindow console = new ConsoleWindow("Hosted Gaming Bot Console", 24, 120);
		System.setOut(console.getOut());

		final DiscordClient client = DiscordClient.create(token);
		IntentSet intents = IntentSet.of(Intent.GUILD_MESSAGES, Intent.GUILD_MESSAGE_REACTIONS,
				Intent.GUILD_MESSAGE_TYPING, Intent.DIRECT_MESSAGES, Intent.DIRECT_MESSAGE_REACTIONS);
		client.gateway().setEnabledIntents(intents);
		client.gateway().setDisabledIntents(intents.not());
		final GatewayDiscordClient gateway = client.login().block();

		gateway.onDisconnect().subscribe(ctx -> {
			RUNNING = false;
			IN_EVENT = false;
		});

		eventRunnable = () -> {
			try {
				runEvent(gateway, configs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};

		RUNNING = true;
		handleConsole(console, gateway);

		console.dispose();
	}

	private static void handleConsole(final ConsoleWindow console, final GatewayDiscordClient gateway) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(console.getIn()));
		String str;
		while (RUNNING) {
			try {
				switch (((str = reader.readLine()) != null ? str : "").toLowerCase()) { // Makes sure str is never
																						// null
				case "start":
					start();
					break;

				case "stop":
					stop();
					break;

				case "exit":
					exit(gateway);
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
	}

	private static void exit(final GatewayDiscordClient gateway) {
		if (IN_EVENT)
			return;
		RUNNING = false;
		gateway.logout().subscribe();
	}

	private static void start() {
		if (IN_EVENT)
			return;
		IN_EVENT = true;
		eventThread = new Thread(eventRunnable);
		eventThread.start();
	}

	private static void stop() {
		IN_EVENT = false;
		if (eventThread == null)
			return;
		try {
			eventThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void runEvent(final GatewayDiscordClient gateway, final BotConfigs configs)
			throws InterruptedException {
		final long channelId = 878612609702699022l;
		final List<Long> whitelist = null;
		final Map<String, Integer> keybinds = Map.of("⬆️", KeyEvent.VK_W, "⬇️", KeyEvent.VK_S, "⬅️", KeyEvent.VK_A,
				"➡️", KeyEvent.VK_D);
		final List<String> reactions = List.of("⬅️", "⬆️", "⬇️", "➡️");
		final int maxInputs = 4, minVotes = 2;
		final long delay = 0;
		final Snowflake host = Snowflake.of(465810891997315083l);
		final String title = "Testing HostedGaming bot.";

		final MessageChannel msgChannel = (MessageChannel) gateway.getChannelById(Snowflake.of(channelId)).block();
		final String startupContent = "Starting '" + title + "' hosted by <@" + host.asLong() + ">!";
		final Message msg = msgChannel.createMessage(startupContent).block();
		System.out.println("Starting countdown.");
		int countdown = 3;
		while (IN_EVENT && countdown >= 1) {
			final int whyisthisnecessary = countdown;
			msg.edit(msgEdit -> {
				msgEdit.setContent(startupContent + " \n in " + whyisthisnecessary + " seconds.");
			}).subscribe();
			System.out.println("Starting in " + countdown + " seconds.");
			Thread.sleep(1000);
			countdown--;
		}
		if (IN_EVENT) {
			msg.edit(msgEdit -> {
				msgEdit.setContent("**INPUTS**");
			}).block();
			for (String reaction : reactions) {
				msg.addReaction(ReactionEmoji.unicode(reaction)).subscribe();
			}
		}
		while (IN_EVENT) {
			Thread.sleep(delay);
			Message updatedMsg = msg.getClient().getMessageById(msg.getChannelId(), msg.getId()).block();
			countVotes(updatedMsg, keybinds, whitelist).subscribe(map -> {
				long s = System.currentTimeMillis();
				Map<String, Long> temp;
				issueInputs(chooseVotes(temp = transferItemsAndWait(map), maxInputs, minVotes), keybinds, 1000);
				System.out.println(temp.values().toString());
				System.out.println("issueInputs & chooseVotes & transferItems: " + (System.currentTimeMillis() - s));
			});
		}
		msg.removeAllReactions().subscribe();
		msg.edit(msgEdit -> {
			msgEdit.setContent("**Event has ended!** \n Thanks for participating.");
		}).block();
	}

	/**
	 * Counts all reactions per bounded emoji, filtering for whitelisted users.
	 * 
	 * @param msg       - The {@link Message} of which the reactions must be
	 *                  counted.
	 * @param keybinds  - The bounded emoji's to filter for.
	 * @param whitelist - The id's of whitelisted users. Set to null to disable
	 *                  whitelist.
	 * @return A map containing the number of reactions per bounded emoji, filtered
	 *         for whitelisted users.
	 */

	public static Mono<Map<String, Mono<Long>>> countVotes(Message msg, Map<String, Integer> keybinds, List<Long> whitelist) {

		// Retrieve all messages, count and store them per emoji filtering the
		// whitelisted users.
		Mono<Map<String, Mono<Long>>> counts = Flux.fromStream(msg.getReactions().stream()).filter(reaction -> {
			return reaction.getEmoji().asUnicodeEmoji().isPresent(); // All emoji's without a Unicode type aren't
																		// supported.
		}).filter(reaction -> {
			return keybinds.containsKey(reaction.getEmoji().asUnicodeEmoji().get().getRaw()); // Filter for all bounded
																								// emoji's.
		}).collectMap(reaction -> {
			return reaction.getEmoji().asUnicodeEmoji().get().getRaw(); // Key is unicode name.
		}, reaction -> {
			Mono<Long> l = msg.getReactors(reaction.getEmoji()).filter(user -> { // Filter for whitelisted users.
				if (whitelist == null)
					return true;
				return whitelist.contains(user.getId().asLong());
			}).count(); // Value is count.
			// Mono<Long> out = l.cache();
			// return out;
			return l;
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
				for (Integer key : keybinds.values()) {
					robot.keyRelease(key);
				}
				for (String key : inputs) {
					robot.keyPress(keybinds.get(key));
				}
			} catch (AWTException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

	private static Map<String, Long> transferItemsAndWait(Map<String, Mono<Long>> in) {
		ConcurrentHashMap<String, Long> out = new ConcurrentHashMap<String, Long>(in.size());
		for (Entry<String, Mono<Long>> entry : in.entrySet()) {
			entry.getValue().subscribe(ctx -> {
				out.put(entry.getKey(), ctx);
			});
		}
		while(out.size() < in.size()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

}
