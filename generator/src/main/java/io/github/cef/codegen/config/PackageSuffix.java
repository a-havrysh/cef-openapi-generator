package io.github.cef.codegen.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Package suffixes for architectural layers.
 * Appended to the base API package to form layer-specific packages.
 */
@Getter
@RequiredArgsConstructor
public enum PackageSuffix {

    PROTOCOL(".protocol"),
    ROUTING(".routing"),
    CEF(".cef"),
    UTIL(".util"),
    EXCEPTION(".exception"),
    VALIDATION(".validation"),
    INTERCEPTOR(".interceptor");

    private final String value;
}
