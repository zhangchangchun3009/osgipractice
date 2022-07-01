package osgi.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FileUtil {

    private static final String MIME_TYPES_PATH = "config/mime.types";

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    static ForkJoinPool pool = ForkJoinPool.commonPool();

    private static Map<String, String> mimeTypes = new HashMap<String, String>();

    private synchronized static void loadMimeTypes() {
        if (mimeTypes.isEmpty()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(FileUtil.class.getResourceAsStream(MIME_TYPES_PATH)))) {
                br.lines().forEach(line -> {
                    String[] arrs = line.split(" ");
                    if (arrs.length > 1) {
                        mimeTypes.put(arrs[0], arrs[1]);
                    }
                });
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("file not found", e);
            } catch (IOException e) {
                throw new IllegalArgumentException("file io exception", e);
            }
        }
    }

    /**
     * getMimeType 获取文件MIME类型
     * 
     * @param fileName 文件名
     * @return 文件MIME类型
     * */
    public static String getMimeType(String fileName) {
        loadMimeTypes();
        if (!mimeTypes.isEmpty()) {
            String extendName = fileName.substring(fileName.lastIndexOf(".") + 1);
            String mimeType = mimeTypes.get(extendName);
            if (mimeType != null) {
                return mimeType;
            }
        }
        return DEFAULT_MIME_TYPE;
    }

    public static boolean deleteFile(File file) {
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
                return file.delete();
            }
        }
        return false;
    }

    public static boolean deleteFileByForkJoinFrame(File file) {
        return pool.invoke(new FileDeleteTask(file));
    }

    private static class FileDeleteTask extends RecursiveTask<Boolean> {
        private static final long serialVersionUID = 1L;

        private File file;

        public FileDeleteTask(File file) {
            this.file = file;
        }

        @Override
        protected Boolean compute() {
            if (!file.exists()) {
                return true;
            }
            if (file.isFile()) {
                return file.delete();
            }
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return file.delete();
            } else {
                List<FileDeleteTask> tasks = new ArrayList<FileUtil.FileDeleteTask>(files.length);
                for (int i = 0; i < files.length; i++) {
                    FileDeleteTask subtask = new FileDeleteTask(files[i]);
                    tasks.add(subtask);
                }
                invokeAll(tasks);
                boolean done = true;
                for (FileDeleteTask subtask : tasks) {
                    done &= subtask.join().booleanValue();
                }
                if (!done) {
                    return done;
                }
                return file.delete();
            }
        }

    }

}
