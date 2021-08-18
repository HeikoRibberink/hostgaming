package nl.heikoribberink.hostgaming.configloader;

import java.util.Map;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

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
		boolean stop = false;
		String line = configReader.readLine();
		String id = null;
		if(line.substring(0, 9).equals("channel_id")){
			configReader.skip(10);
			while(true){
				char character = (char) configReader.read();
				if(Character.isDigit(character)){
					id += character;
				}
				if(character == '\n'){
					break;
				}
				if((int) character == -1){
					stop = true;
				}
			}
			if(id == null){
				System.out.println("no ChannelId found");
			}
			else{
				return Long.parseLong(id);
			}
		}
		else if(stop == false){
			getChannelId();
		}
		System.out.println("no ChannelId line found");
		return 0;
	}

	public Map<String, Integer> getKeyMappings() {
		return null;
	}
}
