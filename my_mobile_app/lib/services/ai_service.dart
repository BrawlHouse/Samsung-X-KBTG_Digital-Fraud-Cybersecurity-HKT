import 'package:http/http.dart' as http;

class AIService {
  static Future<String> askAI(String context, String question) async {
    // call backend AI
    final response = await http.post(
      Uri.parse("https://your-backend.com/ai"),
      body: {"context": context, "question": question},
    );
    return response.body;
  }
}
