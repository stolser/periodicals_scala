package com.stolser.javatraining.block02.morelessgame.model;

import com.stolser.javatraining.block02.morelessgame.controller.ConsoleInputReader;
import com.stolser.javatraining.block02.morelessgame.controller.InputReader;
import com.stolser.javatraining.block02.morelessgame.controller.MenuController;
import com.stolser.javatraining.block02.morelessgame.model.menu.Menu;
import com.stolser.javatraining.block02.morelessgame.model.menu.MenuItem;
import com.stolser.javatraining.block02.morelessgame.view.ConsoleViewFactory;
import com.stolser.javatraining.block02.morelessgame.view.ViewFactory;

import java.util.Arrays;

public class Application {

    public void start() {
        ViewFactory viewFactory = ConsoleViewFactory.getInstance();
        InputReader inputReader = new ConsoleInputReader(viewFactory.getViewPrinter());
        Menu mainMenu = generateMainMenu();

        MenuController controller = new MenuController(mainMenu, viewFactory, inputReader);
        controller.processUserInput();
    }

    private Menu generateMainMenu() {
        Menu playGame = new MenuItem(null, "playGame", 1);

        Menu setRandomMax = new MenuItem(null, "setRandomMax", 21);
        Menu setLanguage = new MenuItem(null, "setLanguage", 22);
        Menu settings = new MenuItem(Arrays.asList(setRandomMax, setLanguage), "setLanguage", 2);

        Menu instructions = new MenuItem(null, "instructions", 3);
        Menu about = new MenuItem(null, "about", 4);
        Menu exit = new MenuItem(null, "exit", 5);

        Menu mainMenu = new MenuItem(
                Arrays.asList(playGame, settings, instructions, about, exit),
                "mainMenu", 1);

        return mainMenu;
    }
}
