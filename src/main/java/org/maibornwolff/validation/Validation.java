package org.maibornwolff.validation;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.matches;
import static java.util.stream.Collectors.toList;

public class Validation {

    private final List<String> errors = new ArrayList<>();

    public static Validation ok() {
        return new Validation();
    }

    public static Validation error(String error) {
        final Validation validation = new Validation();
        validation.errors.add(error);
        return validation;
    }

    public static Validation of(Validation... subs) {
        return of(List.of(subs));
    }

    public static Validation of(List<Validation> validations) {
        final Validation validation = new Validation();
        validation.errors.addAll(validations.stream()
                .filter(Validation::hasError)
                .map(Validation::getErrors)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
        return validation;
    }

    public static <T> Validation validateNotNull(T specimen, String name) {
        return Objects.isNull(specimen) ?
                error(name + " should not be null") :
                ok();
    }

    public static <T> Validation validateNotNull(T specimen, Function<T, Validation> callable, String message) {
        return Objects.isNull(specimen) ?
                error(message) :
                Validation.of(callable.apply(specimen));
    }

    public static <T> Validation validateNotEmpty(String specimen, String message) {
        return Objects.isNull(specimen) || specimen.isEmpty() ?
                error(message) :
                ok();
    }

    public static <T> Validation validateNotEmpty(Collection<T> specimen, String message) {
        return Objects.isNull(specimen) || specimen.isEmpty() ?
                error(message) :
                ok();
    }

    public static <T> Validation validateNotEmpty(Collection<T> collection, Function<T, Validation> callable, String message) {
        return Objects.isNull(collection) || collection.isEmpty() ?
                error(message) :
                Validation.of(collection.stream().map(callable).collect(toList()));
    }

    public static <T> Validation validateNotNull(Collection<T> collection, Function<T, Validation> callable, String message) {
        return Objects.isNull(collection) ?
                error(message) :
                Validation.of(collection.stream().map(callable).collect(toList()));
    }

    public static Validation validateNotNullOrEmpty(String specimen, String name) {
        return specimen == null || specimen.isEmpty() ?
                error(name + " should have a value") :
                ok();
    }

    public static Validation validateNotNullAndMatches(String specimen, String regex, String name) {
        return specimen == null || !matches(regex, specimen) ?
                error(name + " should match " + regex) :
                ok();
    }

    public static Validation validateEmptyOrMatches(String specimen, String regex, String name) {
        return specimen == null || specimen.isEmpty() || matches(regex, specimen) ?
                ok() :
                error(name + " should match " + regex);
    }

    public static Validation validateEmptyOrMatches(Optional<String> specimen, String regex, String name) {
        return specimen.isEmpty() || matches(regex, specimen.get()) ?
                ok() :
                error(name + " should match " + regex);
    }

    public static <T> Validation validateIsPresent(Optional<T> specimen, String name) {
        return validateNotNull(specimen, opt -> opt.isPresent() ?
                ok() :
                error(name + " should be present"), name + " should not be null");
    }

    public static <T> Validation validateIsPresent(Optional<T> specimen, Function<T, Validation> callable, String name) {
        return specimen.map(t -> Validation.of(callable.apply(t)))
                .orElseGet(() -> error(name + " should be present"));
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public Validation canThrow(String message) {
        if (hasError()) {
            throw new ValidationException(message, this);
        }
        return this;
    }

    @Override
    public String toString() {
        return this.hasError() ? "Validation errors:\n" + String.join("\n", errors) : "No errors";

    }
}
