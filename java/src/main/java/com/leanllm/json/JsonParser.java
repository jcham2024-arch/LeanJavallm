package com.leanllm.json;

import java.util.*;

/**
 * @summary Zero-dependency recursive-descent JSON parser.
 * @threadsafe Thread-safe when used via static parse() entrypoint.
 * @security Limits recursion depth to prevent stack exhaustion on adversarial inputs.
 */
public final class JsonParser {
    private final String src;
    private int idx = 0;
    private int depth = 0;

    private JsonParser(String src) { this.src = Objects.requireNonNull(src).trim(); }

    public static JsonNode parse(String rawJson) {
        return new JsonParser(rawJson).parseValue();
    }

    private JsonNode parseValue() {
        skipWhitespace();
        if (idx >= src.length()) throw new IllegalArgumentException("Unexpected end of JSON");
        if (++depth > JsonNode.MAX_RECURSION_DEPTH) throw new IllegalStateException("Max JSON depth exceeded");
        try {
            char c = src.charAt(idx);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return new JsonNode.JsonString(parseString());
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            return parseNumber();
        } finally {
            depth--;
        }
    }

    private JsonNode.JsonObject parseObject() {
        idx++; // skip '{'
        Map<String, JsonNode> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') { idx++; return new JsonNode.JsonObject(map); }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            if (consume() != ':') throw new IllegalArgumentException("Expected ':' at " + idx);
            map.put(key, parseValue());
            skipWhitespace();
            char next = consume();
            if (next == '}') break;
            if (next != ',') throw new IllegalArgumentException("Expected ',' or '}' at " + idx);
        }
        return new JsonNode.JsonObject(map);
    }

    private JsonNode.JsonArray parseArray() {
        idx++; // skip '['
        List<JsonNode> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') { idx++; return new JsonNode.JsonArray(list); }
        while (true) {
            list.add(parseValue());
            skipWhitespace();
            char next = consume();
            if (next == ']') break;
            if (next != ',') throw new IllegalArgumentException("Expected ',' or ']' at " + idx);
        }
        return new JsonNode.JsonArray(list);
    }

    private String parseString() {
        consume(); // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (idx < src.length()) {
            char c = consume();
            if (c == '"') return sb.toString();
            if (c == '\\') {
                char esc = consume();
                switch (esc) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> sb.append(esc);
                }
            } else sb.append(c);
        }
        throw new IllegalArgumentException("Unterminated string at " + idx);
    }

    private JsonNode.JsonBoolean parseBoolean() {
        if (src.startsWith("true", idx)) { idx += 4; return new JsonNode.JsonBoolean(true); }
        if (src.startsWith("false", idx)) { idx += 5; return new JsonNode.JsonBoolean(false); }
        throw new IllegalArgumentException("Invalid boolean at " + idx);
    }

    private JsonNode.JsonNull parseNull() {
        if (src.startsWith("null", idx)) { idx += 4; return new JsonNode.JsonNull(); }
        throw new IllegalArgumentException("Invalid null at " + idx);
    }

    private JsonNode.JsonNumber parseNumber() {
        int start = idx;
        if (peek() == '-') idx++;
        while (idx < src.length() && (Character.isDigit(src.charAt(idx)) || src.charAt(idx) == '.')) idx++;
        return new JsonNode.JsonNumber(Double.parseDouble(src.substring(start, idx)));
    }

    private void skipWhitespace() {
        while (idx < src.length() && Character.isWhitespace(src.charAt(idx))) idx++;
    }
    private char peek() { return idx < src.length() ? src.charAt(idx) : '\0'; }
    private char consume() { return src.charAt(idx++); }
}
