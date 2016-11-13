package TPCServer;

public interface ServerProtocolFactory<T> {
	   ServerProtocol<T> create();
}
