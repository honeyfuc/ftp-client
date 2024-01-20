package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.menu.MenuView;
import ru.yankovyaroslav.ftp.util.UserAction;

public class FtpServerConnectionMenu implements MenuView {

    @Override
    public void showMenu() {
        System.out.println("\n     Подключение к FTP серверу     ");
        System.out.print("IP-адресс сервера: ");
        String ftpServerIp = UserAction.getServerConnectionData();
        System.out.print("Логин: ");
        String username = UserAction.getServerConnectionData();
        System.out.print("Пароль: ");
        String password = UserAction.getServerConnectionData();

    }

    @Override
    public void handleMenuOption() {

    }

}
