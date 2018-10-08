package com.stolser.javatraining.webproject.controller.form.validator;

import com.stolser.javatraining.webproject.controller.form.validator.periodical.*;
import com.stolser.javatraining.webproject.controller.form.validator.user.UserEmailValidator_;
import com.stolser.javatraining.webproject.controller.form.validator.user.UserPasswordValidator_;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.*;

/**
 * Produces validators for different parameter names.
 */
public class ValidatorFactory_ {
    private static final String THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM =
            "There is no validator for such a parameter!";

    public static Validator getPeriodicalNameValidator() {
        return PeriodicalNameValidator_.getInstance();
    }

    public static Validator getPeriodicalCategoryValidator() {
        return PeriodicalCategoryValidator_.getInstance();
    }

    public static Validator getPeriodicalPublisherValidator() {
        return PeriodicalPublisherValidator_.getInstance();
    }

    public static Validator getPeriodicalCostValidator() {
        return PeriodicalCostValidator_.getInstance();
    }

    public static Validator getUserPasswordValidator() {
        return UserPasswordValidator_.getInstance();
    }

    /**
     * Returns a concrete validator for this specific parameter.
     *
     * @param paramName a http parameter name that need to be validated
     */
    public static Validator newValidator(String paramName) {
        switch (paramName) {
            case PERIODICAL_NAME_PARAM_NAME:
                return PeriodicalNameValidator_.getInstance();

            case PERIODICAL_CATEGORY_PARAM_NAME:
                return PeriodicalCategoryValidator_.getInstance();

            case PERIODICAL_PUBLISHER_PARAM_NAME:
                return PeriodicalPublisherValidator_.getInstance();

            case PERIODICAL_COST_PARAM_NAME:
                return PeriodicalCostValidator_.getInstance();

            case USER_EMAIL_PARAM_NAME:
                return UserEmailValidator_.getInstance();

            case USER_PASSWORD_PARAM_NAME:
                return UserPasswordValidator_.getInstance();

            default:
                throw new ValidationProcessorException_(THERE_IS_NO_VALIDATOR_FOR_SUCH_PARAM);
        }
    }
}
