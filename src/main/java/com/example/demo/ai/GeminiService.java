package com.example.demo.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Implementarea AI-ului descris in README, folosind Google Gemini.
 *
 * Are doua responsabilitati:
 * 1. genereaza textul unei lecturi educationale + minitestul (3 intrebari grila) - vezi generateReading();
 * 2. verifica poza trimisa ca dovada pentru o sarcina casnica (Gemini Vision) - vezi checkProof().
 *
 * Cheia API se citeste din variabila de mediu GEMINI_API_KEY (proprietatea gemini.api-key).
 * Fara cheie, endpoint-urile care folosesc AI-ul raspund cu 503 si un mesaj explicit.
 */
@Service
public class GeminiService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GeminiService(@Value("${gemini.api-key:}") String apiKey,
                         @Value("${gemini.model:gemini-2.5-flash}") String model,
                         @Value("${gemini.url:https://generativelanguage.googleapis.com}") String baseUrl) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    /**
     * Genereaza lectura si minitestul pentru subiectul ales de copil.
     * Rezultatul este parsate din JSON-ul intors de Gemini.
     */
    public Reading generateReading(String topic, String userName) {
        String prompt = """
                Esti un profesor prietenos care vorbeste cu un copil de 10 ani.
                Scrie un text educativ, vesel si usor de citit (maximum 200 de cuvinte) despre: "%s".
                Adreseaza-te copilului pe numele "%s".
                Apoi creeaza exact 3 intrebari grila despre text, fiecare cu 3 variante (A, B, C).
                Raspunde DOAR cu JSON: {"text": "...", "questions": [{"question": "...",
                "options": ["...", "...", "..."], "correctOption": "A"}]}
                """.formatted(topic, userName);

        String json = callGemini(prompt, null, null);

        JsonNode root = objectMapper.readTree(json);
        String text = root.path("text").asText("");
        List<Question> questions = new ArrayList<>();
        for (JsonNode node : root.path("questions")) {
            JsonNode options = node.path("options");
            if (options.size() < 3) {
                continue;
            }
            questions.add(new Question(
                    node.path("question").asText(""),
                    options.get(0).asText(),
                    options.get(1).asText(),
                    options.get(2).asText(),
                    normalizeOption(node.path("correctOption").asText())));
        }
        if (text.isBlank() || questions.size() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Gemini nu a returnat o lectura valida. Incearca din nou.");
        }
        return new Reading(text, questions);
    }

    /**
     * Trimite poza dovezii catre Gemini Vision si cere un verdict: aprobat sau nu + feedback pentru copil.
     */
    public Proof checkProof(byte[] photo, String contentType, String taskTitle, String taskDescription,
                            String userName) {
        String prompt = """
                Esti un inspector prietenos pentru sarcinile casnice ale copilului "%s".
                Sarcina: "%s" - %s.
                Analizeaza fotografia si decide daca munca pare terminata.
                Raspunde DOAR cu JSON: {"approved": true sau false, "feedback": "mesaj scurt in romana"}
                """.formatted(userName, taskTitle, taskDescription);

        String json = callGemini(prompt, photo, contentType);

        JsonNode root = objectMapper.readTree(json);
        String feedback = root.path("feedback").asText("").trim();
        if (feedback.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Gemini nu a returnat un feedback valid. Incearca din nou.");
        }
        return new Proof(root.path("approved").asBoolean(false), feedback);
    }

    /**
     * Construieste cererea HTTP catre Gemini si extrage textul din raspuns.
     * Daca primei parti i se adauga o imagine (inline_data), cererea devine una cu vision.
     */
    private String callGemini(String prompt, byte[] image, String contentType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI-ul nu este configurat. Seteaza variabila de mediu GEMINI_API_KEY.");
        }

        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode content = body.putArray("contents").addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);
        if (image != null && image.length > 0) {
            ObjectNode inlineData = parts.addObject().putObject("inline_data");
            inlineData.put("mime_type", contentType == null || contentType.isBlank() ? "image/jpeg" : contentType);
            inlineData.put("data", Base64.getEncoder().encodeToString(image));
        }
        body.putObject("generationConfig").put("responseMimeType", "application/json");

        String uri = "%s/v1beta/models/%s:generateContent?key=%s".formatted(baseUrl, model, apiKey);
        String response;
        try {
            response = restClient.post()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Apelul catre Gemini a esuat: " + ex.getMessage());
        }

        String text = objectMapper.readTree(response)
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText("");
        if (text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Raspunsul Gemini este gol.");
        }
        return extractJson(text);
    }

    /**
     * Gemini poate adauga text in jurul JSON-ului; pastram doar obiectul JSON.
     */
    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        return start >= 0 && end > start ? raw.substring(start, end + 1) : raw;
    }

    /**
     * Varianta corecta este mereu A, B sau C; orice altceva este tratat ca A.
     */
    private String normalizeOption(String value) {
        String option = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return switch (option) {
            case "A", "B", "C" -> option;
            default -> "A";
        };
    }

    /** Textul lecturii + intrebarile minitestului. */
    public record Reading(String text, List<Question> questions) {
    }

    /** O intrebare grila cu exact 3 variante. */
    public record Question(String question, String optionA, String optionB, String optionC,
                           String correctOption) {
    }

    /** Verdictul AI-ului pentru o poza: aprobat/respins + mesaj pentru copil. */
    public record Proof(boolean approved, String feedback) {
    }
}
