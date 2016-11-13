package TPCServer;
/**
 *GameProtocolFactory implements ServerProtocolFactory
 */
public class GameProtocolFactory implements ServerProtocolFactory {
	public AsyncServerProtocol create(){
		return new GameProtocol();
	}
}