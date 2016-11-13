package JsonHandle;

public class Question {
	private String question;
	private String realAnswer;
	
	public Question(String q, String a){
		question=q;
		realAnswer= a;
	}

	public String getQuestion() {
		return question;
	}

	public String getRealAnswer() {
		return realAnswer;
	}
}
