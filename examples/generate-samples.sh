#!/bin/bash
# Generates sample output from test-openapi.yaml for documentation purposes
# Run from project root: ./examples/generate-samples.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SPEC="$PROJECT_DIR/generator/src/test/resources/test-openapi.yaml"

echo "Building generator..."
cd "$PROJECT_DIR"
./gradlew :generator:publishToMavenLocal -q

echo "Generating Kotlin sample..."
rm -rf "$SCRIPT_DIR/generated-kotlin"
cd "$PROJECT_DIR"
java -cp "generator/build/libs/*:generator/build/resources/main" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g cef-kotlin \
  -i "$SPEC" \
  -o "$SCRIPT_DIR/generated-kotlin" \
  --api-package com.example.api \
  --model-package com.example.api.dto \
  --additional-properties hideGenerationTimestamp=true 2>/dev/null || true

echo "Generating Java sample..."
rm -rf "$SCRIPT_DIR/generated-java"
java -cp "generator/build/libs/*:generator/build/resources/main" \
  org.openapitools.codegen.OpenAPIGenerator generate \
  -g cef \
  -i "$SPEC" \
  -o "$SCRIPT_DIR/generated-java" \
  --api-package com.example.api \
  --model-package com.example.api.dto \
  --additional-properties hideGenerationTimestamp=true,generateBuilders=true 2>/dev/null || true

echo "Done. Check examples/generated-kotlin/ and examples/generated-java/"
