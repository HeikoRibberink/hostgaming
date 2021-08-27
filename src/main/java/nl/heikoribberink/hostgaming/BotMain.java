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
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import nl.heikoribberink.hostgaming.configloader.BotConfigs;
import nl.heikoribberink.hostgaming.utils.ConsoleWindow;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Main class for the HostGaming bot.
 * 
 * @author <a href="https://github.com/HeikoRibberink">Heiko Ribberink</a>
 */
public class BotMain {
	public static void main(String[] args) {
		final ConsoleWindow console = new ConsoleWindow("Hosted Gaming Bot Console", 24, 120);
		System.setOut(console.getOut());
		System.setErr(console.getOut());

		final BotConfigs configs;
		if(args.length == 1) configs = new BotConfigs(args[0]);
		// else configs = new BotConfigs("src\\test\\java\\nl\\heikoribberink\\hostgaming\\Minecraft.hg.conf");
		else configs = new BotConfigs(".hg.conf");

		System.out.println(configs.getKeyMappings());

		try {
			start(configs, console);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static volatile boolean RUNNING = false;
	private static volatile boolean IN_EVENT = false;

	private static Runnable eventRunnable;
	private static Thread eventThread;

	private static void start(final BotConfigs configs, final ConsoleWindow console) throws Exception {
		final String token = configs.getToken();
		final DiscordClient client = DiscordClient.create(token);
		IntentSet intents = IntentSet.of(Intent.GUILD_MESSAGES, Intent.GUILD_MESSAGE_REACTIONS,
				Intent.GUILD_MESSAGE_TYPING, Intent.DIRECT_MESSAGES, Intent.DIRECT_MESSAGE_REACTIONS);
		client.gateway().setEnabledIntents(intents);
		client.gateway().setDisabledIntents(intents.not());
		final GatewayDiscordClient gateway = client.login().block();

		gateway.onDisconnect().subscribe(ctx -> {
			System.out.println("Disconnected.");
			if(!RUNNING) {
				return;
			}
			stop();
		});

		final GlobalKeyboardHook kHook = new GlobalKeyboardHook();
		final int exitKey = configs.getExitKey();
		kHook.addKeyListener(new GlobalKeyAdapter() {
			@Override
			public void keyPressed(GlobalKeyEvent event) {
				if (event.getVirtualKeyCode() == exitKey)
					stop();
			}

		});
		System.out.format("Starting global keyboard hook for emergency stop key (%s). %n", KeyEvent.getKeyText(exitKey));

		eventRunnable = () -> {
			try {
				// activityReactionVote(gateway, configs);
				activityReactionEventVote(gateway, configs);
				System.out.println("Activity has ended.");
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				System.out.println("Activity has been aborted.");
			}
		};

		RUNNING = true;
		handleConsole(console, gateway);
		System.out.println("Shutting down global keyboard hook.");
		kHook.shutdownHook();

		System.out.println("Disposing console.");
		console.dispose();
	}

	private static void handleConsole(final ConsoleWindow console, final GatewayDiscordClient gateway) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(console.getIn()));
		String str;
		while (RUNNING) {
			try {
				str = reader.readLine();
				if (str != null) {
					str = str.toLowerCase();
					System.out.println("[HOST] " + str);
					switch (str) {
						case "start":
							start();
							break;

						case "stop":
							stop();
							break;

						case "exit":
							exit(gateway);
							break;
						
						case "help":
							printHelp();

						default:
							break;
					}
				}
				Thread.sleep(50);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printHelp() {
		System.out.println("[-----HELP-----\n" +
		"| You can start the bot by typing commands in the lowermost text box and pressing 'Enter'. \n" +
		"| The following commands are currently available: \n" +
		"| 	start		Starts the activity.\n" +
		"| 	stop		Stops the activity.\n" +
		"| 	exit		Closes all connections and stops this program.\n" +
		"| 	help		Displays this information.\n" +
		"| For more information, read README.md \n" +
		"|______________");
	}

	private synchronized static void exit(final GatewayDiscordClient gateway) {
		if (IN_EVENT) {
			System.out.println("The activity is still running! Stop the activity (by running 'stop') before exiting!");
			return;
		}
		System.out.println("Closing connections and stopping the bot.");
		RUNNING = false;
		gateway.logout().block();
	}

	private synchronized static void start() {
		System.out.println("Starting activity.");
		if (IN_EVENT) {
			System.out.println("The activity is already running, no need to start it again.");
			return;
		}
		IN_EVENT = true;
		eventThread = new Thread(eventRunnable);
		eventThread.start();
	}

	private synchronized static void stop() {
		System.out.println("Stopping activity.");
		if(!IN_EVENT) {
			System.out.println("The activity is not currently running.");
			return;
		}
		IN_EVENT = false;
		try {
			eventThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Runs the activity and chooses the input events to be issued by voting through
	 * counting all reactions after a specified period of time. Usually
	 * {@link #activityReactionEventVote(GatewayDiscordClient, BotConfigs)} is
	 * preferred over this way to run the activity. This function may be faster for
	 * very large audiences however, although it has not yet been tested and is only
	 * speculation.
	 * 
	 * @param gateway - The {@link GatewayDiscordClient gateway} used for
	 *                communicating with Discord.
	 * @param configs - The {@link BotConfigs} configurations.
	 * @throws InterruptedException Specified by {@link Thread#sleep(long)}.
	 * @throws IOException          Specified by {@link BotConfigs}.
	 */
	private static void activityReactionVote(final GatewayDiscordClient gateway, final BotConfigs configs)
			throws InterruptedException, IOException {
		final long channelId = configs.getChannelId();
		final List<Long> whitelist = configs.getWhitelistedUsers();
		final Map<String, Integer> keybinds = configs.getKeyMappings();
		final int maxInputs = configs.getMaxInputs(), minVotes = configs.getMinVotes();
		final long delay = configs.getInputDelay();
		final long host = configs.getHost();
		final String title = configs.getEventTitle();
		final long selfId = gateway.getRestClient().getSelf().block().id().asLong();

		final MessageChannel msgChannel = (MessageChannel) gateway.getChannelById(Snowflake.of(channelId)).block();
		final String startupContent = "Starting '" + title + "' hosted by <@" + host + ">!";
		final Message msg = msgChannel.createMessage(startupContent).block();
		System.out.println("Starting countdown.");
		int countdown = 3;
		while (IN_EVENT && countdown >= 1) {
			final int whyisthisnecessary = countdown;
			msg.edit(msgEdit -> {
				msgEdit.setContent(startupContent + " \n in " + whyisthisnecessary + " seconds.");
			}).subscribe();
			System.out.format("Starting in %d seconds. %n", countdown);
			Thread.sleep(1000);
			countdown--;
		}
		if (IN_EVENT) {
			msg.edit(msgEdit -> {
				msgEdit.setContent("**INPUTS**");
			}).block();
			for (String reaction : keybinds.keySet()) {
				msg.addReaction(ReactionEmoji.unicode(reaction)).subscribe();
			}
		}
		while (IN_EVENT) {
			Thread.sleep(delay);
			Message updatedMsg = msg.getClient().getMessageById(msg.getChannelId(), msg.getId()).block();
			countVotesImmediate(updatedMsg, keybinds, whitelist).subscribe(map -> {
				long s = System.currentTimeMillis();
				Map<String, Long> temp;
				issueInputs(chooseVotes(temp = transferItemsAndWait(map), maxInputs, minVotes), keybinds);
			});
		}
		msg.removeAllReactions().subscribe();
		msg.edit(msgEdit -> {
			msgEdit.setContent("**Event has ended!** \n Thanks for participating.");
		}).block();
	}

	/**
	 * Runs the activity and chooses the input events to be issued by voting through
	 * counting all the {@link ReactionAddEvent}s and the
	 * {@link ReactionRemoveEvent}s.
	 * 
	 * @param gateway - The {@link GatewayDiscordClient gateway} used for
	 *                communicating with Discord.
	 * @param configs - The {@link BotConfigs} configurations.
	 * @throws InterruptedException Specified by {@link Thread#sleep(long)}.
	 * @throws IOException          Specified by {@link BotConfigs}.
	 */

	private static void activityReactionEventVote(final GatewayDiscordClient gateway, final BotConfigs configs)
			throws InterruptedException, IOException {
		final long channelId = configs.getChannelId();
		final List<Long> whitelist = configs.getWhitelistedUsers();
		final Map<String, Integer> keybinds = configs.getKeyMappings();
		final int maxInputs = configs.getMaxInputs(), minVotes = configs.getMinVotes();
		final long delay = configs.getInputDelay();
		final long host = configs.getHost();
		final String title = configs.getEventTitle();
		final long selfId = gateway.getRestClient().getSelf().block().id().asLong();

		final MessageChannel msgChannel = (MessageChannel) gateway.getChannelById(Snowflake.of(channelId)).block();
		final String startupContent = "Starting '" + title + "' hosted by <@" + host + ">!";
		final Message msg = msgChannel.createMessage(startupContent).block();
		System.out.println("Starting countdown.");
		int countdown = 3;
		while (IN_EVENT && countdown >= 1) {
			final int whyisthisnecessary = countdown;
			msg.edit(msgEdit -> {
				msgEdit.setContent(startupContent + " \n in " + whyisthisnecessary + " seconds.");
			}).subscribe();
			System.out.format("Starting in %d seconds. %n", countdown);
			Thread.sleep(1000);
			countdown--;
		}
		if (IN_EVENT) {
			for (String reaction : keybinds.keySet()) {
				msg.addReaction(ReactionEmoji.unicode(reaction)).subscribe();
			}
			msg.edit(msgEdit -> {
				msgEdit.setContent("**INPUTS**");
			}).block();
		}
		final ConcurrentHashMap<String, Long> votes = new ConcurrentHashMap<String, Long>();
		Disposable addEvent = gateway.on(ReactionAddEvent.class).filter(event -> {
			return event.getChannelId().asLong() == channelId;
		}).filter(event -> {
			if (event.getUserId().asLong() == selfId)
				return false;
			if (whitelist == null)
				return true;
			return whitelist.contains(event.getUserId().asLong());
		}).filter(event -> {
			return event.getEmoji().asUnicodeEmoji().isPresent();
		}).filter(event -> {
			return keybinds.containsKey(event.getEmoji().asUnicodeEmoji().get().getRaw());
		}).subscribe(event -> {
			String emoji = event.getEmoji().asUnicodeEmoji().get().getRaw();
			Object count = votes.get(emoji);
			if (count == null)
				votes.put(emoji, 1l);
			else
				votes.put(emoji, (Long) count + 1);
		}, error -> {
			error.printStackTrace();
		});
		Disposable removeEvent = gateway.on(ReactionRemoveEvent.class).filter(event -> {
			return event.getChannelId().asLong() == channelId;
		}).filter(event -> {
			if (event.getUserId().asLong() == selfId)
				return false;
			if (whitelist == null)
				return true;
			return whitelist.contains(event.getUserId().asLong());
		}).filter(event -> {
			return event.getEmoji().asUnicodeEmoji().isPresent();
		}).filter(event -> {
			return keybinds.containsKey(event.getEmoji().asUnicodeEmoji().get().getRaw());
		}).subscribe(event -> {
			String emoji = event.getEmoji().asUnicodeEmoji().get().getRaw();
			Object count = votes.get(emoji);
			if (count == null)
				votes.put(emoji, -1l);
			else
				votes.put(emoji, (Long) count - 1);
		}, error -> {
			error.printStackTrace();
		});
		while (IN_EVENT) {
			Thread.sleep(delay);
			issueInputs(chooseVotes(votes, maxInputs, minVotes), keybinds);
		}
		addEvent.dispose();
		removeEvent.dispose();
		msg.removeAllReactions().subscribe();
		msg.edit(msgEdit -> {
			msgEdit.setContent("**Event has ended!** \n Thanks for participating.");
		}).block();
	}

	/**
	 * Counts all reactions per bounded emoji by reading them from the message,
	 * filtering for whitelisted users.
	 * 
	 * @param msg       - The {@link Message} of which the reactions must be
	 *                  counted.
	 * @param keybinds  - The bounded emoji's to filter for.
	 * @param whitelist - The id's of whitelisted users. Set to null to disable
	 *                  whitelist.
	 * @return A map containing the number of reactions per bounded emoji, filtered
	 *         for whitelisted users.
	 */

	private static Mono<Map<String, Mono<Long>>> countVotesImmediate(Message msg, Map<String, Integer> keybinds,
			List<Long> whitelist) {

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

	private static List<String> chooseVotes(Map<String, Long> counts, int maxInputs, int minVotes) {
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

	private static volatile Robot robot;

	private static void issueInputs(List<String> inputs, Map<String, Integer> keybinds) {
		if (robot == null)
			try {
				robot = new Robot();
			} catch (AWTException e1) {
				e1.printStackTrace();
			}
		Thread thread = new Thread(() -> {
			if (!IN_EVENT)
				return;
			for (Integer key : keybinds.values()) {
				robot.keyRelease(key);
			}
			for (String key : inputs) {
				robot.keyPress(keybinds.get(key));
			}
		});
		thread.start();
	}

	/**
	 * ransfers the items from one map to another by waiting for all the
	 * {@link Mono} items to complete. Intended for use in combination with
	 * {@link BotMain#activityReactionVote(GatewayDiscordClient, BotConfigs)}.
	 * 
	 * @param in - The Map<String, Mono<Long>> from which to transfer the items
	 * @return a Map<String, Long> containing all items after waiting for the
	 *         completed calculations.
	 */

	private static Map<String, Long> transferItemsAndWait(Map<String, Mono<Long>> in) {
		ConcurrentHashMap<String, Long> out = new ConcurrentHashMap<String, Long>(in.size());
		for (Entry<String, Mono<Long>> entry : in.entrySet()) {
			entry.getValue().subscribe(ctx -> {
				out.put(entry.getKey(), ctx);
			});
		}
		while (out.size() < in.size()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

	public static boolean getRunning() {
		return RUNNING;
	}

	public static boolean getInEvent() {
		return IN_EVENT;
	}

}
