package com.stolser.javatraining.webproject.controller.request.processor.periodical;

import com.stolser.javatraining.webproject.controller.form.validator.ValidationResult;
import com.stolser.javatraining.webproject.controller.form.validator.ValidatorFactory;
import com.stolser.javatraining.webproject.controller.form.validator.front.message.FrontMessageFactory;
import com.stolser.javatraining.webproject.controller.form.validator.front.message.FrontendMessage;
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor;
import com.stolser.javatraining.webproject.controller.utils.HttpUtils;
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical;
import com.stolser.javatraining.webproject.service.PeriodicalService;
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.*;
import static com.stolser.javatraining.webproject.model.entity.periodical.Periodical.Status.*;

/**
 * Processes a POST request to persist one periodical. It handles both {@code create} and
 * {@code update} operations by analysing {@code periodicalOperationType} request parameter.
 */
public class PersistOnePeriodical implements RequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistOnePeriodical.class);
    private static final String EXCEPTION_DURING_PERSISTING_PERIODICAL_WITH_ID =
            "Exception during persisting a periodical with id = {}.";
    private PeriodicalService periodicalService = PeriodicalServiceImpl.getInstance();
    private FrontMessageFactory messageFactory = FrontMessageFactory.getInstance();

    private PersistOnePeriodical() {
    }

    private static class InstanceHolder {
        private static final PersistOnePeriodical INSTANCE = new PersistOnePeriodical();
    }

    public static PersistOnePeriodical getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) {
        List<FrontendMessage> generalMessages = new ArrayList<>();
        Periodical periodicalToSave;
        try {
            periodicalToSave = HttpUtils.getPeriodicalFromRequest(request);
        } catch (RuntimeException e) {
            processExceptionAndRedirect(request, response, e);
            return null;
        }

        String redirectUri = getRedirectUriByOperationType(request, periodicalToSave);
        request.getSession().setAttribute(PERIODICAL_ATTR_NAME, periodicalToSave);

        if (isPeriodicalToSaveValid(periodicalToSave, request)) {
            generalMessages.add(messageFactory.getInfo(MSG_VALIDATION_PASSED_SUCCESS));
        } else {
            HttpUtils.sendRedirect(request, response, redirectUri);
            return null;
        }

        Periodical periodicalInDb = periodicalService.findOneById(periodicalToSave.getId());
        Periodical.Status oldStatus = (periodicalInDb != null) ? periodicalInDb.getStatus() : null;
        Periodical.Status newStatus = periodicalToSave.getStatus();

        checkPeriodicalForActiveSubscriptions(generalMessages, periodicalToSave, oldStatus, newStatus);

        try {
            if (isStatusChangedFromActiveOrInactiveToDiscarded(oldStatus, newStatus)) {
                int discardedPeriodicalsNumber = periodicalService.updateAndSetDiscarded(periodicalToSave);

                if (discardedPeriodicalsNumber == 0) {
                    addErrorMessageAndSendRedirect(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_ERROR, generalMessages,
                            request, response, redirectUri);
                    return null;
                }
            } else {
                periodicalService.save(periodicalToSave);
            }

            addGeneralMessagesToSession(request, generalMessages);
            return DisplayAllPeriodicals.getInstance().process(request, response);

        } catch (RuntimeException e) {
            addErrorMessageAndSendRedirect(MSG_PERIODICAL_PERSISTING_ERROR, generalMessages,
                    request, response, redirectUri);
            return null;
        }
    }

    private void addErrorMessageAndSendRedirect(String message, List<FrontendMessage> generalMessages,
                                                HttpServletRequest request, HttpServletResponse response,
                                                String redirectUri) {
        generalMessages.add(messageFactory.getError(message));
        HttpUtils.addGeneralMessagesToSession(request, generalMessages);
        HttpUtils.sendRedirect(request, response, redirectUri);
    }

    private boolean periodicalToSaveHasActiveSubscriptions(Periodical periodicalToSave) {
        return periodicalService.hasActiveSubscriptions(periodicalToSave.getId());
    }

    private Periodical.OperationType getOperationTypeFromRequest(HttpServletRequest request) {
        return Periodical.OperationType.valueOf(request
                .getParameter(PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME).toUpperCase());
    }

    private void checkPeriodicalForActiveSubscriptions(List<FrontendMessage> generalMessages,
                                                       Periodical periodicalToSave,
                                                       Periodical.Status oldStatus,
                                                       Periodical.Status newStatus) {
        if (isStatusChangedFromActiveToInactive(oldStatus, newStatus)
                && periodicalToSaveHasActiveSubscriptions(periodicalToSave)) {
            generalMessages.add(messageFactory.getWarning(MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_WARNING));
        }
    }

    private void processExceptionAndRedirect(HttpServletRequest request, HttpServletResponse response,
                                             Exception e) {
        List<FrontendMessage> generalMessages = new ArrayList<>();

        LOGGER.error(EXCEPTION_DURING_PERSISTING_PERIODICAL_WITH_ID,
                request.getParameter(ENTITY_ID_PARAM_NAME), e);

        generalMessages.add(messageFactory.getError(MSG_PERIODICAL_PERSISTING_ERROR));
        HttpUtils.addGeneralMessagesToSession(request, generalMessages);
        HttpUtils.sendRedirect(request, response, PERIODICAL_LIST_URI);
    }

    private void addGeneralMessagesToSession(HttpServletRequest request, List<FrontendMessage> generalMessages) {

        switch (getOperationTypeFromRequest(request)) {
            case CREATE:
                generalMessages.add(messageFactory.getSuccess(MSG_PERIODICAL_CREATED_SUCCESS));
                break;
            case UPDATE:
                generalMessages.add(messageFactory.getSuccess(MSG_PERIODICAL_UPDATED_SUCCESS));
                break;
            default:
                throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL);
        }

        HttpUtils.addGeneralMessagesToSession(request, generalMessages);
    }

    private boolean isStatusChangedFromActiveOrInactiveToDiscarded(Periodical.Status oldStatus,
                                                                   Periodical.Status newStatus) {
        return (ACTIVE.equals(oldStatus) || INACTIVE.equals(oldStatus))
                && DISCARDED.equals(newStatus);
    }

    private boolean isStatusChangedFromActiveToInactive(Periodical.Status oldStatus, Periodical.Status newStatus) {
        return ACTIVE.equals(oldStatus) && INACTIVE.equals(newStatus);
    }

    private String getRedirectUriByOperationType(HttpServletRequest request, Periodical periodicalToSave) {
        String redirectUri;
        switch (getOperationTypeFromRequest(request)) {
            case CREATE:
                redirectUri = PERIODICAL_CREATE_NEW_URI;
                break;
            case UPDATE:
                redirectUri = PERIODICAL_LIST_URI + "/" + periodicalToSave.getId() + "/update";
                break;
            default:
                throw new IllegalArgumentException(INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL);
        }

        return redirectUri;
    }

    private boolean isPeriodicalToSaveValid(Periodical periodicalToSave, HttpServletRequest request) {
        Map<String, FrontendMessage> messages = new HashMap<>();

        validateName(periodicalToSave, request, messages);
        validateCategory(periodicalToSave, request, messages);
        validatePublisher(periodicalToSave, request, messages);
        validateCost(periodicalToSave, request, messages);

        int messagesSize = messages.size();
        if (messagesSize > 0) {
            request.getSession().setAttribute(MESSAGES_ATTR_NAME, messages);
        }

        return messagesSize == 0;
    }

    private void validateName(Periodical periodicalToSave, HttpServletRequest request,
                              Map<String, FrontendMessage> messages) {
        ValidationResult result = ValidatorFactory.getPeriodicalNameValidator()
                .validate(periodicalToSave.getName(), request);

        if (result.getStatusCode() != STATUS_CODE_SUCCESS) {
            messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.getError(result.getMessageKey()));
        }
    }

    private void validateCategory(Periodical periodicalToSave, HttpServletRequest request,
                                  Map<String, FrontendMessage> messages) {
        ValidationResult result = ValidatorFactory.getPeriodicalCategoryValidator()
                .validate(periodicalToSave.getCategory().toString(), request);

        if (result.getStatusCode() != STATUS_CODE_SUCCESS) {
            messages.put(PERIODICAL_NAME_PARAM_NAME, messageFactory.getError(result.getMessageKey()));
        }
    }

    private void validatePublisher(Periodical periodicalToSave, HttpServletRequest request,
                                   Map<String, FrontendMessage> messages) {
        ValidationResult result = ValidatorFactory.getPeriodicalPublisherValidator()
                .validate(periodicalToSave.getPublisher(), request);

        if (result.getStatusCode() != STATUS_CODE_SUCCESS) {
            messages.put(PERIODICAL_PUBLISHER_PARAM_NAME, messageFactory.getError(result.getMessageKey()));
        }
    }

    private void validateCost(Periodical periodicalToSave, HttpServletRequest request,
                              Map<String, FrontendMessage> messages) {
        ValidationResult result = ValidatorFactory.getPeriodicalCostValidator()
                .validate(String.valueOf(periodicalToSave.getOneMonthCost()), request);

        if (result.getStatusCode() != STATUS_CODE_SUCCESS) {
            messages.put(PERIODICAL_COST_PARAM_NAME, messageFactory.getError(result.getMessageKey()));
        }
    }

}
