
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
            "  \"contents\": [{\n" +
            "    \"parts\": [{\"text\": \"유튜브 짐승 친구들 땅땅이가 자기소개하는 내용 적어줘\"}]\n" +
            "  }]\n" +
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
            // Gemini API 요청을 보내고 응답을 받습니다.
            HttpResponse<String> geminiResponse = client.send(geminiRequest, HttpResponse.BodyHandlers.ofString());

            // 응답이 성공적이면
            if (geminiResponse.statusCode() == 200) {
                // Gemini 응답을 Slack 메시지로 전송
                String geminiResponseBody = geminiResponse.body();
                System.out.println("Gemini Response: " + geminiResponseBody);

                // Slack 웹훅 URL로 메시지를 보냅니다.
                HttpRequest slackRequest = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"text\": \"" + slackMessage + " " + geminiResponseBody + "\"}"))
                    .build();

                // Slack 메시지 전송
                HttpResponse<String> slackResponse = client.send(slackRequest, HttpResponse.BodyHandlers.ofString());
                
                // Slack 메시지 전송 성공 여부 확인
                if (slackResponse.statusCode() == 200) {
                    System.out.println("Slack 메시지가 성공적으로 전송되었습니다!");
                } else {
                    System.err.println("Slack으로 메시지 전송 오류. 상태 코드: " + slackResponse.statusCode());
                }
            } else {
                System.err.println("Gemini API 오류: 상태 코드 " + geminiResponse.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
