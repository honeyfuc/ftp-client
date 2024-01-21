package ru.yankovyaroslav.ftp.util;

import ru.yankovyaroslav.ftp.domain.exception.MenuOutOfRangeException;

import java.util.InputMismatchException;
import java.util.Scanner;

public class UserAction {

    private static final Scanner console = new Scanner(System.in);

    public static int getUserChoice(int optionCount) {
        System.out.print("Выберите пункт Меню: ");
        int userChoice = 0;
        try {
            userChoice = console.nextInt();
            if (userChoice <= 0 || userChoice > optionCount) {
                throw new MenuOutOfRangeException("Выбранное значение не удовлетворяет данному Меню.");
            }
        } catch (MenuOutOfRangeException e) {
            System.out.println("Выбранного вами пункта нет в Меню.");
        } catch (InputMismatchException e) {
            System.out.println("Введите числовое значени пункта Меню.");
        }
        console.nextLine();
        return userChoice;
    }


    public static String getServerConnectionData() {
        try {
            return console.nextLine().trim();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String getFilePath() {
        try {
            return console.nextLine().trim();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
