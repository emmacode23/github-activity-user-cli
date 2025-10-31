import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubActivity {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java GithubActivity <github_username>");
            return;
        }

        String username = args[0];
        String apiUrl = "https://api.github.com/users/" + username + "/events";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Java GitHubActivity CLI");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println(" Erreur : impossible de récupérer les données (" + responseCode + ")");
                return;
            }
            //LE buffered (le tampon)reader va nou permetre de lire des texte de plus grande quantites
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String json = response.toString();

            String[] events = json.split("\\},\\{");

            System.out.println("\n GitHub Activity for user: " + username + "\n");

            for (String event : events) {
                if (event.contains("\"type\":\"PushEvent\"")) {
                    String repo = extractRepo(event);
                    int commits = countOccurrences(event, "\"message\"");
                    System.out.println("- Pushed " + commits + " commits to " + repo);
                } else if (event.contains("\"type\":\"IssuesEvent\"")) {
                    String repo = extractRepo(event);
                    System.out.println("- Opened a new issue in " + repo);
                } else if (event.contains("\"type\":\"WatchEvent\"")) {
                    String repo = extractRepo(event);
                    System.out.println("- Starred " + repo);
                } else if (event.contains("\"type\":\"ForkEvent\"")) {
                    String repo = extractRepo(event);
                    System.out.println("- Forked " + repo);
                }
            }

        } catch (Exception e) {
            System.out.println(" Erreur : " + e.getMessage());
        }
    }

  private static String extractRepo(String event) {
        int start = event.indexOf("\"repo\":{\"name\":\"");
        if (start == -1) return "unknown repo";
        start += "\"repo\":{\"name\":\"".length();
        int end = event.indexOf("\"", start);
        return event.substring(start, end);
    }

    private static int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}
