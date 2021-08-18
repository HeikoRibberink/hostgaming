package nl.heikoribberink.hostgaming.configloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

	File botConfig;
	BufferedReader configReader;

	public BotConfigs(String fileLocation) {
		botConfig = new File(fileLocation);
		try {
			configReader = new BufferedReader(new FileReader(botConfig));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getToken() {
		return null;
	}

	public long getChannelId() throws IOException {
		String id = null;
		configReader.skip(13);
		for(int i = 0; i < 18; i++){
			id += configReader.read();
		}
		return Long.parseLong(id);
	}

	public Map<String, Integer> getKeyMappings() {
		return null;
	}
}
