package main;

import java.nio.charset.Charset;

import Reactor.reactor.Reactor;
import Reactor.tokenizer.FixedSeparatorMessageTokenizer;
import Reactor.tokenizer.MessageTokenizer;
import Reactor.tokenizer.StringMessage;
import Reactor.tokenizer.TokenizerFactory;
import TPCServer.GameProtocolFactory;
import TPCServer.ServerProtocolFactory;

public class RunningServer {
	public static void main(String args[]) {
        if (args.length != 2) {
            System.err.println("Usage: java Reactor <port> <pool_size>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            int poolSize = Integer.parseInt(args[1]);

            Reactor<StringMessage> reactor = startTBGPServer(port, poolSize);

            Thread thread = new Thread(reactor);
            thread.start();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Reactor<StringMessage> startTBGPServer(int port, int poolSize) {
        ServerProtocolFactory<StringMessage> protocolMaker = new GameProtocolFactory();


        final Charset charset = Charset.forName("UTF-8");
        TokenizerFactory<StringMessage> tokenizerMaker = new TokenizerFactory<StringMessage>() {
            public MessageTokenizer<StringMessage> create() {
                return new FixedSeparatorMessageTokenizer("\n", charset);
            }
        };

        Reactor<StringMessage> reactor = new Reactor<StringMessage>(port, poolSize, protocolMaker, tokenizerMaker);
        return reactor;
    }
}
