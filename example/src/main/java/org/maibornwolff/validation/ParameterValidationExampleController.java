package org.maibornwolff.validation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.maibornwolff.validation.Validation.*;

public class ParameterValidationExampleController {

    Logger log = Logger.getLogger("Data controller");

    /**
     * Validate input parameters and throw ValidationException if one parameter is not valid.
     */
    public Collection<String> getSomeData(String param1, Integer param2, Optional<String> param3, Collection<String> paramCollection) {
        of(
                validateNotNullOrEmpty(param1, "param1 should be set"),
                validateNotNull(param2, "param2 should not be null"),
                validateIsPresent(param3, p -> validateEmptyOrMatches(p, "\\d{2}", "param2"), "param2"),
                validateNotEmpty(paramCollection, e -> validateNotEmpty(e, "colection element"), "collection should not be empty")
        ).canThrow("Error getting some data");


        return List.of("FOOs");
    }

    /**
     * Validate input parameters and throw ValidationException if one parameter is not valid.
     */
    public Collection<String> getSomeOtherData(String param1, Integer param2) {
        final Validation validation = of(
                validateNotNullOrEmpty(param1, "param1 should be set"),
                validateNotNull(param2, "param2 should not be null")
                );

        if(validation.hasError()) {
            log.info("Some parameters are not ok: " + validation);
        }

        return List.of("FOOs");
    }

}
