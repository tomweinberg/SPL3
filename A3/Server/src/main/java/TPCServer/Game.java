package TPCServer;
import java.util.ArrayList;

import Reactor.tokenizer.StringMessage;
/**
 * interface of Game
 * 
 */
public interface Game {
	public Game gameGenerator();
	public void start(Room room);
	public void response (String answerFromClient, ProtocolCallback<StringMessage> cb);
	public void response (int answerFromClient, ProtocolCallback<StringMessage> cb);
}
