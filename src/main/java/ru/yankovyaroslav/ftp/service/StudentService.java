package ru.yankovyaroslav.ftp.service;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.domain.exception.FileDataException;
import ru.yankovyaroslav.ftp.domain.student.Student;
import ru.yankovyaroslav.ftp.util.UserAction;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        boolean isDownloaded = ftpClient.downloadFile(ftpClient.getFile());
        if (isDownloaded) {
            List<Student> students = null;
            JsonObject studentsJsonObject = readFileDataToJsonObject();
            if (studentsJsonObject != null && !studentsJsonObject.isEmpty()) {
                JsonArray studentsJsonArray = studentsJsonObject.getJsonArray("students");
                students = parseStudents(studentsJsonArray);
            }
            if (students != null) {
                System.out.println("\nСписок студентов");
                for (Student student : students) {
                    System.out.println("\t" + student);
                }
            }
        } else {
            System.out.println("\nОшибка! Вероятно, вы не загрузили файл на сервер.\nПопробуйте снова )))");
        }
    }

    public void getStudentById() {
    }

    public void addNewStudent() {
    }

    public void deleteStudentById() {
    }

    private List<Student> parseStudents(JsonArray jsonArray) {
        List<Student> students = new ArrayList<>();
        for (JsonValue studentValue : jsonArray) {
            JsonObject studentJsonObject = (JsonObject) studentValue;
            Long studentId = (long) studentJsonObject.getInt("id");
            String studentName = studentJsonObject.getString("name");
            students.add(new Student(studentId, studentName));
        }
        students.sort(Comparator.comparing(Student::getName));
        return students;
    }

    private JsonObject readFileDataToJsonObject() {
        try {
            InputStream inputStream = new ByteArrayInputStream(ftpClient.getFileBuffer().toByteArray());
            return Json.createReader(inputStream).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonObject.EMPTY_JSON_OBJECT;
    }


}
