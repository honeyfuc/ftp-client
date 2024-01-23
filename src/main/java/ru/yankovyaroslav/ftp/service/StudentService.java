package ru.yankovyaroslav.ftp.service;

import ru.yankovyaroslav.ftp.client.FTPClient;
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
                printAllStudents(students);
            }
        } else {
            System.out.println("\nОшибка! Вероятно, вы не загрузили файл на сервер.\nПопробуйте снова )))");
        }
    }

    public void addNewStudent() {
        boolean isDownloaded = ftpClient.downloadFile(ftpClient.getFile());
        if (isDownloaded) {
            String username = getNewUsername();
            JsonObject jsonObject = readFileDataToJsonObject();
            if (jsonObject != null) {
                JsonArray jsonArray = jsonObject.getJsonArray("students");

                long nextStudentId = getNextStudentId(jsonArray);

                JsonObject newStudentJsonObject = Json.createObjectBuilder()
                        .add("id", nextStudentId)
                        .add("name", username)
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
            long id = getStudentId();
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

    public void deleteStudentById() {
        boolean isDownloaded = ftpClient.downloadFile(ftpClient.getFile());
        if (isDownloaded) {
            long studentId = getStudentId();
            JsonObject jsonObject = readFileDataToJsonObject();
            if (jsonObject != null) {
                JsonArray studentsJsonArray = jsonObject.getJsonArray("students");
                if (studentId < 0 || studentId > studentsJsonArray.size()) {
                    System.out.println("\nСтудента с id = " + studentId + " нет в данном файле");
                } else {
                    JsonArrayBuilder updatedStudentJsonArray = Json.createArrayBuilder();
                    for (JsonValue value : studentsJsonArray) {
                        JsonObject studentJsonObject = (JsonObject) value;
                        long id = studentJsonObject.getInt("id");
                        if (studentId != id) {
                            updatedStudentJsonArray.add(studentJsonObject);
                        }
                    }
                    JsonObjectBuilder updatedJsonObject = Json.createObjectBuilder(jsonObject);
                    updatedJsonObject.remove("students");
                    updatedJsonObject.add("students", updatedStudentJsonArray.build());
                    writeUpdatedFileToServer(updatedJsonObject.build());
                    System.out.println("\nСтудент с id = " + studentId + " успешно удалён!");
                }
            }
        } else {
            System.out.println("\nОшибка! Вероятно, вы не загрузили файл на сервер.\nПопробуйте снова )))");
        }
    }

    private static long getStudentId() {
        System.out.print("Введите id студента: ");
        return UserAction.getUserId();
    }

    private static String getNewUsername() {
        System.out.print("Введите имя нового студента: ");
        return UserAction.getUsername();
    }

    private static long getNextStudentId(JsonArray jsonArray) {
        long currentMaxId = 0;
        for (JsonValue value : jsonArray) {
            JsonObject studentObject = (JsonObject) value;
            long studentId = (long) studentObject.getInt("id");
            if (studentId > currentMaxId) {
                currentMaxId = studentId;
            }
        }

        return currentMaxId + 1;
    }

    private static void printAllStudents(List<Student> students) {
        System.out.println("\nСписок студентов");
        for (Student student : students) {
            System.out.println("\t" + student);
        }
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

    private JsonObject readFileDataToJsonObject() {
        try {
            InputStream inputStream = new ByteArrayInputStream(ftpClient.getFileBuffer().toByteArray());
            return Json.createReader(inputStream).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JsonObject.EMPTY_JSON_OBJECT;
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
}
