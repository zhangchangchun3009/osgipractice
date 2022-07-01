package osgi.common.util.modbustcp.slave.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import osgi.common.util.JacksonUtil;
import osgi.common.util.PropertyUtil;
import osgi.common.util.modbustcp.slave.db.ModbusPoolDBMS.ModbusPoolDB;

/**
 * @author zhangchangchun
 * @Date 2022年3月29日
 */
public class Serializer {

    private static final String CHARSET_UTF_8 = "UTF-8";

    private static final String STORE_FILE_NAME = ".modbus.db";

    private static ObjectMapper mapper = JacksonUtil.getDefaultObjectMapper();

    private static String defaultFilePath;

    private static String fileDirPath;

    private static String fileFullPath;

    static {
        defaultFilePath = PropertyUtil.getBaseDir();
        fileDirPath = PropertyUtil.getAppPropAsString("scm.common.modbusslave.serializeFilePath", defaultFilePath);
        fileDirPath = fileDirPath.endsWith("/") || fileDirPath.endsWith("\\") ? fileDirPath : fileDirPath + "/";
        fileFullPath = fileDirPath + STORE_FILE_NAME;
    }

    public static synchronized void serialize() throws IOException {
        File storeFileDir = new File(fileDirPath);
        if (!storeFileDir.exists()) {
            storeFileDir.mkdirs();
        }
        Map<String, ModbusPoolDB> holder = ModbusPoolDBMS.getInstanceHolder();
        List<SerialData> object = new ArrayList<SerialData>();
        for (String unitId : holder.keySet()) {
            ModbusPoolDB pool = holder.get(unitId);
            short[] data = pool.holdingRegisters;
            SerialData database = new SerialData(unitId, data);
            object.add(database);
        }
        File storeFile = new File(fileFullPath);
        try (FileOutputStream fout = new FileOutputStream(storeFile);) {
            String text = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            fout.write(text.getBytes(CHARSET_UTF_8));
            fout.flush();
        }
    }

    public static void deserialize() throws IOException {
        File storeFile = new File(fileFullPath);
        if (!storeFile.exists()) {
            return;
        }
        try (FileInputStream fin = new FileInputStream(storeFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(fin, CHARSET_UTF_8))) {
            char[] buffer = new char[1024];
            int charRead = 0;
            StringBuilder sb = new StringBuilder(512);
            while ((charRead = br.read(buffer, 0, 1024)) != -1) {
                sb.append(Arrays.copyOfRange(buffer, 0, charRead));
            }
            String serialized = sb.toString();
            if (serialized == null || serialized.length() == 0) {
                return;
            }
            JsonNode root = mapper.readTree(serialized);
            if (!root.isArray()) {
                return;
            }
            Map<String, ModbusPoolDB> holder = ModbusPoolDBMS.getInstanceHolder();
            ArrayNode rootA = (ArrayNode) root;
            for (JsonNode dbNode : rootA) {
                SerialData database = mapper.treeToValue(dbNode, SerialData.class);
                if (database == null || database.getUnitId() == null || database.getData() == null
                        || database.getData().length == 0) {
                    continue;
                }
                String unitId = database.getUnitId();
                short[] data = database.getData();
                ModbusPoolDB db = holder.get(unitId);
                if (db == null) {
                    continue;
                }
                short[] table = db.holdingRegisters;
                int length = Math.min(table.length, data.length);
                for (int i = 0; i < length; i++) {
                    table[i] = data[i];
                }
            }
        }
    }

}
