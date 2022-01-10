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

    public static <T> Validation validateNotNull(T checkee, String name) {
        return Objects.isNull(checkee) ?
                error(name + " should not be null") :
                ok();
    }

    public static <T> Validation validateNotNull(T checkee, Function<T, Validation> callable, String message) {
        return Objects.isNull(checkee) ?
                error(message) :
                Validation.of(callable.apply(checkee));
    }

    public static <T> Validation validateNotEmpty(String checkee, String message) {
        return Objects.isNull(checkee) || checkee.isEmpty() ?
                error(message) :
                ok();
    }

    public static <T> Validation validateNotEmpty(Collection<T> checkee, String message) {
        return Objects.isNull(checkee) || checkee.isEmpty() ?
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

    public static Validation validateNotNullOrEmpty(String checkee, String name) {
        return checkee == null || checkee.isEmpty() ?
                error(name + " should have a value") :
                ok();
    }

    public static Validation validateNotNullAndMatches(String checkee, String regex, String name) {
        return checkee == null || !matches(regex, checkee) ?
                error(name + " should match " + regex) :
                ok();
    }

    public static Validation validateEmptyOrMatches(String checkee, String regex, String name) {
        return checkee == null || checkee.isEmpty() || matches(regex, checkee) ?
                ok() :
                error(name + " should match " + regex);
    }

    public static Validation validateEmptyOrMatches(Optional<String> checkee, String regex, String name) {
        return checkee.isEmpty() || matches(regex, checkee.get()) ?
                ok() :
                error(name + " should match " + regex);
    }

    public static <T> Validation validateIsPresent(Optional<T> checkee, String name) {
        return validateNotNull(checkee, opt -> opt.isPresent() ?
                ok() :
                error(name + " should be present"), name + " should not be null");
    }

    public static <T> Validation validateIsPresent(Optional<T> checkee, Function<T, Validation> callable, String name) {
        return checkee.map(t -> Validation.of(callable.apply(t)))
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
