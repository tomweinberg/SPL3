package TPCServer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Reactor.tokenizer.StringMessage;


public class Room {
	private String roomName;
	private ArrayList<ProtocolCallback<StringMessage>> players;
	private ConcurrentHashMap <ProtocolCallback<StringMessage>, String> nicks;
	private Game theGame;
	private boolean beeingPlayed;
	private String expectedRespond;
/**
 * Room in the server
 * @param roomName the name of the room
 */
	public Room(String roomName){
		this.roomName=roomName;
		players= new ArrayList<ProtocolCallback<StringMessage>>();
		theGame = null;
		beeingPlayed = false;
		nicks= new ConcurrentHashMap<ProtocolCallback<StringMessage>, String>();
		expectedRespond = null;
	}
	/**
	 * return the nick the the player  
	 * @param cb the callback of the player
	 */
	public String getNick(ProtocolCallback<StringMessage> cb){
		return nicks.get(cb);
	}
	/**
	 * add one player to the room the
	 * @param cb the callback of the player
	 * @param nick string of the nick
	 * 
	 * @return true if the player was add to the room 
	 */
	public boolean addPlayer(ProtocolCallback<StringMessage> cb, String nick){
		if(!beeingPlayed){
			players.add(cb);
			nicks.put(cb, nick);
			try {
				cb.sendMessage(new StringMessage("SYSMSG JOIN ACCEPTED"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		else{
			try {
				cb.sendMessage(new StringMessage("SYSMSG JOIN REJECTED there is a game in progress."));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 *
	 * 
	 * @return true if now there is game playing in the room. 
	 */
	public boolean getBeeingPlayed(){
		return beeingPlayed;
	}
	
	
	/**
	 * Start a game in the room
	 * @param game the game
	 * @param cb callback of the player that start the game
	 */
	public synchronized void startGame(Game game, ProtocolCallback<StringMessage> cb){  
		if(!beeingPlayed){
			theGame = game;
			beeingPlayed=true;
			try {
				cb.sendMessage(new StringMessage("SYSMSG STARTGAME ACCEPTED"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			game.start(this);
		}
		else{
			try {
				cb.sendMessage(new StringMessage("SYSMSG STARTGAME REJECTED someone else started the game"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return Array of the nick that in the room
	 */
	public ArrayList<ProtocolCallback<StringMessage>> getPlayers(){
		return players;
	}
	/**
	 * Send chat Message to all the players in the room
	 * @param msg String of the message
	 * @param cb the callback of the player that send the message
	 * @param nick the nickname of the player that send the message
	 */
	
	public synchronized void chatMessage(String msg, ProtocolCallback<StringMessage> cb, String nick){
		if(!beeingPlayed){
			players.forEach((callback) ->{
				if(callback != cb){
					try {
						callback.sendMessage(new StringMessage("USRMSG "+nick+": "+msg));
					} catch (Exception e) {
					}
				}
			});
			try {
				cb.sendMessage(new StringMessage("SYSMSG MSG ACCEPTED"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			try {
				cb.sendMessage(new StringMessage("SYSMSG MSG REJECTED  you can't chat while playing."));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Send response to the game
	 * @param response String of the response
	 * @param cb callback of the player
	 */
	
	public void playing(String response, ProtocolCallback<StringMessage> cb){
		if(expectedRespond.equals("TXTRESP")){
			theGame.response(response, cb);
		}
		else{
			try {
				cb.sendMessage(new StringMessage("SYSMSG TXTRESP REJECTED you're not supposed to send an answer"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Send response to the game
	 * @param response int of the response
	 * @param cb callback of the player
	 */
	public void playing(int response, ProtocolCallback<StringMessage> cb){
		if(expectedRespond.equals("SELECTRESP")){
			theGame.response(response, cb);
		}
		else{
			try {
				cb.sendMessage(new StringMessage("SYSMSG SELECTRESP REJECTED you're not supposed to choose an answer"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
/**
 * Remove player from the room
 * @param cb callback of the player
 */
	
	public void removePlayer(ProtocolCallback<StringMessage> cb){
		players.remove(cb);
	}
	/**
	 * the expected Answer from the player
	 * @param answer String of the answer
	 */
	
	
	public void expectedAnswerChanger(String answer){
		expectedRespond=answer;
	}
	/**
	 * The game finish
	 */
	
	public void finishGame(){
		beeingPlayed=false;
		theGame=null;
	}
}
