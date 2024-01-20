package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.menu.MenuView;
import ru.yankovyaroslav.ftp.util.UserAction;

public class MainMenu implements MenuView {

    private final int MENU_OPTION_COUNT = 2;

    @Override
    public void showMenu() {
        System.out.println("\n     FTP клиент     ");
        System.out.println("1. Подключиться к FTP серверу");
        System.out.println("2. Выйти");
        handleMenuOption();
    }

    @Override
    public void handleMenuOption() {
        int userChoice = UserAction.getUserChoice(MENU_OPTION_COUNT);
        switch (userChoice) {
            case 1:
                FtpServerConnectionMenu ftpServerConnectionMenu = new FtpServerConnectionMenu();
                ftpServerConnectionMenu.showMenu();
                break;
            case 2:
                System.out.println("Выход из приложения...");
                break;
            default:
                break;
        }
    }

}
