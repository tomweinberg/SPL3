package TPCServer;
import java.io.IOException;

public class GameRunner {
	public static void main(String[] args) throws IOException{
		int port = Integer.decode(args[0]).intValue();
		TPCServer server = new TPCServer(port, new GameProtocolFactory());
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Server stopped");
		}
		
		
				
	}
}
