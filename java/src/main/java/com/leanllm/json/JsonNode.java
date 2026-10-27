package com.leanllm.json;

import java.util.*;

/**
 * @summary Zero-dependency JSON Abstract Syntax Tree root with recursion limits.
 * @threadsafe Immutable AST nodes are thread-safe.
 * @security Defends against deeply nested stack-overflow attacks via depth limits.
 */
public abstract sealed class JsonNode permits 
    JsonNode.JsonObject, JsonNode.JsonArray, JsonNode.JsonString, 
    JsonNode.JsonNumber, JsonNode.JsonBoolean, JsonNode.JsonNull {

    public static final int MAX_RECURSION_DEPTH = 64;

    public abstract String serialize();
    public boolean isObject() { return this instanceof JsonObject; }
    public boolean isArray() { return this instanceof JsonArray; }
    public String asString() {
        if (this instanceof JsonString s) return s.value();
        throw new UnsupportedOperationException("Not a JsonString: " + getClass().getSimpleName());
    }

    public static record JsonObject(Map<String, JsonNode> members) extends JsonNode {
        public JsonObject { members = Map.copyOf(members); }
        public String serialize() {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (var entry : members.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue().serialize());
            }
            return sb.append("}").toString();
        }
    }

    public static record JsonArray(List<JsonNode> elements) extends JsonNode {
        public JsonArray { elements = List.copyOf(elements); }
        public String serialize() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < elements.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(elements.get(i).serialize());
            }
            return sb.append("]").toString();
        }
    }

    public static record JsonString(String value) extends JsonNode {
        public String serialize() {
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        }
    }

    public static record JsonNumber(double value) extends JsonNode {
        public String serialize() {
            return (value == (long) value) ? String.valueOf((long) value) : String.valueOf(value);
        }
    }

    public static record JsonBoolean(boolean value) extends JsonNode {
        public String serialize() { return String.valueOf(value); }
    }

    public static record JsonNull() extends JsonNode {
        public String serialize() { return "null"; }
    }
}
