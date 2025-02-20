import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
public class text {
    public static void main(String[] args) { // 진입부분
        // 이게 있어야 이 클래스를 실행했을 때 작동을 함
        // 웹훅을 만들 거임 -> URL 필요함
        // 환경변수로 받아올 것임 -> yml 파일에서 전달하게
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        String slackMessage = System.getenv("SLACK_WEBHOOK_MSG");
        String apiKey = System.getenv("GEMINI_API_KEY");  // Gemini API 키

        // Gemini API URL을 수정하여 API 키를 포함합니다.
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // 요청 본문 형식 (curl 요청에 맞게 수정)
        String messageRequest = "{\n" +
            "  \"contents\": [\n" +
            "    {\n" +
            "      \"parts\": [\n" +
            "        {\"text\": \"유튜브 짐승 친구들 땅땅이가 자기소개하는 내용 적어줘\"}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        // Java 11 -> fetch
        HttpClient client = HttpClient.newHttpClient();
        // 요청을 얹힐 거다
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create(webhookUrl))
        //     .header("Content-Type", "application/json")
        //     .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + message + "\"}"))
        //     .build();

        HttpRequest geminiRequest = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(messageRequest)) // 요청 본문
            .build();
        
        // try {
        //     HttpResponse<String> response = client.send(
        //         request, HttpResponse.BodyHandlers.ofString()
        //     );
        //     System.out.println("요청 코드: " + response.statusCode());
        //     System.out.println("응답 결과: " + response.body());
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        try {
            HttpResponse<String> geminiResponse = client.send(geminiRequest, HttpResponse.BodyHandlers.ofString());

            if (geminiResponse.statusCode() == 200) {
                String geminiResponseBody = geminiResponse.body();
                String geminiText = extractTextFromGeminiResponse(geminiResponseBody);
                System.out.println("Gemini Response Text: " + geminiText);

                String slackMessageBody = "{\"text\": \"" + slackMessage + " " + geminiText + "\"}";

                HttpRequest slackRequest = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(slackMessageBody))
                    .build();

                HttpResponse<String> slackResponse = client.send(slackRequest, HttpResponse.BodyHandlers.ofString());

                if (slackResponse.statusCode() == 200) {
                    System.out.println("Slack 메시지가 성공적으로 전송되었습니다!");
                } else {
                    System.err.println("Slack으로 메시지 전송 오류. 상태 코드: " + slackResponse.statusCode());
                }
            } else {
                System.err.println("Gemini API 오류: 상태 코드 " + geminiResponse.statusCode());
                System.err.println("Gemini API 응답: " + geminiResponse.body()); // 오류 메시지 출력 추가
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractTextFromGeminiResponse(String geminiResponseBody) {
        String text = "";
        try {
            JSONObject responseJson = new JSONObject(geminiResponseBody);
            text = responseJson.getJSONArray("candidates")
                               .getJSONObject(0)
                               .getJSONObject("content")
                               .getJSONArray("parts")
                               .getJSONObject(0)
                               .getString("text");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
    
}

