package com.ostoliarov.webproject.controller

/**
  * Created by Oleg Stoliarov on 10/13/18.
  */
object ApplicationResources {
	val CHARACTER_ENCODING = "UTF-8"

	val MESSAGES_ATTR_NAME = "messages"
	val GENERAL_MESSAGES_FRONT_BLOCK_NAME = "topMessages"
	val CURRENT_USER_ATTR_NAME = "currentUser"
	val PERIODICAL_ATTR_NAME = "periodical"
	val PERIODICAL_STATUSES_ATTR_NAME = "periodicalStatuses"
	val PERIODICAL_CATEGORIES_ATTR_NAME = "periodicalCategories"
	val ORIGINAL_URI_ATTR_NAME = "originalUri"
	val USERNAME_ATTR_NAME = "username"
	val USER_ROLE_ATTR_NAME = "userRole"
	val USER_EMAIL_ATTR_NAME = "userEmail"
	val ALL_PERIODICALS_ATTR_NAME = "allPeriodicals"
	val ALL_USERS_ATTR_NAME = "allUsers"
	val LANGUAGE_ATTR_NAME = "language"
	val PERIODICAL_STATISTICS_ATTR_NAME = "periodicalStatistics"
	val FINANCIAL_STATISTICS_ATTR_NAME = "financialStatistics"
	val ERROR_MESSAGE_ATTR_NAME = "errorMessage"

	val PARAM_NAME = "paramName"
	val PARAM_VALUE = "paramValue"
	val SIGN_IN_USERNAME_PARAM_NAME = "signInUsername"
	val SIGN_UP_USERNAME_PARAM_NAME = "signUpUsername"
	val USER_PASSWORD_PARAM_NAME = "password"
	val USER_REPEAT_PASSWORD_PARAM_NAME = "repeatPassword"
	val PERIODICAL_ID_PARAM_NAME = "periodicalId"
	val PERIODICAL_NAME_PARAM_NAME = "periodicalName"
	val PERIODICAL_CATEGORY_PARAM_NAME = "periodicalCategory"
	val PERIODICAL_PUBLISHER_PARAM_NAME = "periodicalPublisher"
	val PERIODICAL_COST_PARAM_NAME = "periodicalCost"
	val PERIODICAL_OPERATION_TYPE_PARAM_ATTR_NAME = "periodicalOperationType"
	val SUBSCRIPTION_PERIOD_PARAM_NAME = "subscriptionPeriod"
	val ENTITY_ID_PARAM_NAME = "entityId"
	val USER_INVOICES_PARAM_NAME = "userInvoices"
	val USER_SUBSCRIPTIONS_PARAM_NAME = "userSubscriptions"
	val PERIODICAL_DESCRIPTION_PARAM_NAME = "periodicalDescription"
	val PERIODICAL_STATUS_PARAM_NAME = "periodicalStatus"
	val USER_ROLE_PARAM_NAME = "userRole"
	val USER_EMAIL_PARAM_NAME = "userEmail"

	val VALIDATION_BUNDLE_PATH = "webproject/i18n/validation"

	val ADMIN_PANEL_VIEW_NAME = "adminPanel"
	val PERIODICAL_LIST_VIEW_NAME = "periodicals/periodicalList"
	val ONE_PERIODICAL_VIEW_NAME = "periodicals/onePeriodical"
	val CREATE_EDIT_PERIODICAL_VIEW_NAME = "periodicals/createAndEdit"
	val USER_LIST_VIEW_NAME = "users/userList"
	val ONE_USER_INFO_VIEW_NAME = "users/userAccount"
	val BACKEND_MAIN_PAGE_VIEW_NAME = "home"
	val SIGN_UP_PAGE_VIEW_NAME = "createNewUser"
	val PAGE_404_VIEW_NAME = "errors/page-404"
	val STORAGE_EXCEPTION_PAGE_VIEW_NAME = "errors/storage-error-page"
	val GENERAL_ERROR_PAGE_VIEW_NAME = "errors/error-page"
	val ACCESS_DENIED_PAGE_VIEW_NAME = "errors/accessDenied"

	val ADMIN_PANEL_URI = "/backend/adminPanel"
	val PERIODICAL_LIST_URI = "/backend/periodicals"
	val LOGIN_PAGE = "/login.jsp"
	val SIGN_IN_URI = "/backend/signIn"
	val SIGN_OUT_URI = "/backend/signOut"
	val SIGN_UP_URI = "/backend/createNewUser"
	val USERS_LIST_URI = "/backend/users"
	val CURRENT_USER_ACCOUNT_URI = "/backend/users/currentUser"
	val PERIODICAL_CREATE_NEW_URI = "/backend/periodicals/createNew"
	val PERIODICAL_DELETE_DISCARDED_URI = "/backend/periodicals/discarded"

	val STATUS_CODE_SUCCESS = 200
	val STATUS_CODE_VALIDATION_FAILED = 412

