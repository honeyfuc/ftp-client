package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.menu.MenuView;
import ru.yankovyaroslav.ftp.util.UserAction;

import java.awt.*;
import java.util.Scanner;

public class ServerInteractionMenu implements MenuView {

    private final int MENU_OPTION_COUNT = 3;

    @Override
    public void showMenu() {
        System.out.println("\n     Действия     ");
        System.out.println("1. Загрузить файл на сервер");
        System.out.println("2. Выбрать действие");
        System.out.println("3. Логирование");
        System.out.println("4. Выйти");
        handleMenuOption();
    }

    @Override
    public void handleMenuOption() {
        int userChoice = UserAction.getUserChoice(MENU_OPTION_COUNT);
        switch (userChoice) {
            case 1:
                System.out.print("Введите путь до файла, который хотите загрузить: ");
                String file = UserAction.getFilePath();
                FTPClient ftpClient = FTPClient.getInstance();
                ftpClient.uploadFile(file);
                this.showMenu();
            case 2:
                System.out.println("Выход из приложения...");
                break;
            default:
                this.showMenu();
                break;
        }
    }
}
