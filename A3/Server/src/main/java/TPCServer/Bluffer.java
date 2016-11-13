package TPCServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.Random;
import JsonHandle.JsonClass;
import JsonHandle.Question;
import JsonHandle.QuestionsArray;
import Reactor.tokenizer.StringMessage;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * Game Bluffer implements Game
 * 
 */

public class Bluffer implements Game {
	private ArrayList <ProtocolCallback<StringMessage>> callbacks;
	private ConcurrentHashMap <ProtocolCallback<StringMessage>, Integer> players;
	private ConcurrentHashMap <ProtocolCallback<StringMessage>, Integer> pointsForCurrentRound;
	private ConcurrentHashMap <String, ConcurrentLinkedQueue<ProtocolCallback<StringMessage>>> answers;
	private ConcurrentHashMap <ProtocolCallback<StringMessage>, String> answerResponds;
	private String[] choices;
	private int questionCounter;
	private Room room;
	private String currentQuestion;
	private String currentAnswer;
	private AtomicInteger counterAnswer;
	private AtomicInteger counterChoice;
	private int numOfPlayers;
	private String[] thequestions;
	private String[] theanswers;
	private Random randomGenerator;
	
	
	/**
	 * Creates new Bluffer game.
	 * 
	 */
	
	public Bluffer(){
		players = new ConcurrentHashMap<ProtocolCallback<StringMessage>, Integer>();
		questionCounter=0;
		counterAnswer= null;
		counterChoice= null;
		answers = new ConcurrentHashMap<String, ConcurrentLinkedQueue<ProtocolCallback<StringMessage>>>();
		numOfPlayers=0;
		choices=null;
		pointsForCurrentRound = new ConcurrentHashMap<ProtocolCallback<StringMessage>, Integer>();
		callbacks= new ArrayList<ProtocolCallback<StringMessage>>();
		thequestions = new String[3];
		theanswers= new String[3];
		randomGenerator = new Random();
		answerResponds = new ConcurrentHashMap<ProtocolCallback<StringMessage>, String>();
	}
	
	/**
	 * Generates a new Bluffer game instance.
	 * 
	 */
	
	public Game gameGenerator() {
		return new Bluffer();
	}
	/**
	 * start the  game.
	 * @param room the room that the game is playing
	 * 
	 */

