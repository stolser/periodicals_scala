package com.stolser.javatraining.block02.morelessgame.model;

import com.google.common.collect.Range;
import com.stolser.javatraining.block02.morelessgame.controller.InputReader;
import com.stolser.javatraining.block02.morelessgame.view.ViewGenerator;
import com.stolser.javatraining.block02.morelessgame.view.ViewPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The class abstracting a game process.
 */
public class Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);
    /**
     * The minimum distance between the lower and upper bound in the initial range.
     */
    private static final int MINIMUM_RANGE_SIZE = 10;
    private static final int LOWER_BOUND_MIN = -1000;
    private static final int UPPER_BOUND_MAX = 1000;
    /**
     * A default value for the Lower Bound of the range.
     */
    private static int lowerBoundDefault = 0;
    /**
     * A default value for the Upper Bound of the range.
     */
    private static int upperBoundDefault = 100;
    private ViewPrinter output;
    private ViewGenerator viewGenerator;
    private InputReader input;
    /**
     * An attempt counter for the current game.
     */
    private int nextAttemptSerialNo = 1;
    /**
     * All attempts of a user during this game.
     */
    private List<UserAttempt> userAttempts;
    /**
     * The current range in which the secret number resides.
     */
    private Range<Integer> currentRange;
    /**
     * The secret number that the computer has picked.
     */
    private int target;
    /**
     * A flag showing whether a user has guessed the secret number or not.
     */
    private boolean targetIsNotHit;
    /**
     * The number picked by a user during the current attempt.
     */
    private int userInput;

    public Game(Environment environment) {
        output = environment.getViewPrinter();
        viewGenerator = environment.getViewGenerator();
        input = environment.getInputReader();
        target = Utils.randomInt();
        userAttempts = new LinkedList<>();
        currentRange = Range.closed(lowerBoundDefault, upperBoundDefault);
        targetIsNotHit = true;
    }

    /**
     * Starts the game.
     */
    public void start() {
        LOGGER.debug("Starting a new Game. Default range: {}...", currentRange);

        displayGameStartMessage();

        do {
            getNewNumberFromUser();
            UserAttempt currentAttempt = createNewAttempt();

            checkInputNumberAndUpdateCurrentAttempt(currentAttempt);

            LOGGER.debug("newAttempt: {}", currentAttempt);
            userAttempts.add(currentAttempt);

        } while (targetIsNotHit);

        printStatisticsAboutGame();

    }

    private void displayGameStartMessage() {
        output.printlnMessageWithKey("generalMessages", "game.startMessage");
    }

    private void getNewNumberFromUser() {
        int userNumber;
        String enterNextNumberMessage = MessageFormat.format(
                output.getMessageWithKey("generalMessages", "menu.enterNextNumber"), currentRange);

        do {
            output.printString(enterNextNumberMessage);
            userNumber = input.readIntValue();
            LOGGER.debug("userNumber = {}", userNumber);
            if(userEnteredIncorrectValue(userNumber)) {
                output.printMessageWithKey("generalMessages", "input.enterNextNumber.error");
            }

        } while (userEnteredIncorrectValue(userNumber));

        userInput = userNumber;
    }

    private UserAttempt createNewAttempt() {
        UserAttempt newAttempt = new UserAttempt(nextAttemptSerialNo, currentRange);
        nextAttemptSerialNo++;
        newAttempt.setNumber(userInput);
        return newAttempt;
    }

    private void checkInputNumberAndUpdateCurrentAttempt(UserAttempt currentAttempt) {
        if (userGuessedTheNumber()) {
            currentAttempt.setResult(UserAttempt.AttemptResult.ATTEMPT_RESULT_SCORE);
            targetIsNotHit = false;
        } else {
            if(userInput < target) {
                currentAttempt.setResult(UserAttempt.AttemptResult.ATTEMPT_RESULT_TOO_SMALL);
                currentRange = getUpperSubRange();
            } else {
                currentAttempt.setResult(UserAttempt.AttemptResult.ATTEMPT_RESULT_TOO_LARGE);
                currentRange = getLowerSubRange();
            }

            currentAttempt.setNewRange(currentRange);
        }
    }

    private boolean userGuessedTheNumber() {
        return (userInput == target);
    }

    private Range<Integer> getLowerSubRange() {
        return Range.closed(currentRange.lowerEndpoint(), userInput - 1);
    }

    private Range<Integer> getUpperSubRange() {
        return Range.closed(userInput + 1, currentRange.upperEndpoint());
    }

    private boolean userEnteredIncorrectValue(int userNumber) {
        return ! currentRange.contains(userNumber);
    }

    public static Range<Integer> getLowerBoundLimits() {
        return Range.closed(LOWER_BOUND_MIN,
                upperBoundDefault - MINIMUM_RANGE_SIZE);
    }

    public static boolean isValueForLowerBoundOk(int newValue) {
        return getLowerBoundLimits().contains(newValue);
    }

    public static void setLowerBoundDefault(int newValue) {
        checkArgument(isValueForLowerBoundOk(newValue));
        lowerBoundDefault = newValue;
    }

    public static Range<Integer> getUpperBoundLimits() {
        return Range.closed(lowerBoundDefault + MINIMUM_RANGE_SIZE,
                UPPER_BOUND_MAX);
    }

    public static boolean isValueForUpperBoundOk(int newValue) {
        return getUpperBoundLimits().contains(newValue);
    }

    public static void setUpperBoundDefault(int newValue) {
        checkArgument(isValueForUpperBoundOk(newValue));
        upperBoundDefault = newValue;
    }

    public static int getLowerBoundDefault() {
        return lowerBoundDefault;
    }

    public static int getUpperBoundDefault() {
        return upperBoundDefault;
    }

    private void printStatisticsAboutGame() {
        LOGGER.debug("...the target has been hit. The game is finished.");
        output.printlnMessageWithKey("generalMessages", "game.targetIsHit");
        output.printlnString(viewGenerator.getGameStatisticsView(userAttempts));
    }
}
