import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import com.fasterxml.jackson.databind.*;

public class QuizLeaderboard {

    static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    static final String REG_NO = "RA2311003020822";

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Set<String> seen = new HashSet<>();
        Map<String, Integer> scores = new HashMap<>();

        // 🔁 Poll 10 times
        for (int i = 0; i < 10; i++) {

            String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + i;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body().trim();

            // ✅ Handle invalid server response
            if (!responseBody.startsWith("{")) {
                System.out.println("Skipping invalid response: " + responseBody);
                Thread.sleep(5000);
                continue;
            }

            JsonNode root = mapper.readTree(responseBody);
            JsonNode events = root.get("events");

            if (events == null || !events.isArray()) {
                System.out.println("No valid events found, skipping...");
                Thread.sleep(5000);
                continue;
            }

            for (JsonNode event : events) {
                String roundId = event.get("roundId").asText();
                String participant = event.get("participant").asText();
                int score = event.get("score").asInt();

                String key = roundId + "_" + participant;

                // ✅ Deduplication
                if (!seen.contains(key)) {
                    seen.add(key);
                    scores.put(participant,
                            scores.getOrDefault(participant, 0) + score);
                }
            }

            Thread.sleep(5000); // ⏳ mandatory delay
        }

        // 📊 Create leaderboard
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (String participant : scores.keySet()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("participant", participant);
            entry.put("totalScore", scores.get(participant));
            leaderboard.add(entry);
        }

        // ❗ IMPORTANT FIX
        if (leaderboard.isEmpty()) {
            System.out.println("No valid data collected. Try running again.");
            return;
        }

        // 🔽 Sort descending
        leaderboard.sort((a, b) ->
                (int) b.get("totalScore") - (int) a.get("totalScore"));

        // 📦 Prepare request
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("regNo", REG_NO);
        requestBody.put("leaderboard", leaderboard);

        String json = mapper.writeValueAsString(requestBody);

        // 📤 Submit result
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResponse =
                client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("Submission Response:");
        System.out.println(postResponse.body());
    }
}