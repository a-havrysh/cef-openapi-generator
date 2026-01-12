package com.intellij.openapi.project;

/**
 * Minimal stub interface for IntelliJ Project.
 * Allows generated code to compile without full IntelliJ Platform SDK.
 */
public interface Project {
    /**
     * Get service by class.
     */
    <T> T getService(Class<T> serviceClass);

    /**
     * Get project name.
     */
    String getName();
}
