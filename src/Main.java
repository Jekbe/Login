import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static boolean run = true;
    public static void main(String[] args) {
        Thread konsola = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (run) {
                if (scanner.nextLine().equals("exit")) run = false;
                else System.out.println("Nieznana opcja");
            }
        });
        konsola.start();

        try (ServerSocket SocketAPI = new ServerSocket(8002)){
            System.out.println("Serwer działa");
            while (run) {
                Socket socket = SocketAPI.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Nowy klient");

                String request = in.readLine();
                String response = switch (request.split(";")[1]) {
                    case "id:10" -> register(request);
                    case "id:20" -> login(request);
                    default -> "status:400";
                };

                System.out.println("Wysyłanie odpowiedzi: " + response);
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Błąd: " + e);
        }
    }

    private static String register(String request){
        System.out.println("Rejestracja dostał ramkę: " + request);

        String[] ramka = request.split(";");
        String login = ramka[2].substring(6);
        String haslo = ramka[3].substring(6);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mikroserwisy", "root", "")){
            PreparedStatement preparedStatement;
            ResultSet resultSet;

            preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM users WHERE Login=?");
            preparedStatement.setString(1, login);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            if (resultSet.getInt("count") > 0) return "status:406";

            preparedStatement = connection.prepareStatement("INSERT INTO users (Login, Haslo) VALUES (?,?)");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, haslo);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            System.out.println("Błąd: " + e);
            return "status:500";
        }

        System.out.println("Rejestracj przetworzył zapytanie");
        return "status:200";
    }

    private static String login(String request){
        System.out.println("Login dostał ramkę: " + request);

        String[] ramka = request.split(";");
        String login = ramka[2].substring(6);
        String haslo = ramka[3].substring(6);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mikroserwisy", "root", "")){
            PreparedStatement preparedStatement;
            ResultSet resultSet;

            preparedStatement = connection.prepareStatement("SELECT count(*) AS count FROM users WHERE Login=? AND Haslo=?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, haslo);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            if (resultSet.getInt("count") == 0) return "status:401";
        } catch (SQLException e){
            System.out.println("Błąd: " + e);
            return "status:500";
        }

        System.out.println("Login przetworzył zapytanie");
        return "status:200";
    }
}