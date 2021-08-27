package nl.heikoribberink.hostgaming.configloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;

import com.vdurmont.emoji.EmojiManager;

import discord4j.core.object.reaction.ReactionEmoji;
/**
 * Class used for loading and storing bot configurations from and to a file.
 * 
 * @author <a href="https://github.com/MichaPehlivan">Micha Pehlivan</a>
 */
public class BotConfigs {

	private File botConfig, keyConfig, whiteList;
	private BufferedReader configReader, keyReader, whitelistReader;
	private ArrayList<String> configLines, keyLines, whiteListLines;
	private String token, eventTitle, mode;
	private long channelId, host;
	private int inputDelay, maxInputs, minVotes, exitKey;
	private Map<String, Integer> keyMappings = new HashMap<String, Integer>();
	private List<Long> whiteListedUsers = new ArrayList<Long>();

	public BotConfigs(String ConfigLocation) {
		try {
			botConfig = new File(ConfigLocation);
			configReader = new BufferedReader(new FileReader(botConfig, Charset.forName("UTF-8")));
			configLines = getLines(configReader);
			keyConfig = new File(getKeyPath());
			keyReader = new BufferedReader(new FileReader(keyConfig, Charset.forName("UTF-8")));
			keyLines = getLines(keyReader);
			whiteList = new File(getWhiteListPath());
			whitelistReader = new BufferedReader(new FileReader(whiteList, Charset.forName("UTF-8")));
			whiteListLines = getLines(whitelistReader);

			setVariables();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//function for converting a file to String form
	private ArrayList<String> getLines(BufferedReader reader) throws IOException{
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
	private String findValue(String ValueName, ArrayList<String> lines, boolean mode) throws IOException{		
		int ValueIndex = -1;
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).contains(ValueName.toLowerCase())){
				ValueIndex = i;
				break;
			}
		}
		if(ValueIndex != -1){
			String value = "";	
			String line = lines.get(ValueIndex);
			for(int i = 0; i < line.length(); i++){
				if(mode){
					if(Character.isDigit(line.charAt(i)) ){
						value += line.charAt(i);
					}
				}
				else if(line.charAt(i) != '=' && i > (line.indexOf(ValueName) + ValueName.length())){
					value += line.charAt(i);
				}
			}
			if(value.equals("")){
				throw new NullPointerException(ValueName + " found, but no value specified");
			}
			return value.trim();
		}
		else{
			throw new NullPointerException("no " + ValueName + " specified");
		}
	}

	//unicode to keycode conversion
	private int unicodeToKeyCode(String keyName) throws IllegalArgumentException, IllegalAccessException{
		String fieldName = "VK_" + keyName;
		fieldName = fieldName.toUpperCase();
		Field f;
		try {
    		f = KeyEvent.class.getField(fieldName);
		} catch (NoSuchFieldException e) {
    		e.printStackTrace();
    		throw new IllegalArgumentException("Key code is invalid.");
		}
		if(f.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) throw new IllegalArgumentException("Key code is invalid.");
		return f.getInt(this);
	}

	//setting up the keybinds hashmap
	private void readKeyMappings(){
		for(int i = 0; i < keyLines.size(); i++){
			String line = keyLines.get(i).trim();
			String emoji = null;
			String keyName = "";
			boolean emojiChecking = true;
			for(int j = 0; j < line.length(); j++){
				char currentChar = line.charAt(j);
				if(currentChar == ':'){
					emojiChecking = false;
				}
				if(EmojiManager.isEmoji(Character.toString(currentChar)) && emojiChecking){
					emoji = Character.toString(currentChar);
				}
				if(currentChar != ':' && !emojiChecking){
					keyName += currentChar;
				}
			}
			try {
				keyMappings.put(emoji, unicodeToKeyCode(keyName.trim()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	//function for setting the values of all variables
	private void setVariables() throws IOException{
		token = findValue("token", configLines, false);
		eventTitle = findValue("event_title", configLines, false);
		channelId = Long.parseLong(findValue("channel_id", configLines, false));
		inputDelay = Integer.parseInt(findValue("input_delay", configLines, true));
		maxInputs = Integer.parseInt(findValue("max_inputs", configLines, true));
		minVotes = Integer.parseInt(findValue("min_votes", configLines, true));
		host = Long.parseLong(findValue("host_id", configLines, true));
		eventTitle = findValue("event_title", configLines, false);
		mode = findValue("mode", configLines, false);
		exitKey = Integer.parseInt(findValue("exit_key", configLines, true));
		
		for(int i = 0; i < whiteListLines.size(); i++){
			whiteListedUsers.add(Long.parseLong(whiteListLines.get(i)));
		}

		readKeyMappings();
	}

	//getters for paths
	private String getKeyPath() throws IOException{
		String path = findValue("keypath", configLines, false);
		return path;
	}

	private String getWhiteListPath() throws IOException{
		String path = findValue("whitelistpath", configLines, false);
		return path;
	}

	//variable getters
	public String getToken() {
		return token;
	}

	public long getChannelId() {
		return channelId;
	}

	public int getInputDelay() {
		return inputDelay;
	}

	public int getMaxInputs() {
		return maxInputs;
	}

	public int getMinVotes() {
		return minVotes;
	}

	public long getHost() {
		return host;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public String getMode() {
		return mode;
	}

	public int getExitKey(){
		return exitKey;
	}

	public Map<String, Integer> getKeyMappings() {
		return keyMappings;
	}

	public List<Long> getWhitelistedUsers() {
		return whiteListedUsers;
	}
}