package TPCServer;
import java.io.IOException;

import Reactor.tokenizer.StringMessage;
/**
 * 
 *Protocol of Game base on text implement AsyncServerProtocol
 *
 */
 
class GameProtocol implements AsyncServerProtocol<StringMessage> {
	private String nick;
	private boolean _shouldClose = false;
	private boolean _connectionTerminated = false;
	/**
	 * 
	 *create new  GameProtocol
	 *
	 */
	public GameProtocol() {
		nick="";
	}
	/**
	 * return true if the game is end
	 * @param msg the input from the player
	 *
	 */
	public boolean isEnd(StringMessage msg)
	{
		return msg.getMessage().equals("QUIT");
	}
	/**
	 * Get input from the player and send it to the TBGP Singleton
	 * @param msg the input from the player
	 * @param callback different callback for every player
	 *
	 */
	@Override
	public void processMessage(StringMessage msg, ProtocolCallback<StringMessage> callback) {
		String[] firstWord = msg.getMessage().split(" ");
		String msgWithoutFirstWord="";
		
		if(firstWord.length>1){
			msgWithoutFirstWord = msg.getMessage().substring(firstWord[0].length()+1, msg.getMessage().length());
		}
		
		if(firstWord[0].equals("NICK")){
			if(TBGPSingleton.getInstance().returnContainsNick(firstWord[1]) || !nick.equals("")){
				try {
					callback.sendMessage(new StringMessage("SYSMSG NICK REJECTED"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				TBGPSingleton.getInstance().addNick(firstWord[1], callback);
				nick = firstWord[1];
			}
		}
		
		else if(firstWord[0].equals("JOIN")){
			if(!nick.equals("") && !firstWord[1].equals(null))
				TBGPSingleton.getInstance().joinRoom(firstWord[1], callback, nick);
			else{
				try {
					callback.sendMessage(new StringMessage("SYSMSG JOIN REJECTED"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		else if(firstWord[0].equals("LISTGAMES")){
			String res = TBGPSingleton.getInstance().sendGames();
			if(res.equals("")){
				try {
					callback.sendMessage(new StringMessage("SYSMSG LISTGAMES REJECTED"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				try {
					callback.sendMessage(new StringMessage("SYSMSG LISTGAMES ACCEPTED "+res));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(firstWord[0].equals("STARTGAME")){
			TBGPSingleton.getInstance().gameStarter(firstWord[1], callback);
		}
		else if(firstWord[0].equals("MSG")){
			if(!nick.equals("")){
				TBGPSingleton.getInstance().chating(msgWithoutFirstWord, callback, nick);
			}
			else{
				try {
					callback.sendMessage(new StringMessage("SYSMSG MSG REJECTED"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if(firstWord[0].equals("TXTRESP")){
			TBGPSingleton.getInstance().gameInteractor(msgWithoutFirstWord, callback);
		}
		else if(firstWord[0].equals("SELECTRESP")){
			int response=-1;
			try{
				response = new Integer(msgWithoutFirstWord).intValue();
			}catch(Exception e){
				try {
					callback.sendMessage(new StringMessage("SYSMSG SELECTRESP REJECTED you need to enter a number"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(response!= -1)
				TBGPSingleton.getInstance().gameInteractor(response, callback);
		}
		else if(firstWord[0].equals("QUIT")){
			if(isEnd(msg)){
				if(TBGPSingleton.getInstance().quit(callback, nick)){
					System.out.println("quit true");
					_shouldClose = true;
				}
			}
		}
		else{
			try {
				callback.sendMessage(new StringMessage("SYSMSG "+firstWord[0]+" REJECTED not vaild expression"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public boolean shouldClose() {
		return this._shouldClose;
	}


	public void connectionTerminated() {
		this._connectionTerminated = true;
	}


}
