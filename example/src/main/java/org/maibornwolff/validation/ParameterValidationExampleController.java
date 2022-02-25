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
     * Validate input parameters and log the validation result
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
