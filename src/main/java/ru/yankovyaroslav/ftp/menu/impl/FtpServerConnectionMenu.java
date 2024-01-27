package ru.yankovyaroslav.ftp.menu.impl;

import ru.yankovyaroslav.ftp.client.FTPClient;
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

        boolean isConnected = connectServer(ftpServerIp, username, password);

        if (isConnected) {
            showServerModeMenu();
        }
    }

    private boolean connectServer(String ftpServerIp,
                               String username,
                               String password) {
        FTPClient ftpClient = FTPClient.getInstance();
        ftpClient.setHost(ftpServerIp);
        ftpClient.setUsername(username);
        ftpClient.setPassword(password);
        ftpClient.connect();

        return ftpClient.isConnected();
    }

    public void showError(String errorMessage) {
        System.out.println("Ошибка при соединении с FTP сервером");
        showMenu();
    }

    private static void showServerModeMenu() {
        ServerModeMenu serverModeMenu = new ServerModeMenu();
        serverModeMenu.showMenu();
    }

    @Override
    public void handleMenuOption() {

    }

}
