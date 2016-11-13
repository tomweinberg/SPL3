package TPCServer;

public interface ServerProtocol <T> {
	
	
	void processMessage(T msg, ProtocolCallback<T> callback);
	
	boolean isEnd(T msg);
}
