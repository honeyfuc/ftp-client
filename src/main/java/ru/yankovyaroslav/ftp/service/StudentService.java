package ru.yankovyaroslav.ftp.service;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.util.UserAction;

public class StudentService {
    
    private static StudentService instance;

    private FTPClient ftpClient = FTPClient.getInstance();
    private StudentService() {
        
    }
    
    public static StudentService getInstance() {
        if (instance == null) {
            instance = new StudentService();
        }
        return instance;
    }

    public void getAllStudents() {
        System.out.print("\n Введите имя файла, с которым хотите работать: ");
        String fileName = UserAction.getFilePath();
        boolean isDownloaded = ftpClient.downloadFile(fileName);
        if (isDownloaded) {

        }
    }

    public void getStudentById() {
    }

    public void addNewStudent() {
    }

    public void deleteStudentById() {
    }
}
