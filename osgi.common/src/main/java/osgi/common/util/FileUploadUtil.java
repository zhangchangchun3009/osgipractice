
package osgi.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileUploadUtil.
 *
 * @author zhangchangchun
 * @since 2021年4月8日
 */
public class FileUploadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadUtil.class);

    /**
     * 从request中提取文件
     * 
     * @param request
     * @return
     */
    public static List<FileItem> parseRequest(HttpServletRequest request) {
        if (!ServletFileUpload.isMultipartContent(request)) {
            return new ArrayList<>();
        }
        // 大于5M的文件将被缓存为临时文件
        int sizeThreshold = 5 * 1024 * 1024;
        File repository = new File(SystemUtils.JAVA_IO_TMPDIR);
        FileItemFactory fileItemFactory = new DiskFileItemFactory(sizeThreshold, repository);
        ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
        fileUpload.setHeaderEncoding("UTF-8");
        fileUpload.setProgressListener(new ProgressListener() {
            @Override
            public void update(long pBytesRead, long pContentLength, int arg2) {
                LOGGER.trace("文件大小为：" + pContentLength + ",当前已处理：" + pBytesRead);
            }
        });
        // 设置上传单个文件的大小的最大值，
        fileUpload.setFileSizeMax(10 * 1024 * 1024);
        // 设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和
        fileUpload.setSizeMax(1024 * 1024 * 20);
        List<FileItem> fileItemList = new ArrayList<FileItem>();
        try {
            List<FileItem> allItemList = fileUpload.parseRequest(request);
            for (FileItem temp : allItemList) {
                if (!temp.isFormField()) {
                    fileItemList.add(temp);
                }
            }
        } catch (FileUploadException e) {
            LOGGER.error("FileUploadException:", e);
        }
        return fileItemList;

    }

    /**
     * 将上传文件写入指定目录
     *
     * @param fileItemList the file item list
     * @param savePath the save path
     */
    public static void saveFile(List<FileItem> fileItemList, String savePath) {
        for (FileItem fileItem : fileItemList) {
            if (fileItem != null) {
                String fileName = fileItem.getName();
                // 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：
                // c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
                // 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
                fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                // 得到上传文件的扩展名
                String fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
                LOGGER.trace("上传文件的扩展名为:" + fileExtName);
                // String savePath =
                // this.getServletContext().getRealPath("/WEB-INF/upload");
                String savefileName = mkFileName(fileName);
                // 得到文件保存的路径
                String savePathStr = mkFilePath(savePath, fileName);
                LOGGER.trace("保存文件名为:" + savefileName + " 保存路径为:" + savePathStr);
                // 获取item中的上传文件的输入流
                try (InputStream in = fileItem.getInputStream();
                        FileOutputStream fos = new FileOutputStream(savePathStr + File.separator + savefileName);) {
                    // 创建一个缓冲区
                    byte[] byteBuf = new byte[8 * 1024];
                    while (true) {
                        int len = in.read(byteBuf);
                        if (len < 0) {
                            break;// 读取完毕
                        }
                        fos.write(byteBuf);// 写入数据
                    }
                    fileItem.delete();
                } catch (IOException e) {
                    LOGGER.error("FileUploadException:", e);
                }
            }
        }

    }

    /**
     * 生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
     * 
     * @param fileName
     * @return
     */
    public static String mkFileName(String fileName) {
        return UUID.randomUUID().toString().replace("_", "") + "_" + fileName;
    }

    /**
     * 生成文件路径
     * 
     * @param savePath
     * @param fileName
     * @return
     */
    public static String mkFilePath(String savePath, String fileName) {
        // 构造新的保存目录
        String dir = savePath;
        // File既可以代表文件也可以代表目录
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir;
    }

}
