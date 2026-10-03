package com.fosder.pocketdimension.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cliente GET pequeño para créditos de traducción comunitarios.
 *
 * La conexión solo se ejecuta en un hilo daemon, acepta HTTPS (o HTTP local
 * para pruebas), limita el tamaño de respuesta y nunca envía credenciales.
 */
public final class PocketTranslationApiClient {
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;
    private static final int MAX_ENTRIES = 32;
    private static final int MAX_TEXT_LENGTH = 96;
    private static final String CACHE_FILE = "pocketdimension_translation_cache.json";

    private PocketTranslationApiClient() {}

    public interface Callback {
        void onSuccess(List<RemoteCredit> credits);

        void onFailure(String reason);
    }

    public static final class RemoteCredit {
        private final String code;
        private final String language;
        private final String author;
        private final String status;

        private RemoteCredit(String code, String language, String author, String status) {
            this.code = code;
            this.language = language;
            this.author = author;
            this.status = status;
        }

        public String getCode() {
            return code;
        }

        public String getLanguage() {
            return language;
        }

        public String getAuthor() {
            return author;
        }

        public String getStatus() {
            return status;
        }
    }

    public static void fetch(String endpoint, int timeoutMs, Callback callback) {
        final String safeEndpoint = endpoint == null ? "" : endpoint.trim();
        Thread worker = new Thread(() -> {
            try {
                List<RemoteCredit> credits = request(safeEndpoint, timeoutMs);
                saveCache(credits);
                callback.onSuccess(credits);
            } catch (Exception exception) {
                callback.onFailure(exception.getClass().getSimpleName());
            }
        }, "PocketDimension-TranslationAPI");
        worker.setDaemon(true);
        worker.start();
    }

    public static List<RemoteCredit> loadCached() {
        try {
            Path cache = cachePath();
            if (!Files.isRegularFile(cache)) {
                return Collections.emptyList();
            }
            byte[] bytes = Files.readAllBytes(cache);
            if (bytes.length > MAX_RESPONSE_BYTES) {
                return Collections.emptyList();
            }
            return parse(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private static List<RemoteCredit> request(String endpoint, int timeoutMs) throws Exception {
        URI uri = URI.create(endpoint);
        if (!isAllowed(uri)) {
            throw new IOException("Only HTTPS or local HTTP endpoints are allowed");
        }

        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "PocketDimension/1.0.3");
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("HTTP " + responseCode);
            }
            return parse(readLimited(connection.getInputStream()));
        } finally {
            connection.disconnect();
        }
    }

    private static boolean isAllowed(URI uri) {
        if (uri == null || uri.getHost() == null) return false;
        String scheme = uri.getScheme();
        if ("https".equalsIgnoreCase(scheme)) return true;
        if (!"http".equalsIgnoreCase(scheme)) return false;
        String host = uri.getHost();
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host);
    }

    private static String readLimited(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                if (output.size() + read > MAX_RESPONSE_BYTES) {
                    throw new IOException("Response too large");
                }
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static List<RemoteCredit> parse(String json) {
        JsonElement root = new JsonParser().parse(json);
        JsonArray entries;
        if (root.isJsonArray()) {
            entries = root.getAsJsonArray();
        } else if (root.isJsonObject() && root.getAsJsonObject().get("translations") != null
                && root.getAsJsonObject().get("translations").isJsonArray()) {
            entries = root.getAsJsonObject().getAsJsonArray("translations");
        } else if (root.isJsonObject() && root.getAsJsonObject().get("credits") != null
                && root.getAsJsonObject().get("credits").isJsonObject()) {
            entries = new JsonArray();
            for (java.util.Map.Entry<String, JsonElement> credit : root.getAsJsonObject()
                    .getAsJsonObject("credits").entrySet()) {
                if (!credit.getValue().isJsonObject()) continue;
                JsonObject object = credit.getValue().getAsJsonObject();
                object.addProperty("code", credit.getKey());
                entries.add(object);
            }
        } else {
            throw new IllegalArgumentException("Expected translations or credits JSON");
        }

        List<RemoteCredit> credits = new ArrayList<>();
        for (JsonElement element : entries) {
            if (credits.size() >= MAX_ENTRIES || !element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            String code = clean(object, "code");
            String language = clean(object, "language");
            String author = clean(object, "author");
            if (author.isEmpty()) author = clean(object, "translator");
            String status = clean(object, "status");
            if (status.isEmpty()) status = "Community translation";
            if (!code.isEmpty() && !language.isEmpty() && !author.isEmpty()) {
                credits.add(new RemoteCredit(code, language, author, status));
            }
        }
        if (credits.isEmpty()) {
            throw new IllegalArgumentException("No valid translation credits");
        }
        return credits;
    }

    private static String clean(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) return "";
        String value = object.get(key).getAsString().replace('\n', ' ').replace('\r', ' ').trim();
        return value.length() > MAX_TEXT_LENGTH ? value.substring(0, MAX_TEXT_LENGTH) : value;
    }

    private static void saveCache(List<RemoteCredit> credits) {
        try {
            JsonArray entries = new JsonArray();
            for (RemoteCredit credit : credits) {
                JsonObject object = new JsonObject();
                object.addProperty("code", credit.code);
                object.addProperty("language", credit.language);
                object.addProperty("author", credit.author);
                object.addProperty("status", credit.status);
                entries.add(object);
            }
            JsonObject root = new JsonObject();
            root.add("translations", entries);
            Path cache = cachePath();
            Files.createDirectories(cache.getParent());
            Files.write(cache, root.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // La caché es opcional: un error de escritura no debe cerrar la pantalla.
        }
    }

    private static Path cachePath() {
        Path gameDirectory = Paths.get(Minecraft.getInstance().gameDirectory.toURI());
        return gameDirectory.resolve("config").resolve(CACHE_FILE);
    }
}
