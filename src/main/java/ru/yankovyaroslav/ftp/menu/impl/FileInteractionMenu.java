package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.domain.exception.FileDataException;
import ru.yankovyaroslav.ftp.menu.MenuView;
import ru.yankovyaroslav.ftp.service.StudentService;
import ru.yankovyaroslav.ftp.util.UserAction;

public class FileInteractionMenu implements MenuView {

    private final int MENU_OPTION_COUNT = 5;

    private StudentService studentService;

    public FileInteractionMenu(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public void showMenu() {
        System.out.println("\n     Работа с файлом (" + FTPClient.getInstance().getFile() + ")     ");
        System.out.println("1. Получить список всех студентов");
        System.out.println("2. Получить информацию о студенте по id");
        System.out.println("3. Добавить студента");
        System.out.println("4. Удалить студента по id");
        System.out.println("5. Назад");
        handleMenuOption();
    }

    @Override
    public void handleMenuOption() {
        int userChoice = UserAction.getUserChoice(MENU_OPTION_COUNT);
        switch (userChoice) {
            case 1:
                studentService.getAllStudents();
                showMenu();
                break;
            case 2:
                long studentId = getStudentId();
                studentService.getStudentById(studentId);
                showMenu();
                break;
            case 3:
                studentService.addNewStudent();
                showMenu();
                break;
            case 4:
                long id = getStudentId();
                studentService.deleteStudentById(id);
                showMenu();
                break;
            case 5:
                showServerInteractionMenu();
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

    private static long getStudentId() {
        System.out.print("Введите id студента: ");
        return UserAction.getUserId();
    }

}
