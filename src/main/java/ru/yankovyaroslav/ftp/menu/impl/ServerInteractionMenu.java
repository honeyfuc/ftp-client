package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.menu.MenuView;
import ru.yankovyaroslav.ftp.service.StudentService;
import ru.yankovyaroslav.ftp.util.UserAction;

import java.awt.*;
import java.io.IOException;
import java.util.Scanner;

public class ServerInteractionMenu implements MenuView {

    private final int MENU_OPTION_COUNT = 4;

    private FTPClient ftpClient = FTPClient.getInstance();

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
                boolean isUploaded = false;
                try {
                    isUploaded = ftpClient.uploadFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getToNextStep(file, isUploaded);
                break;
            case 2:
                showFileInteractionMenu();
                break;
            case 3:
                break;
            case 4:
                System.out.println("Выход из проиложения...");
                System.exit(0);
            default:
                this.showMenu();
                break;
        }
    }

    private static void showFileInteractionMenu() {
        FileInteractionMenu fileInteractionMenu = new FileInteractionMenu(StudentService.getInstance());
        fileInteractionMenu.showMenu();
    }

    private void getToNextStep(String file, boolean isUploaded) {
        if (isUploaded) {
            System.out.printf("SUCCESSFUL !!! Файл %s был успешно загружен на FTP сервер\n", file);
            this.showMenu();
        } else {
            System.out.printf("ERROR !!! Файл %s не удалось загрузить на FTP сервер.\nПопробуйте заново.\n", file);
            this.showMenu();
        }
    }
}
