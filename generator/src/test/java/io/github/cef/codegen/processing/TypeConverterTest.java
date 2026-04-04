package io.github.cef.codegen.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeConverterTest {

    @Nested
    class DetectType {

        @Test void string()     { assertEquals("String", TypeConverter.detectType(List.of("hello"))); }
        @Test void integer()    { assertEquals("Integer", TypeConverter.detectType(List.of(42))); }
        @Test void longType()   { assertEquals("Long", TypeConverter.detectType(List.of(42L))); }
        @Test void doubleType() { assertEquals("Double", TypeConverter.detectType(List.of(3.14))); }
        @Test void floatType()  { assertEquals("Float", TypeConverter.detectType(List.of(3.14f))); }
        @Test void booleanType(){ assertEquals("Boolean", TypeConverter.detectType(List.of(true))); }

        @Test void bigDecimal() {
            assertEquals("java.math.BigDecimal", TypeConverter.detectType(List.of(new BigDecimal("1.23"))));
        }

        @Test void bigInteger() {
            assertEquals("java.math.BigInteger", TypeConverter.detectType(List.of(new BigInteger("999"))));
        }

        @Test void nullList()      { assertEquals("String", TypeConverter.detectType(null)); }
        @Test void emptyList()     { assertEquals("String", TypeConverter.detectType(Collections.emptyList())); }
        @Test void nullFirstValue(){ assertEquals("String", TypeConverter.detectType(Collections.singletonList(null))); }
    }

    @Nested
    class FormatLiteral {

        @Test void stringValue()  { assertEquals("\"hello\"", TypeConverter.formatLiteral("hello", "String")); }
        @Test void stringEscape() { assertEquals("\"say \\\"hi\\\"\"", TypeConverter.formatLiteral("say \"hi\"", "String")); }
        @Test void integerValue() { assertEquals("42", TypeConverter.formatLiteral(42, "Integer")); }
        @Test void longValue()    { assertEquals("100", TypeConverter.formatLiteral(100L, "Long")); }
        @Test void doubleValue()  { assertEquals("3.14", TypeConverter.formatLiteral(3.14, "Double")); }
        @Test void floatValue()   { assertEquals("2.5", TypeConverter.formatLiteral(2.5f, "Float")); }
        @Test void booleanValue() { assertEquals("true", TypeConverter.formatLiteral(true, "Boolean")); }
        @Test void nullValue()    { assertEquals("null", TypeConverter.formatLiteral(null, "String")); }

        @Test void bigDecimal() {
            assertEquals("new java.math.BigDecimal(\"1.23\")",
                TypeConverter.formatLiteral(new BigDecimal("1.23"), "java.math.BigDecimal"));
        }

        @Test void unknownType() {
            assertEquals("\"something\"", TypeConverter.formatLiteral("something", "UnknownType"));
        }
    }

    @Nested
    class Kotlinify {

        @Test void javaUtilList() { assertEquals("List<String>", TypeConverter.kotlinify("java.util.List<String>")); }
        @Test void javaUtilMap()  { assertEquals("Map<String, Any>", TypeConverter.kotlinify("java.util.Map<String, Any>")); }
        @Test void javaUtilSet()  { assertEquals("Set<Int>", TypeConverter.kotlinify("java.util.Set<Int>")); }
        @Test void javaLangObject(){ assertEquals("Any", TypeConverter.kotlinify("java.lang.Object")); }
        @Test void bareObject()   { assertEquals("Any", TypeConverter.kotlinify("Object")); }
        @Test void integer()      { assertEquals("Int", TypeConverter.kotlinify("Integer")); }
        @Test void voidType()     { assertEquals("Unit", TypeConverter.kotlinify("Void")); }
        @Test void nullInput()    { assertNull(TypeConverter.kotlinify(null)); }
        @Test void alreadyKotlin(){ assertEquals("String", TypeConverter.kotlinify("String")); }
    }

    @Nested
    class KotlinifyDefaultValue {

        @Test void arrayList()   { assertEquals("emptyList()", TypeConverter.kotlinifyDefaultValue("new ArrayList<>()")); }
        @Test void hashMap()     { assertEquals("emptyMap()", TypeConverter.kotlinifyDefaultValue("new HashMap<>()")); }
        @Test void hashSet()     { assertEquals("emptySet()", TypeConverter.kotlinifyDefaultValue("new HashSet<>()")); }
        @Test void mutableList() { assertEquals("mutableListOf<String>()", TypeConverter.kotlinifyDefaultValue("new ArrayList<String>()")); }
        @Test void nullInput()   { assertNull(TypeConverter.kotlinifyDefaultValue(null)); }
        @Test void noChange()    { assertEquals("emptyList()", TypeConverter.kotlinifyDefaultValue("emptyList()")); }
    }
}
