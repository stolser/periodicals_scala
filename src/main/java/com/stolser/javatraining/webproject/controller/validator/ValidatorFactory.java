package com.stolser.javatraining.webproject.controller.validator;

import com.stolser.javatraining.webproject.controller.validator.periodical.PeriodicalCategoryValidator;
import com.stolser.javatraining.webproject.controller.validator.periodical.PeriodicalCostValidator;
import com.stolser.javatraining.webproject.controller.validator.periodical.PeriodicalNameValidator;
import com.stolser.javatraining.webproject.controller.validator.periodical.PeriodicalPublisherValidator;
import com.stolser.javatraining.webproject.controller.validator.user.SignInUsernameValidator;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.*;

/**
 * Produces validators for different parameter names.
 */
public class ValidatorFactory {
    private static final String THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM =
            "There is no validator for such a parameter!";

    private static class InstanceHolder {
        private static final ValidatorFactory INSTANCE = new ValidatorFactory();
    }

    public static ValidatorFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @param paramName a http parameter name that need to be validated
     * @return a concrete validator for this specific parameter
     */
    public Validator newValidator(String paramName) {
        switch (paramName) {
            case SIGN_IN_USERNAME_PARAM_NAME:
                return new SignInUsernameValidator();

            case PERIODICAL_NAME_PARAM_NAME:
                return new PeriodicalNameValidator();

            case PERIODICAL_CATEGORY_PARAM_NAME:
                return new PeriodicalCategoryValidator();

            case PERIODICAL_PUBLISHER_PARAM_NAME:
                return new PeriodicalPublisherValidator();

            case PERIODICAL_COST_PARAM_NAME:
                return new PeriodicalCostValidator();

            default:
                throw new IllegalArgumentException(THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM);
        }
    }
}
