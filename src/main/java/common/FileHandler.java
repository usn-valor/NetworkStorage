package common;

import io.netty.buffer.ByteBuf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler {

    //private static final byte MANAGER_BYTE = 3;
    private static final String dir = "/Users/Arsen/IdeaProjects/NetworkStorage/NetworkStorageServer/src/main/resources/UserFiles/";

    private static State currentState = State.NICKNAME_LENGTH;
    private static int nextLength;
    private static int nicknameLength;
    private static String nickname;
    private static long fileLength;
    private static long receivedFileLength;
    private static BufferedOutputStream out;

    public static void acceptFile(Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {

            if (currentState == State.NICKNAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    buf.readByte();
                    System.out.println("STATE: Get nickname length");
                    nicknameLength = buf.readInt();
                    currentState = State.NICKNAME;
                    System.out.println(nicknameLength);
                }
            }

            if (currentState == State.NICKNAME) {
                if (buf.readableBytes() >= nicknameLength) {
                    byte[] nick = new byte[nicknameLength];
                    buf.readBytes(nick);
                    nickname = new String(nick, "UTF-8");
                    System.out.println("STATE: Get nickname");
                    System.out.println(nickname);
                    currentState = State.NAME_LENGTH;
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                    System.out.println(nextLength);
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
                    File userDirectory = new File(dir + nickname);
                    if (!userDirectory.exists())
                        Files.createDirectory(Paths.get(dir + nickname));
                    File file = new File(userDirectory + "/_" + new String(fileName));
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    currentState = State.FILE_LENGTH;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.NICKNAME_LENGTH;
                        System.out.println("File received");
                        out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
