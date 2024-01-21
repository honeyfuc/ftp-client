package ru.yankovyaroslav.ftp;

import ru.yankovyaroslav.ftp.menu.impl.MainMenu;

public class Application {

    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        mainMenu.showMenu();
    }
}
