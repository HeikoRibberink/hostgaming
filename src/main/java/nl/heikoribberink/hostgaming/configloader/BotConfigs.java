package nl.heikoribberink.hostgaming.configloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
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

	File botConfig, KeyConfig, WhiteList;
	BufferedReader configReader, KeyReader, WhitelistReader;
	String Token;
	long ChannelId;
	double InputDelay;
	int MaxInputs, MinVotes;
	Map<String, Integer> KeyMappings;
	List<Long> WhiteListedUsers;

	public BotConfigs(String ConfigLocation, String KeyLocation, String WhitelistLocation) {
		botConfig = new File(ConfigLocation);
		KeyConfig = new File(KeyLocation);
		WhiteList = new File(WhitelistLocation);
		try {
			configReader = new BufferedReader(new FileReader(botConfig));
			KeyReader = new BufferedReader(new FileReader(KeyConfig));
			WhitelistReader = new BufferedReader(new FileReader(WhiteList));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Token = getToken();
			ChannelId = getChannelId();
			InputDelay = getInputDelay();
			MaxInputs = getMaxInputs();
			MinVotes = getMinVotes();
			KeyMappings = getKeyMappings();
			WhiteListedUsers = getWhitelistedUsers();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getToken() {
		return null;
	}

	public long getChannelId() throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		while(true){
			String line = configReader.readLine();
			if(line != null){
				lines.add(line);
			}
			else{
				break;
			}
		}
		
		int ChannelIdIndex = 0;
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).contains("channel_id")){
				ChannelIdIndex = i+1;
			}
		}
		if(ChannelIdIndex == 0){
			System.out.println("no ChannelId specified");
			return 0;
		}
		else{
			String id = null;	
			String line = lines.get(ChannelIdIndex - 1);
			for(int i = 0; i < line.length(); i++){
				if(Character.isDigit(line.charAt(i)) ){
					id += line.charAt(i);
				}
			}
			if(id == null){
				System.out.println("channel_id found, but no id specified");
			}
			return Long.parseLong(id);
		}
	}

	public double getInputDelay(){
		return 0;
	}

	public int getMaxInputs(){
		return 0;
	}

	public int getMinVotes(){
		return 0;
	}

	public Map<String, Integer> getKeyMappings() {
		return null;
	}

	public List<Long> getWhitelistedUsers() {
		return null;
	}
}
