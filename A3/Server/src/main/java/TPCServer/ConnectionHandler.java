package TPCServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;import java.nio.ByteBuffer;

import Reactor.tokenizer.StringMessage;


class ConnectionHandler implements Runnable {
	
	private BufferedReader in;
	private PrintWriter out;
	Socket clientSocket;
	ServerProtocol<StringMessage> protocol;
	
	public ConnectionHandler(Socket acceptedSocket, ServerProtocol<StringMessage> p)
	{
		in = null;
		out = null;
		clientSocket = acceptedSocket;
		protocol = p;
		System.out.println("Accepted connection from client!");
		System.out.println("The client is from: " + acceptedSocket.getInetAddress() + ":" + acceptedSocket.getPort());
	}
	
	public void run()
	{

		String msg;
		
		try {
			initialize();
		}
		catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}

		try {
			process();
		} 
		catch (IOException e) {
			System.out.println("Error in I/O");
		} 
		
		System.out.println("Connection closed - bye bye...");
		close();

	}
	
	public void process() throws IOException
	{
		String msg;
		ProtocolCallback<StringMessage> callback = new ProtocolCallback<StringMessage>() {			
			public void sendMessage(StringMessage m) throws IOException {
				out.println(m.getMessage());	
			}
		};
		
		while ((msg = in.readLine()) != null)
		{
			System.out.println("Received \"" + msg + "\" from client");
			StringMessage msgmsg = new StringMessage(msg);
			protocol.processMessage(msgmsg, callback);
			if(protocol.isEnd(msgmsg)){
				break;
			}
		}
	}
	
	// Starts listening
	public void initialize() throws IOException
	{
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8"), true);
		System.out.println("I/O initialized");
	}
	
	// Closes the connection
	public void close()
	{
		try {
			if (in != null)
			{
				in.close();
			}
			if (out != null)
			{
				out.close();
			}
			
			clientSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("Exception in closing I/O");
		}
	}
	
}