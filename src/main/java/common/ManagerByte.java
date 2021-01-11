package common;

import java.util.HashMap;
import java.util.Map;

public class ManagerByte {

    public static Map<String, Byte> map = new HashMap();

    static {
        map.put("new_user", (byte) 1);
        map.put("authorize", (byte) 2);
        map.put("upload", (byte) 3);
        map.put("download", (byte) 4);
    }
}
