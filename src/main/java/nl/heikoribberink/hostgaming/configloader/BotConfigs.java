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
	String Token, EventTitle;
	long ChannelId, Host;
	int InputDelay, MaxInputs, MinVotes;
	Map<String, Integer> KeyMappings;
	List<Long> WhiteListedUsers;

	public BotConfigs(String ConfigLocation) {
		botConfig = new File(ConfigLocation);
		KeyConfig = new File(getKeyPath());
		WhiteList = new File(getWhiteListPath());
		try {
			configReader = new BufferedReader(new FileReader(botConfig));
			KeyReader = new BufferedReader(new FileReader(KeyConfig));
			WhitelistReader = new BufferedReader(new FileReader(WhiteList));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Token = getToken();
			EventTitle = getEventTitle();
			ChannelId = getChannelId();
			Host = getHost();
			InputDelay = getInputDelay();
			MaxInputs = getMaxInputs();
			MinVotes = getMinVotes();
			KeyMappings = getKeyMappings();
			WhiteListedUsers = getWhitelistedUsers();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
		general function for getting a value from a file
		mode == true is for numbers, mode == false is for numbers and characters
	*/
	public String findValue(String ValueName, BufferedReader reader, boolean mode) throws IOException{
		ArrayList<String> lines = new ArrayList<String>();
		while(true){
			String line = reader.readLine();
			if(line != null){
				lines.add(line);
			}
			else{
				break;
			}
		}
		
		int ValueIndex = 0;
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).contains(ValueName.toLowerCase())){
				ValueIndex = i+1;
			}
		}
		if(ValueIndex == 0){
			System.out.println("no " + ValueName + " specified");
			return null;
		}
		else{
			String value = null;	
			String line = lines.get(ValueIndex - 1);
			for(int i = 0; i < line.length(); i++){
				if(mode){
					if(Character.isDigit(line.charAt(i)) ){
						value += line.charAt(i);
					}
				}
				else{
					if(line.charAt(i) != '=' && i > (line.indexOf(ValueName) + ValueName.length()) ){
						value += line.charAt(i);
					}
				}
			}
			if(value == null){
				System.out.println(ValueName + " found, but no id specified");
			}
			return value;
		}
	}

	public String getKeyPath() throws IOException{
		String path = findValue("keypath", configReader, false);
		return path;
	}

	public String getWhiteListPath() throws IOException{
		String path = findValue("whitelistpath", configReader, false);
		return path;
	}

	public String getToken() throws IOException {
		String token = findValue("token", configReader, false);
		return token;
	}

	public long getChannelId() throws IOException {
		String id = findValue("channel_id", configReader, true);
		return Long.parseLong(id);
	}

	public int getInputDelay() throws IOException{
		String delay = findValue("input_delay", configReader, true);
		return Integer.parseInt(delay);
	}

	public int getMaxInputs() throws IOException{
		String max_inputs = findValue("max_inputs", configReader, true);
		return Integer.parseInt(max_inputs);
	}

	public int getMinVotes() throws IOException{
		String min_votes = findValue("min_votes", configReader, true);
		return Integer.parseInt(min_votes);
	}

	public long getHost(){
		return 0l;
	}

	public String getEventTitle(){
		return null;
	}

	public Map<String, Integer> getKeyMappings() {
		return null;
	}

	public List<Long> getWhitelistedUsers() {
		return null;
	}
}
