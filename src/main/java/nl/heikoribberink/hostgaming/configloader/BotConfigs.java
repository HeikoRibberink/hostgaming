package nl.heikoribberink.hostgaming.configloader;

import java.util.Map;

/**
 * Class used for loading and storing bot configurations from and to a file.
 * 
 * @author <a href="https://github.com/MichaPehlivan">Micha Pehlivan</a>
 */

// Deze class moet jij schrijven, Micha. Ik heb alle benodigde functies
// toegevoegd. Het is de bedoeling dat je de configs van een .hg.conf file leest
// per game, en dat het readen een van deze objects returned. Ik heb met de
// Minecraft.hg.conf een voorbeeld van de format van de file gegeven, maar het
// hoeft niet persé zo.
public class BotConfigs {

	public BotConfigs(String fileLocation) {

	}

	public String getToken() {
		return null;
	}

	public long getGuildId() {
		return 0l;
	}

	public long getChannelId() {
		return 0l;
	}

	public Map<String, Integer> getKeyMappings() {
		return null;
	}
}
