
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
        String message = System.getenv("SLACK_WEBHOOK_MSG");

        // Java 11 -> fetch
        HttpClient client = HttpClient.newHttpClient();
        // 요청을 얹힐 거다
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + message + "\"}"))
            .build();
        
        try {
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
            );
            System.out.println("요청 코드: " + response.statusCode());
            System.out.println("응답 결과: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
