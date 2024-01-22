package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.domain.ServerMode;
import ru.yankovyaroslav.ftp.menu.MenuView;
import ru.yankovyaroslav.ftp.util.UserAction;

import java.awt.*;

public class ServerModeMenu implements MenuView {

    private final int MENU_OPTION_COUNT = 2;

    private FTPClient ftpClient = FTPClient.getInstance();
    @Override
    public void showMenu() {
        System.out.println("\n     Режимы работы     ");
        System.out.println("1. Пассивный режим");
        System.out.println("2. Активный режим");
        handleMenuOption();
    }

    @Override
    public void handleMenuOption() {
        int userChoice = UserAction.getUserChoice(MENU_OPTION_COUNT);
        switch (userChoice) {
            case 1:
                ftpClient.setServerMode(ServerMode.PASSIVE);
                showServerInteractionMenu();
                break;
            case 2:
                System.out.println("Выход из приложения...");
                break;
            default:
                this.showMenu();
                break;
        }
    }

    private static void showServerInteractionMenu() {
        ServerInteractionMenu serverInteractionMenu = new ServerInteractionMenu();
        serverInteractionMenu.showMenu();
    }

}
