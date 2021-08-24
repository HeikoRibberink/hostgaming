package nl.heikoribberink.hostgaming;

import java.util.Map;
import java.util.Optional;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;

public class ReactionEventHandler implements Subscriber<ReactionAddEvent>{

	private Subscription s;
	private long channelId;
	private Map<String, Integer> keybinds;

	public ReactionEventHandler(long channelId, Map<String, Integer> keybinds) {
		this.channelId = channelId;
		this.keybinds = keybinds;
	}

	@Override
	public void onSubscribe(Subscription s) {
		this.s = s;
		s.request(Long.MAX_VALUE);
	}

	@Override
	public void onNext(ReactionAddEvent item) {
		if(item.getChannelId().asLong() == channelId) {
			Optional<Unicode> u;
			if((u = item.getEmoji().asUnicodeEmoji()).isPresent()) {
				if(keybinds.containsKey(u.get().getRaw())) {
					System.out.println(item.getUserId().asString() + ": " + u.get().getRaw());
				}
			}
		}
		
		
	}

	@Override
	public void onError(Throwable throwable) {
	}

	@Override
	public void onComplete() {
		
	}

	public synchronized void cancelSubscription() {
		s.cancel();
	}

	
}
