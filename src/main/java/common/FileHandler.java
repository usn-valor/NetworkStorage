package common;

import io.netty.buffer.ByteBuf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FileHandler {

    private static final String MANAGER_STRING = "upload";

    private static State currentState = State.IDLE;
    private static int nextLength;
    private static long fileLength;
    private static long receivedFileLength;
    private static BufferedOutputStream out;
    private static File file;

    public static void sendFile(Object obj) {

    }

    public static File acceptFile(Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        System.out.println(buf.getByte(0));
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == ManagerByte.map.get(MANAGER_STRING)) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
                    file = new File("_" + new String(fileName));
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
                        currentState = State.IDLE;
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
        return file;
    }
}
