package JsonHandle;

import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class JsonClass implements JsonDeserializer<QuestionsArray> {
	
	public QuestionsArray deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		JsonArray jsonQuestionArray = jsonObject.get("questions").getAsJsonArray();
		Question[] questions = new Question[jsonQuestionArray.size()];
		for (int i = 0; i < questions.length; i++) {
		      JsonElement jsonQuestion = jsonQuestionArray.get(i);
		      JsonObject QuestionObject = jsonQuestion.getAsJsonObject();
		      JsonElement questionElement = QuestionObject.get("questionText");
		      String question = questionElement.getAsString();
		      JsonElement answerElement = QuestionObject.get("realAnswer");
		      String realAnswer = answerElement.getAsString();
		      questions[i] = new Question(question, realAnswer);
		}
		return new QuestionsArray(questions);
	}
}