	val PERIODICAL_NAME_PATTERN_REGEX = "[а-яА-ЯіІїЇєЄёЁ\\w\\s!&?$#@'\"-]{2,45}"
	val PERIODICAL_PUBLISHER_PATTERN_REGEX = "[а-яА-ЯіІїЇєЄёЁ\\w\\s-]{2,45}"
	val PERIODICAL_COST_PATTERN_REGEX = "0|[1-9]{1}\\d{0,4}"
	val USER_EMAIL_PATTERN_REGEX = "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$"
	val USER_PASSWORD_PATTERN_REGEX = "[\\w!@#$%^&*/=(){}<>_]{6,12}"

	val MSG_KEY_CATEGORY_NEWS = "category.news"
	val MSG_KEY_CATEGORY_NATURE = "category.nature"
	val MSG_KEY_CATEGORY_FITNESS = "category.fitness"
	val MSG_KEY_CATEGORY_BUSINESS = "category.business"
	val MSG_KEY_CATEGORY_SPORTS = "category.sports"
	val MSG_KEY_CATEGORY_SCIENCE_AND_ENGINEERING = "category.scienceAndEngineering"
	val MSG_KEY_CATEGORY_TRAVELLING = "category.travelling"
	val MSG_KEY_CATEGORY_UNKNOWN = "category.unknown"

	val MSG_SUCCESS = "validation.ok"
	val MSG_CREDENTIALS_ARE_NOT_CORRECT = "validation.credentialsAreNotCorrect"
	val USERNAME_IS_NOT_UNIQUE_TRY_ANOTHER_ONE = "validation.usernameIsNotUnique"
	val USER_EMAIL_IS_NOT_UNIQUE_TRY_ANOTHER_ONE = "validation.userEmailIsNotUnique"
	val MSG_PERIODICAL_NAME_INCORRECT = "periodicalName.validationError"
	val MSG_PERIODICAL_NAME_DUPLICATION = "periodicalName.duplicationError"
	val MSG_PERIODICAL_PUBLISHER_ERROR = "periodicalPublisher.validationError"
	val MSG_PERIODICAL_CATEGORY_ERROR = "periodicalCategory.validationError"
	val MSG_PERIODICAL_COST_ERROR = "periodicalCost.validationError"
	val MSG_INCORRECT_USER_ID = "validation.invoiceOperation.incorrectUserId"
	val MSG_VALIDATION_PASSED_SUCCESS = "validation.passedSuccessfully.success"
	val MSG_VALIDATION_PERIODICAL_IS_NOT_VISIBLE = "validation.periodicalIsNotVisible"
	val MSG_VALIDATION_NO_SUCH_INVOICE = "validation.invoice.noSuchInvoice"
	val MSG_VALIDATION_INVOICE_IS_NOT_NEW = "validation.invoice.invoiceIsNotNew"
	val MSG_INVOICE_PAYMENT_SUCCESS = "validation.invoiceWasPaid.success"
	val MSG_INVOICE_PAYMENT_ERROR = "validation.invoice.payInvoiceError"
	val MSG_VALIDATION_PERIODICAL_IS_NULL = "validation.periodicalIsNull"
	val MSG_VALIDATION_SUBSCRIPTION_PERIOD_IS_NOT_VALID = "validation.subscriptionPeriodIsNotValid"
	val MSG_INVOICE_CREATION_SUCCESS = "validation.invoiceCreated.success"
	val MSG_INVOICE_PERSISTING_FAILED = "validation.invoicePersistingFailed"
	val MSG_PERIODICALS_DELETED_SUCCESS = "validation.discardedPeriodicalsDeleted.success"
	val MSG_NO_PERIODICALS_TO_DELETE = "validation.thereIsNoPeriodicalsToDelete.warning"
	val INCORRECT_OPERATION_DURING_PERSISTING_A_PERIODICAL = "Incorrect periodicalOperationType during persisting a periodical."
	val MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_WARNING = "validation.periodicalHasActiveSubscriptions.warning"
	val MSG_PERIODICAL_HAS_ACTIVE_SUBSCRIPTIONS_ERROR = "validation.periodicalHasActiveSubscriptions.error"
	val MSG_PERIODICAL_CREATED_SUCCESS = "periodicalCreatedNew.success"
	val MSG_PERIODICAL_UPDATED_SUCCESS = "periodicalUpdated.success"
	val MSG_PERIODICAL_PERSISTING_ERROR = "periodicalPersisting.error"
	val MSG_ERROR_USER_IS_BLOCKED = "error.userIsBlocked"
	val MSG_USER_EMAIL_REGEX_ERROR = "validation.userEmailIsIncorrect"
	val MSG_USER_EMAIL_DUPLICATION_ERROR = "validation.userEmailIsNotUnique"
	val MSG_USER_PASSWORD_ERROR = "validation.userPasswordIsIncorrect"
	val MSG_NEW_USER_WAS_NOT_CREATED_ERROR = "userWasNotCreated.error"
	val MSG_VALIDATION_PASSWORDS_ARE_NOT_EQUAL = "validation.passwordsAreNotEqual"

	val METHODS_URI_SEPARATOR = ":"
	val METHOD_METHOD_SEPARATOR = "\\|"
}
