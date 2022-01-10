package org.maibornwolff.validation;

public class ValidationException extends RuntimeException {
  private final Validation validation;

  public ValidationException(Validation validation) {
    super();
    this.validation = validation;
  }

  public ValidationException(String message, Validation validation) {
    super(message);
    this.validation = validation;
  }

  public Validation getValidation() {
    return validation;
  }
}
