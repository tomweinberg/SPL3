package TPCServer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import Reactor.tokenizer.StringMessage;
/**
 * 
 * Singleton of TBG
 *
 */
public class TBGPSingleton {
	private HashMap<String, Game> games;
	private HashMap <String, Room> rooms;
	private HashMap <ProtocolCallback<StringMessage>, Room> usersCallbacks;
	private ArrayList<String> nicks;
	private ArrayList<ProtocolCallback<StringMessage>> isInRoom;
	private Object gameStarterRoomLeaver;
	private Object addingRoom;
	
	
	private static class SingletonHolder {
        private static TBGPSingleton instance = new TBGPSingleton();
    }
	
	
	
    private TBGPSingleton() {
        games = new HashMap<String, Game>();
        games.put("BLUFFER", new Bluffer());  /// When Creating the Bluffer Class (for real), change the arguments accordingly.
        rooms = new HashMap<String, Room>();
        usersCallbacks = new HashMap<ProtocolCallback<StringMessage>, Room>();
        nicks = new ArrayList<String>();
        isInRoom= new ArrayList<ProtocolCallback<StringMessage>>();
        gameStarterRoomLeaver = new Object();
        addingRoom = new Object();
    }
    
    /**
     * 
     * @return the TBGPSingleton
     */
    
    public static TBGPSingleton getInstance() {
        return SingletonHolder.instance;
    }
	/**
	 * Add nick to the TBGPSingleton
	 * @param nick String of the nick
	 * @param cb Player callback
	 */
    
    
	public synchronized void addNick(String nick, ProtocolCallback<StringMessage> cb){ 
		if(!returnContainsNick(nick)){
			nicks.add(nick);
			try {
				cb.sendMessage(new StringMessage("SYSMSG NICK ACCEPTED"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param nick String of the nick
	 * @return true if the nick is in the TBGPSingleton
	 */
	
	public boolean returnContainsNick(String nick){
		return nicks.contains(nick);
	}
	
	/**
	 * Join one player to room, if its new room create it. if the player is in the room remove the player from the room
	 * @param room String of the room name
	 * @param cb player callback
	 * @param nick String of the nick
	 */
	
	public void joinRoom(String room, ProtocolCallback<StringMessage> cb, String nick){
		if(!isInRoom.contains(cb)){
			if(!rooms.containsKey(room)){
				synchronized (addingRoom) {
					if(!rooms.containsKey(room))
						rooms.put(room, new Room(room));
				}
			}
			boolean added = rooms.get(room).addPlayer(cb, nick);
			if(added){
				usersCallbacks.put(cb, rooms.get(room));
			}
			isInRoom.add(cb);
		}
		else{
			synchronized (gameStarterRoomLeaver) {
				if(!usersCallbacks.get(cb).getBeeingPlayed()){
					usersCallbacks.get(cb).removePlayer(cb);
					if(!rooms.containsKey(room)){
						synchronized (addingRoom) {
							if(!rooms.containsKey(room))
								rooms.put(room, new Room(room));
						}
					}
					boolean added = rooms.get(room).addPlayer(cb, nick);
					if(added){
						usersCallbacks.put(cb, rooms.get(room));
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @return a list of all the games
	 */
	
	public String sendGames(){
		Set<String> theGames = games.keySet();
		String ans = "";
		Iterator<String> game = theGames.iterator();
		while(game.hasNext()){
			ans= ans+" "+game.next();
		}
		return ans;
	}
	
	/**
	 * Start a new game in the room
	 * @param gameName String of the game name
	 * @param cb callback of the player
	 */
	
	public void gameStarter(String gameName, ProtocolCallback<StringMessage> cb){
		synchronized (gameStarterRoomLeaver) {
			if(games.containsKey(gameName.toUpperCase()) && usersCallbacks.containsKey(cb)){
				Game toStart = games.get(gameName.toUpperCase()).gameGenerator();
				usersCallbacks.get(cb).startGame(toStart, cb);
			}
			else{
				try {
					cb.sendMessage(new StringMessage("SYSMSG STARTGAME REJECTED"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Send a msg to all the players in the room
	 * @param msg What ge want to send
	 * @param cb sender callback
	 * @param nick the nick of the sender
	 */
	
	public void chating(String msg, ProtocolCallback<StringMessage> cb, String nick){
		if(isInRoom.contains(cb))
			usersCallbacks.get(cb).chatMessage(msg, cb, nick);
		else{
			try {
				cb.sendMessage(new StringMessage("SYSMSG MSG REJECTED you're not in a room"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Send response to the room
	 * @param response String of the response
	 * @param cb player callback
	 */
	public void gameInteractor (String response, ProtocolCallback<StringMessage> cb){
		usersCallbacks.get(cb).playing(response, cb);
	}
	/**
	 * Send response to the room
	 * @param response int of the response
	 * @param cb player callback
	 */
	public void gameInteractor (int response, ProtocolCallback<StringMessage> cb){
		usersCallbacks.get(cb).playing(response, cb);
	}
	
	/**
	 * 	Quit the room
	 * @param cb player callback
	 * @param nick String of the nick
	 * @return true if he quit
	 */
	
	public boolean quit(ProtocolCallback<StringMessage> cb, String nick){
		if(nick.equals("")){
			try {
				cb.sendMessage(new StringMessage("SYSMSG QUIT ACCEPTED"));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		else{
			if(usersCallbacks.containsKey(cb)){
				if(!usersCallbacks.get(cb).getBeeingPlayed()){
					usersCallbacks.get(cb).removePlayer(cb);
					usersCallbacks.remove(cb);
					nicks.remove(nick);
					try {
						cb.sendMessage(new StringMessage("SYSMSG QUIT ACCEPTED"));
						return true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					try {
						cb.sendMessage(new StringMessage("SYSMSG QUIT REJECTED you're in the middle of the game !"));
						return false;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else{
				nicks.remove(nick);
				try {
					cb.sendMessage(new StringMessage("SYSMSG QUIT ACCEPTED"));
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
		
	}
}
