package com.stolser.javatraining.webproject.controller.form.validator.periodical;

import com.stolser.javatraining.webproject.controller.form.validator.AbstractValidator;
import com.stolser.javatraining.webproject.controller.form.validator.ValidationResult;
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalCategory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.MSG_PERIODICAL_CATEGORY_ERROR;
import static com.stolser.javatraining.webproject.controller.ApplicationResources.STATUS_CODE_VALIDATION_FAILED;

/**
 * Checks whether a periodical category name passed in the request exists.
 */
public class PeriodicalCategoryValidator_ extends AbstractValidator {
    private static ValidationResult failedResult =
            new ValidationResult(STATUS_CODE_VALIDATION_FAILED, MSG_PERIODICAL_CATEGORY_ERROR);

    private PeriodicalCategoryValidator_() {}

    private static class InstanceHolder {
        private static final PeriodicalCategoryValidator_ INSTANCE = new PeriodicalCategoryValidator_();
    }

    public static PeriodicalCategoryValidator_ getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public Optional<ValidationResult> checkParameter(String category, HttpServletRequest request) {
        if (isCategoryNameCorrect(category)) {
            return Optional.empty();
        }

        return Optional.of(failedResult);
    }

    private boolean isCategoryNameCorrect(String category) {
        return Arrays.stream(PeriodicalCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList())
                .contains(category);
    }
}
