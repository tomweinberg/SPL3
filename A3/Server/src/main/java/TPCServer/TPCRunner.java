package TPCServer;

public class TPCRunner {
	public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("Usage: java TPC <port>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);


            TPCServer server = new TPCServer(port, new GameProtocolFactory());

            Thread thread = new Thread(server);
            thread.start();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