	public void start(Room room) {
		for(ProtocolCallback<StringMessage> player: room.getPlayers()){
			callbacks.add(player);
			players.put(player, 0);
			pointsForCurrentRound.put(player, 0);
			numOfPlayers++;
		}
		this.room = room;
		room.expectedAnswerChanger("TXTRESP");              
		BufferedReader jsonReader;
		QuestionsArray questions=null;
		try {
			jsonReader = new BufferedReader(new FileReader("src/main/java/bluffer[3].json"));
			GsonBuilder gsonBuilder = new GsonBuilder();
		    gsonBuilder.registerTypeAdapter(QuestionsArray.class, new JsonClass());
		    Gson gson = gsonBuilder.create();
		    questions = gson.fromJson(jsonReader, QuestionsArray.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for( int i=0; i<3; i++){
			thequestions[i]= questions.getTheQuestions()[i].getQuestion();
			theanswers[i] = questions.getTheQuestions()[i].getRealAnswer();
		}
		sendQuestion();
		counterAnswer= new AtomicInteger(numOfPlayers);
		counterChoice= new AtomicInteger(numOfPlayers);		
	}
	/**
	 * Sends a new question to every client.
	 * 
	 */
	 
	private void sendQuestion(){
			currentQuestion = thequestions[questionCounter];
			currentAnswer = theanswers[questionCounter];
			for(ProtocolCallback<StringMessage> player: players.keySet()){
				try {
					player.sendMessage(new StringMessage("ASKTXT "+currentQuestion));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			questionCounter++;
	}
	/**
	 * Creates a array of the answers of the player with the real answer.
	 * 
	 */
	public String[] choices(){
		if(choices==null){
			String[] tmp = new String[players.size()];
			int i=0;
			for(String answer: answers.keySet()){
				tmp[i]= answer;
				i++;
			}
			int size = answers.keySet().size();
			int rightAnswerPlace = randomGenerator.nextInt(size);
			String[] ans = new String[size+1];
			ans[rightAnswerPlace]= " "+rightAnswerPlace+"."+currentAnswer;
			int h=0;
			for(int j=0; j<ans.length; j++){
				if(j==rightAnswerPlace)
					continue;
				ans[j]=" "+j+"."+tmp[h];
				h++;
			}
			choices = ans;
		}
		return choices;
	}
	/**
	 * add the respond of the player to the array 
@param answerFromClient the answer of the player
@param cb player callback
	 * 
	 */
	public void response(String answerFromClient, ProtocolCallback<StringMessage> cb) {
		if(answerFromClient.equals(currentAnswer)){         
			try {
				cb.sendMessage(new StringMessage("SYSMSG TXTRESP REJECTED your answer is the real answer... please pick a different answer"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}                                                
		else{
			if(answers.containsKey(answerFromClient)){
				answers.get(answerFromClient).add(cb);
			}
			else{
				answers.put(answerFromClient, new ConcurrentLinkedQueue<ProtocolCallback<StringMessage>>());
				answers.get(answerFromClient).add(cb);
			}                                     
			
			try {
				cb.sendMessage(new StringMessage("SYSMSG TXTRESP ACCEPTED"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(counterAnswer.decrementAndGet()==0){
				try {
					choices();
					String toChoose="";
					for(int i=0; i<choices.length; i++){
						toChoose= toChoose+choices[i];
					}
					room.expectedAnswerChanger("SELECTRESP");
					Iterator<ProtocolCallback<StringMessage>> it = callbacks.iterator();
					while(it.hasNext()){
						ProtocolCallback<StringMessage> pcb = it.next();
						pcb.sendMessage(new StringMessage("ASKCHOICES "+toChoose));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get the int of the answer that the player choose 
@param answerFromClient the answer of the player
@param cb player callback
	 * 
	 */
	public void response(int answerFromClient, ProtocolCallback<StringMessage> cb) {
		int size = answers.keySet().size()+1;
		if(answerFromClient<0 || answerFromClient>=size){
			try {
				cb.sendMessage(new StringMessage("SYSMSG SELECTRESP REJECTED your number is not in the right range"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			String theAnswer = choices[answerFromClient].substring(3);
			String theAnswerIs;
			if(theAnswer.equals(currentAnswer)){
				theAnswerIs = "correct! +";
				answerResponds.put(cb, theAnswerIs);
				int newScore = players.get(cb).intValue()+10;
				players.replace(cb, newScore);
				int newPoints = pointsForCurrentRound.get(cb).intValue()+10;
				pointsForCurrentRound.replace(cb, newPoints);
			}
			else{
				theAnswerIs = "wrong! +";
				answerResponds.put(cb, theAnswerIs);
				ConcurrentLinkedQueue<ProtocolCallback<StringMessage>> tmp = answers.get(theAnswer);  
				while(!tmp.isEmpty()){              
					ProtocolCallback<StringMessage> tmpPCB = tmp.remove();
					int newScore = players.get(tmpPCB).intValue()+5;
					players.replace(tmpPCB, newScore);
					int newPoints = pointsForCurrentRound.get(tmpPCB).intValue()+5;
					pointsForCurrentRound.replace(tmpPCB, newPoints);
				}
			}
			try {
				cb.sendMessage(new StringMessage("SYSMSG SELECTRESP ACCEPTED"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(counterChoice.decrementAndGet()==0){
				Iterator<ProtocolCallback<StringMessage>> it = callbacks.iterator();
				while(it.hasNext()){
					ProtocolCallback<StringMessage> pcb = it.next();
					try {
						pcb.sendMessage(new StringMessage("GAMEMSG The correct answer is: "+currentAnswer));
						pcb.sendMessage(new StringMessage("GAMEMSG "+answerResponds.get(pcb)+pointsForCurrentRound.get(pcb).intValue()+"pts."));
						pointsForCurrentRound.replace(pcb, 0);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				answerResponds.clear();
				counterAnswer.set(numOfPlayers);
				counterChoice.set(numOfPlayers);
				choices=null;
				answers.clear();
				if(questionCounter<3){
					room.expectedAnswerChanger("TXTRESP");
					sendQuestion();
				}
				else{
					String endGame="GAMEMSG Summary: ";
					Iterator<ProtocolCallback<StringMessage>> it1 = callbacks.iterator();
					while(it1.hasNext()){
						ProtocolCallback<StringMessage> prot = it1.next();
						endGame= endGame+ room.getNick(prot)+": "+players.get(prot).intValue()+"pts";
						if(it1.hasNext())
							endGame= endGame+", ";
					}
					room.expectedAnswerChanger(null);
					Iterator<ProtocolCallback<StringMessage>> it2 = callbacks.iterator();
					while(it2.hasNext()){
						ProtocolCallback<StringMessage> pcb = it2.next();
						try {
							pcb.sendMessage(new StringMessage(endGame));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					room.finishGame();
				}
			}
		}
	}
}
