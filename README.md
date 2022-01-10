# Validation

------------

An easy-to-use validation framework for validating input parameters of REST endpoints or business objects.

## Version 1.0

------------

* Validation of Strings and Collections for null, empty values
* Support regex matching support for strings
* Summary of several validations
* Support of throwing a ValidationException

## How to use ist?

------------

Create some methods and structure the code like the business object you want to validate:
```java
    public Validation validate(SourceObject source) {
        return Validation.of(
                validateNotNullOrEmpty(source.name, "name"),
                validateNonEmptyCollection(source.subCollection, this::validateSubCollection, "This input has no assigned sub collections"));
    }

    private Validation validateSubCollection(SubElement sub) {
        return Validation.of(
                validateNotNullAndMatches(sub.company, "W\\d{3}", "Company descriptor"),
                validateNotNullOrEmpty(sub.userId, "UserId"),
                validateNotNullOrEmpty(sub.department, "Department"));
    }
```

At the end take the Validation and check it for errors:

```java
    if (validation.hasError()) {
      throw new ValidationException("Impermissible content relevant errors", validation);
    }
```

The ValidationException contains the result of all validations and at the Controller Exception Handler 


