package nl.heikoribberink.hostgaming.configloader;

import java.io.BufferedReader;
import java.io.File;
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

	private File botConfig, keyConfig, whiteList;
	private BufferedReader configReader, keyReader, whitelistReader;
	private ArrayList<String> configLines, keyLines, whiteListLines;
	private String token, eventTitle;
	private long channelId, host;
	private int inputDelay, maxInputs, minVotes;
	private Map<String, Integer> keyMappings;
	private List<Long> whiteListedUsers;

	public BotConfigs(String ConfigLocation) {
		try {
			botConfig = new File(ConfigLocation);
			configReader = new BufferedReader(new FileReader(botConfig));
			configLines = getLines(configReader);
			keyConfig = new File(getKeyPath());
			keyReader = new BufferedReader(new FileReader(keyConfig));
			keyLines = getLines(keyReader);
			whiteList = new File(getWhiteListPath());
			whitelistReader = new BufferedReader(new FileReader(whiteList));
			whiteListLines = getLines(whitelistReader);

			token = getToken();
			eventTitle = getEventTitle();
			channelId = getChannelId();
			host = getHost();
			inputDelay = getInputDelay();
			maxInputs = getMaxInputs();
			minVotes = getMinVotes();
			keyMappings = getKeyMappings();
			whiteListedUsers = getWhitelistedUsers();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//function for converting a file to String form
	public ArrayList<String> getLines(BufferedReader reader) throws IOException{
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
		reader.close();
		return lines;
	}

	/*
		general function for getting a value from a file
		mode == true is for numbers, mode == false is for numbers and characters
	*/
	public String findValue(String ValueName, ArrayList<String> lines, boolean mode) throws IOException{		
		int ValueIndex = -1;
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).contains(ValueName.toLowerCase())){
				ValueIndex = i;
				break;
			}
		}
		if(ValueIndex != -1){
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
				throw new NullPointerException(ValueName + " found, but no id specified");
			}
			return value;
		}
		else{
			throw new NullPointerException("no " + ValueName + " specified");
		}
	}

	public String getKeyPath() throws IOException{
		String path = findValue("keypath", configLines, false);
		return path;
	}

	public String getWhiteListPath() throws IOException{
		String path = findValue("whitelistpath", configLines, false);
		return path;
	}

	public String getToken() throws IOException {
		String token = findValue("token", configLines, false);
		return token;
	}

	public long getChannelId() throws IOException {
		String id = findValue("channel_id", configLines, true);
		return Long.parseLong(id);
	}

	public int getInputDelay() throws IOException{
		String delay = findValue("input_delay", configLines, true);
		return Integer.parseInt(delay);
	}

	public int getMaxInputs() throws IOException{
		String max_inputs = findValue("max_inputs", configLines, true);
		return Integer.parseInt(max_inputs);
	}

	public int getMinVotes() throws IOException{
		String min_votes = findValue("min_votes", configLines, true);
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

	//variable getters
	public String tokenGet(){
		return token;
	}

	public String eventTitleGet(){
		return eventTitle;
	}

	public long channelIdGet(){
		return channelId;
	}

	public long hostGet(){
		return host;
	}

	public int inputDelayGet(){
		return inputDelay;
	}

	public int maxInputsGet(){
		return maxInputs;
	}

	public int minVotesGet(){
		return minVotes;
	}

	public Map<String, Integer> keyMappingsGet(){
		return keyMappings;
	}

	public List<Long> whiteListedUsersGet(){
		return whiteListedUsers;
	}
}
