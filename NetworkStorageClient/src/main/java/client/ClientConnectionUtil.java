package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientConnectionUtil {

    private static final String EXIT = "exit";
    private final UserTools userTools;


    public ClientConnectionUtil(UserTools userTools) {
        this.userTools = userTools;
    }

    public static void main(String[] args) {
        new ClientConnectionUtil(new UserTools()).startClient();
    }

    private void startClient() {
        System.out.println("Что вы хотите сделать?..");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String s;
            while (true) {
                s = br.readLine();
                if (s.equals(EXIT))
                    break;
                userTools.commandListener(s);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
