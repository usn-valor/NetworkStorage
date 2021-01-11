package client;

import common.ManagerByte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CloudServerClient {

    private final UserTools userTools;

    public CloudServerClient(UserTools userTools) {
        this.userTools = userTools;
    }

    public static void main(String[] args) {
        new CloudServerClient(new UserTools()).startClient();
    }

    private void startClient() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String command, str;
            while (true) {
                System.out.println("Что будешь делать?..");
                command = br.readLine();
                switch (command) {
                    case "exit":
                        System.out.println("Выход...");
                        return;
                    case "new_user":
                        byte managerByte = ManagerByte.map.get(command);
                        System.out.println("Введи очень внимательно через запятую имя пользователя, логин и пароль");
                        String newUser = br.readLine();
                        String confirmNewUser;
                        do {
                            System.out.println("Ещё раз введи очень внимательно через запятую имя пользователя, логин и пароль");
                            confirmNewUser = br.readLine();
                        } while (!newUser.equals(confirmNewUser));
                        userTools.createNewUser(confirmNewUser, managerByte);
                        break;
                    case "authorize":
                        managerByte = ManagerByte.map.get(command);
                        String logPass;
                        do {
                            System.out.println("Введи очень внимательно через запятую логин и пароль");
                            logPass = br.readLine();
                        } while (!logPass.matches("\\w+,\\w+"));
                        userTools.authorize(logPass, managerByte);
                        break;
                    case "upload":
                        managerByte = ManagerByte.map.get(command);
                        System.out.println("Введи имя(ена) отправляемого(ых) файла(ов) или директории(й) через запятую (если их больше одного)");
                        str = br.readLine();
                        userTools.uploadFile(str, managerByte);
                        break;
                    case "download":
                        managerByte = ManagerByte.map.get(command);
                        System.out.println("Введи имя(ена) загружаемого(ых) файла(ов) или директории(й) через запятую (если их больше одного)");
                        str = br.readLine();
                        userTools.downloadFile(str, managerByte);
                        break;
                    default:
                        System.out.println("Ты ввёл неправильную команду...");
                    }
                }
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
        }
    }
}
