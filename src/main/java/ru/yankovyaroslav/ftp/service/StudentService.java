package ru.yankovyaroslav.ftp.service;

import ru.yankovyaroslav.ftp.client.FTPClient;
import ru.yankovyaroslav.ftp.domain.exception.FileDataException;
import ru.yankovyaroslav.ftp.domain.student.Student;
import ru.yankovyaroslav.ftp.util.UserAction;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
            List<Student> students = getStudents();
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

    public void addNewStudent() {
        boolean isDownloaded = ftpClient.downloadFile(ftpClient.getFile());
        if (isDownloaded) {
            System.out.print("Введите имя нового студента: ");
            String username = UserAction.getUsername();
            JsonObject jsonObject = readFileDataToJsonObject();
            if (jsonObject != null) {
                JsonArray jsonArray = jsonObject.getJsonArray("students");

                long currentMaxId = 0;
                for (JsonValue value : jsonArray) {
                    JsonObject studentObject = (JsonObject) value;
                    long studentId = (long) studentObject.getInt("id");
                    if (studentId > currentMaxId) {
                        currentMaxId = studentId;
                    }
                }

                long nextStudentId = currentMaxId + 1;

                JsonObject newStudentJsonObject = Json.createObjectBuilder()
                        .add("id", nextStudentId)
                        .add("name",username)
                        .build();

                JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder(jsonArray)
                        .add(newStudentJsonObject);

                JsonObject updatedJsonObject = Json.createObjectBuilder(jsonObject)
                        .add("students", jsonArrayBuilder)
                        .build();

                writeUpdatedFileToServer(updatedJsonObject);
            }
        } else {
            System.out.println("\nОшибка! Вероятно, вы не загрузили файл на сервер.\nПопробуйте снова )))");
        }
    }

    public void getStudentById() {
        boolean isDownloaded = ftpClient.downloadFile(ftpClient.getFile());
        if (isDownloaded) {
            System.out.print("Введите id студента: ");
            long id = UserAction.getUserId();
            List<Student> students = getStudents();
            Optional<Student> student = students.stream().filter(x -> x.getId().equals(id)).findFirst();
            if (student.isPresent()) {
                System.out.println("\nСтудент с id = " + id);
                System.out.println("\t" + student.get());
            } else {
                System.out.println("\nСтудента с id = " + id + " нет в данном файле");
            }

        } else {
            System.out.println("\nОшибка! Вероятно, вы не загрузили файл на сервер.\nПопробуйте снова )))");
        }
    }

    private void writeUpdatedFileToServer(JsonObject updatedJsonObject) {
        try {
            byte[] bytes = updatedJsonObject.toString().getBytes(StandardCharsets.UTF_8);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);
            ftpClient.setFileBuffer(outputStream);
            outputStream.write(bytes, 0, bytes.length);
            ftpClient.updateFileOnServer(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteStudentById() {
    }

    private List<Student> getStudents() {
        List<Student> students = null;
        JsonObject studentsJsonObject = readFileDataToJsonObject();
        if (studentsJsonObject != null && !studentsJsonObject.isEmpty()) {
            JsonArray studentsJsonArray = studentsJsonObject.getJsonArray("students");
            students = parseStudents(studentsJsonArray);
        }
        return students;
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
