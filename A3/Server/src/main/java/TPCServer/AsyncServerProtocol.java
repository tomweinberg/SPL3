package TPCServer;

public interface AsyncServerProtocol<T> extends ServerProtocol<T> {
	void processMessage (T msg, ProtocolCallback<T> callback);
	
	boolean isEnd(T msg);
	
	boolean shouldClose();
	
	void connectionTerminated();
}
