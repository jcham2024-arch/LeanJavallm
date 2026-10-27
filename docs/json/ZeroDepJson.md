---
id: JSON-01
title: "Zero-Dependency JSON AST & Parser"
type: sealed class / utility
module: com.leanllm.json
context_cost: 380
tags: [json, ast, zero-dependency, parser]
related_links:
  - "[[docs/domain/MessageProtocol]]"
  - "[[docs/client/InferenceClients]]"
---

# Zero-Dependency JSON AST & Parser

## Overview
A standalone JSON Abstract Syntax Tree (`JsonNode`) and recursive-descent parser (`JsonParser`) operating strictly within `java.base`.

## Architectural Rationale
Eliminating external dependencies (Jackson, Gson) avoids supply chain CVEs and version drift. The custom parser implements recursion depth protection (`MAX_RECURSION_DEPTH = 64`) against malicious stack overflow payloads.

## Subtype Records
- `JsonObject(Map<String, JsonNode> members)`
- `JsonArray(List<JsonNode> elements)`
- `JsonString(String value)`
- `JsonNumber(double value)`
- `JsonBoolean(boolean value)`
- `JsonNull()`

## Interconnections
- Used by: [[docs/domain/MessageProtocol]] for Tool Schema declarations.
- Consumed by: [[docs/client/InferenceClients]] for request/response serialization.