import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

        try (ServerSocket SocketAPI = new ServerSocket(8002);){
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

                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Błąd: " + e);
        }
    }

    private static String register(String request){
        String[] ramka = request.split(";");
        String login = ramka[2].substring(6);
        String haslo = ramka[3].substring(6);

        try (Connection connection = DriverManager.getConnection("", "", "");){
            String usersQuery = "SELECT COUNT(*) AS count FROM Users WHERE Login=?";
            PreparedStatement preparedStatement = connection.prepareStatement(usersQuery);
            preparedStatement.setString(1, login);
        } catch (SQLException e){
            System.out.println("Błąd: " + e);
        }


        return "typ:register;id:10;";
    }

    private static String login(String request){
        return "typ:login;id:20;";
    }
}