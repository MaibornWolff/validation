/*
 * The MIT License
 * Copyright © 2022 MaibornWolff GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.maibornwolff.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.maibornwolff.validation.Validation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ValidationTest {

    private static final String ERROR_MESSAGE = "Error message";
    private static final String EXCEPTION_MESSAGE = "Exception reason";

    @Test
    void should_return_ok_validation_without_errors() {
        final Validation validation = ok();

        assertThat(validation.hasError()).isFalse();
        assertThat(validation.getErrors()).isEmpty();
    }

    @Test
    void should_not_throw_ValidationException_if_ok() {
        final Validation validation = ok().canThrow(ERROR_MESSAGE);

        assertThat(validation.hasError()).isFalse();
        assertThat(validation.getErrors()).isEmpty();
    }

    @Test
    void should_return_error_validation_with_message() {
        final Validation validation = error(ERROR_MESSAGE);

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors()).hasSize(1).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void should_throw_a_ValidationException_when_error() {
        assertThatThrownBy(() -> error(ERROR_MESSAGE).canThrow(EXCEPTION_MESSAGE))
                .hasMessage(EXCEPTION_MESSAGE)
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void should_add_sub_validations() {
        final Validation validation = of(ok(), error(ERROR_MESSAGE));

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors()).hasSize(1).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void toString_should_generate_readable_output() {
        assertThat(Validation.error("A error message").toString()).isEqualTo("Validation errors:\n" +
                "A error message");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyCollections")
    void should_validate_if_a_Collection_is_not_empty(Collection col) {
        final Validation validation = Validation.validateNotEmpty(col, ERROR_MESSAGE);

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors()).hasSize(1).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void should_validate_if_a_Collection_and_call_the_callable_with_each_sub_element() {
        Function fun = Mockito.mock(Function.class);
        Mockito.when(fun.apply(any())).thenReturn(Validation.ok());

        Collection<String> col = Set.of("value 1");
        final Validation validation = Validation.validateNotEmpty(col, fun, ERROR_MESSAGE);

        assertThat(validation.hasError()).isFalse();
        Mockito.verify(fun).apply(eq("value 1"));
    }

    @Test
    void should_create_validation_error_if_sub_element_is_not_valid() {
        Function fun = Mockito.mock(Function.class);
        Mockito.when(fun.apply(any())).thenReturn(Validation.error("not ok"));

        Collection<String> col = Set.of("value 1");
        final Validation validation = Validation.validateNotEmpty(col, fun, ERROR_MESSAGE);

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors()).hasSize(1).containsExactly("not ok");
    }

    @Test
    void validateNotNullAndMatches_should_validate_regex_without_errors() {
        final Validation validation = validateNotNullAndMatches("W123", "W\\d{3}", "name");

        assertThat(validation.hasError()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"W 12", "A054", "ABCD", "054", "W0123", "B054"})
    void validateNotNullAndMatches_should_validate_regex_with_errors(String wrongValue) {
        final Validation validation = validateNotNullAndMatches(wrongValue, "W\\d{3}", "name");

        assertThat(validation.hasError()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"W123"})
    void validateEmptyOrMatches_should_validate_regex_without_errors(String correctValue) {
        final Validation validation = Validation.validateEmptyOrMatches(correctValue, "W\\d{3}", "name");

        assertThat(validation.hasError()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"LL", "RL"})
    void validateEmptyOrMatches_steering_rule(String correctValue) {
        final Validation validation = Validation.validateEmptyOrMatches(correctValue, "LL|RL", "name");

        assertThat(validation.hasError()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"W 12", "A054", "ABCD", "054", "W0123", "B054"})
    void validateEmptyOrMatches_should_validate_regex_with_errors(String wrongValue) {
        final Validation validation = Validation.validateEmptyOrMatches(wrongValue, "W\\d{3}", "name");

        assertThat(validation.hasError()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"W 12", "A054", "ABCD", "054", "W0123", "B054"})
    void validateEmptyOrMatches_should_validate_optionals_of_string_with_errors(String wrongValue) {
        final Validation validation = Validation.validateEmptyOrMatches(Optional.of(wrongValue), "W\\d{3}", "name");

        assertThat(validation.hasError()).isTrue();
    }

    @Test
    void validateNotNull_should_validate_not_null() {
        final Validation validation = Validation.validateNotNull(null,"name");

        assertThat(validation.hasError()).isTrue();
    }

    @Test
    void validateNotNull_should_validate_not_null_and_call_the_callable_with_the_element() {
        Function fun = Mockito.mock(Function.class);
        Mockito.when(fun.apply(any())).thenReturn(Validation.ok());

        final Validation validation = Validation.validateNotNull("A Value", fun,"name");

        assertThat(validation.hasError()).isFalse();
    }

    @Test
    void validateNotEmpty_should_validate_not_empty_Strings() {
        final Validation validation = validateNotEmpty("", ERROR_MESSAGE);

        assertThat(validation.hasError()).isTrue();
    }

    @Test
    void validateIsPresent_is_null_safe() {
        final Validation validation = validateIsPresent(null, "value");

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors().get(0)).isEqualTo("value should not be null");
    }

    @Test
    void validateIsPresent_should_create_validation_error_if_value_is_not_present() {
        final Validation validation = validateIsPresent(Optional.empty(), "value");

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors().get(0)).isEqualTo("value should be present");
    }

    @Test
    void validateIsPresent_should_have_no_error_if_Optional_is_present() {
        final Validation validation = validateIsPresent(Optional.of("value"), "value");

        assertThat(validation.hasError()).isFalse();
    }

    @Test
    void validateIsPresent_should_call_the_callable_with_the_element() {
        Function fun = Mockito.mock(Function.class);
        Mockito.when(fun.apply(any())).thenReturn(Validation.ok());

        final Validation validation = validateIsPresent(Optional.of("value"), fun, "value");

        assertThat(validation.hasError()).isFalse();
        Mockito.verify(fun).apply(eq("value"));
    }

    @Test
    void validateIsPresent_should_create_validation_error_if_sub_element_is_not_valid() {
        Function fun = Mockito.mock(Function.class);
        Mockito.when(fun.apply(any())).thenReturn(Validation.error("not ok"));

        final Validation validation = validateIsPresent(Optional.of("value"), fun, "value");

        assertThat(validation.hasError()).isTrue();
        assertThat(validation.getErrors()).hasSize(1).containsExactly("not ok");
    }

    public static Stream<Collection<?>> emptyCollections() {
        return Stream.of(
                List.of(),
                Set.of()
        );
    }
}