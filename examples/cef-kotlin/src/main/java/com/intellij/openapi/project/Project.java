package com.intellij.openapi.project;

/**
 * Minimal stub for IntelliJ Project interface.
 * Allows generated code to compile without IntelliJ Platform SDK.
 */
public interface Project {
    <T> T getService(Class<T> serviceClass);
    String getName();
}
