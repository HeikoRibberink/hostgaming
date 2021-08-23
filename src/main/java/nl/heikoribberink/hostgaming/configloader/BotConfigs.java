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

	private File botConfig, KeyConfig, WhiteList;
	private BufferedReader configReader, KeyReader, WhitelistReader;
	private ArrayList<String> ConfigLines, KeyLines, WhiteListLines;
	private String Token, EventTitle;
	private long ChannelId, Host;
	private int InputDelay, MaxInputs, MinVotes;
	private Map<String, Integer> KeyMappings;
	private List<Long> WhiteListedUsers;

	public BotConfigs(String ConfigLocation) {
		try {
			botConfig = new File(ConfigLocation);
			configReader = new BufferedReader(new FileReader(botConfig));
			ConfigLines = getLines(configReader);
			KeyConfig = new File(getKeyPath());
			KeyReader = new BufferedReader(new FileReader(KeyConfig));
			KeyLines = getLines(KeyReader);
			WhiteList = new File(getWhiteListPath());
			WhitelistReader = new BufferedReader(new FileReader(WhiteList));
			WhiteListLines = getLines(WhitelistReader);

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
		System.out.println(ChannelIdGet());
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
		int ValueIndex = 0;
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).contains(ValueName.toLowerCase())){
				ValueIndex = i+1;
			}
		}
		if(ValueIndex == 0){
			throw new NullPointerException("no " + ValueName + " specified");
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
				throw new NullPointerException(ValueName + " found, but no id specified");
			}
			return value;
		}
	}

	public String getKeyPath() throws IOException{
		String path = findValue("keypath", ConfigLines, false);
		return path;
	}

	public String getWhiteListPath() throws IOException{
		String path = findValue("whitelistpath", ConfigLines, false);
		return path;
	}

	public String getToken() throws IOException {
		String token = findValue("token", ConfigLines, false);
		return token;
	}

	public long getChannelId() throws IOException {
		String id = findValue("channel_id", ConfigLines, true);
		System.out.println(Long.parseLong(id));
		return Long.parseLong(id);
	}

	public int getInputDelay() throws IOException{
		String delay = findValue("input_delay", ConfigLines, true);
		return Integer.parseInt(delay);
	}

	public int getMaxInputs() throws IOException{
		String max_inputs = findValue("max_inputs", ConfigLines, true);
		return Integer.parseInt(max_inputs);
	}

	public int getMinVotes() throws IOException{
		String min_votes = findValue("min_votes", ConfigLines, true);
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
	public String TokenGet(){
		return Token;
	}

	public String EventTitleGet(){
		return EventTitle;
	}

	public long ChannelIdGet(){
		return ChannelId;
	}

	public long HostGet(){
		return Host;
	}

	public int InputDelayGet(){
		return InputDelay;
	}

	public int MaxInputsGet(){
		return MaxInputs;
	}

	public int MinVotesGet(){
		return MinVotes;
	}

	public Map<String, Integer> KeyMappingsGet(){
		return KeyMappings;
	}

	public List<Long> WhiteListedUsersGet(){
		return WhiteListedUsers;
	}
}
