package tech.powerjob.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author: zmx
 * @date 2021/4/19 14:17
 */
@Slf4j
public class ZipAndRarTools {

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    /**
     * @param rarFile  rar文件路径
     * @param savePath 要解压的路径
     */
    public static void unrar(String rarFile, String savePath) throws IOException {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;

        // 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
        randomAccessFile = new RandomAccessFile(rarFile, "r");
        inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

        int[] in = new int[inArchive.getNumberOfItems()];
        for (int i = 0; i < in.length; i++) {
            in[i] = i;
        }
        inArchive.extract(in, false, new ExtractCallback(inArchive, "366", savePath));

    }

    public static void main(String[] args) throws Exception {
        File file = new File("E:\\home\\Desktop.zip");
        deCompress(file.getPath(), file.getParent());
    }

    public static void unzip(String rarFile, String savePath) {
        //  String Inputpath = "E:\\b";//压缩包地址
        //  String outpath = "E:\\a";//解压到的文件地址
        ZipEntry zipEntry = null;
        try (
                // ZipInputStream读取压缩文件  //压缩包名称
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(rarFile), Charset.forName("GBK"));
                // 写入到缓冲流中
                BufferedInputStream bufferedInputStream = new BufferedInputStream(zipInputStream);
        ) {
            File fileOut = null;
            // 读取压缩文件中的一个文件
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                // 若当前zipEntry是一个文件夹
                if (zipEntry.isDirectory()) {
                    fileOut = new File(savePath + "\\" + zipEntry.getName());
                    // 在指定路径下创建文件夹
                    if (!fileOut.exists()) {
                        fileOut.mkdirs();
                    }
                    // 若是文件
                } else {
                    // 原文件名与指定路径创建File对象(解压后文件的对象)
                    fileOut = new File(savePath, zipEntry.getName());
                    try (FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);) {
                        // 将文件写入到指定file中
                        int b = 0;
                        while ((b = bufferedInputStream.read()) != -1) {
                            bufferedOutputStream.write(b);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void unTar(String archive, String target) throws IOException {

        File file = new File(archive);

        BufferedInputStream bis = null;

        BufferedOutputStream bos = null;

        GzipCompressorInputStream gcis = null;

        String finalName = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));

            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));

            finalName = file.getParent() + File.separator + fileName;

            bos = new BufferedOutputStream(new FileOutputStream(finalName));
            try {
                gcis = new GzipCompressorInputStream(bis);
                byte[] buffer = new byte[1024];
                int read = -1;
                while ((read = gcis.read(buffer)) != -1) {
                    bos.write(buffer, 0, read);
                }
            } catch (Exception e) {
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (gcis != null) {
                gcis.close();
            }
            if (bis != null) {
                bis.close();
            }
        }

        unCompressTar(finalName, target);
    }

    private static String unCompressTar(String finalName, String target) {
        File file = new File(finalName);
        String basePath = target;
        TarArchiveInputStream tais = null;
        FileOutputStream os = null;
        TarArchiveEntry entry = null;
        String finPath = null;
        try {
            tais = new TarArchiveInputStream(new FileInputStream(file));
            while ((entry = tais.getNextTarEntry()) != null) {
                // 压缩文件下存在文件夹
                if (entry.isDirectory()) {
                    // 一般不会执行
                    new File(basePath + entry.getName()).mkdirs();
                } else {
                    finPath = basePath + entry.getName();
                    File f = new File(finPath);
                    // 父文件夹不存在，创建文件夹
                    if (!f.getParentFile().exists()) {
                        f.getParentFile().mkdirs();
                    }
                    // 文件不存在，创建文件
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                    os = new FileOutputStream(f);
                    byte[] bs = new byte[2048];
                    int len = -1;
                    while ((len = tais.read(bs)) != -1) {
                        os.write(bs, 0, len);
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (tais != null) {
                    tais.close();
                }
                // 解压后删除tar包
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return finPath;
    }



    /**
     * @return void
     * @Description //TODO 支持rar的解压(需要引入winrar.exe程序，放入target目录)
     * @Date 9:35 2020/10/16
     * @Param [sourceRarPath, destDir] sourceRar 需要解压的rar文件全路径;destDir 需要解压到的文件目录
     **/

    public static Boolean unRar(String sourceRarPath, String destDir) throws IOException {
        File zipFile = new File(sourceRarPath); // 解决路径中存在/..格式的路径问题

//        File localFile = new File(destDir);
//        if (localFile.exists()) {
//            FileUtils.forceDelete(localFile);
//        }
//        localFile.mkdir();
        while (destDir.contains("..")) {
            String[] sepList = destDir.split("\\\\");
            destDir = "";
            for (int i = 0; i < sepList.length; i++) {
                if (!"..".equals(sepList[i]) && i < sepList.length - 1 && "..".equals(sepList[i + 1])) {
                    i++;
                } else {
                    destDir += sepList[i] + File.separator;
                }
            }
        } // 获取WinRAR.exe的路径，放在java web工程下的WebRoot路径下

//        String classPath = "";
//        try {
//            classPath = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
//        } catch (URISyntaxException e1) {
//            e1.printStackTrace();
//        } // 兼容main方法执行和javaweb下执行

//        String winrarPath = (classPath.indexOf("WEB-INF") > -1 ? classPath.substring(0, classPath.indexOf("WEB-INF")) : classPath.substring(0, classPath.indexOf("classes"))) + "/WinRAR/WINRar.exe";
//        winrarPath = new File(winrarPath).getAbsoluteFile().getAbsolutePath();
        String system = System.getProperty("os.name").toLowerCase();
        String cmd = "";
        if(isWindows()){
            String winrarPath = "C:\\Program Files\\WinRAR\\WinRAR.exe";
            cmd = winrarPath + " x -o+ " + zipFile + " " + destDir;
        }else if(isLinux()){
            cmd = "rar x -o+ "+ zipFile + " " + destDir;
        }
        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec(cmd);
            log.info("rar 解压开始");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String line = reader.readLine();
            while (line != null) {
                log.info(line);
                line = reader.readLine();
            }
            log.info("rar 解压完成");
            reader.close();
            if (p.waitFor() != 0) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * @return java.lang.String
     * @Description //TODO 调用linux命令解压（linux环境下支持--需要安装相关命令环境）
     * @Date 9:45 2020/10/19
     * @Param [proPath] 压缩包绝对路径
     **/

    public static String run(String zip_path) throws IOException {
        String localFilePath = zip_path.substring(0, zip_path.indexOf("."));
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            FileUtils.forceDelete(localFile);
        }
        localFile.mkdir();
        String[] command = new String[3];
        String zipInfo = "";
        if (zip_path.endsWith(".tar.gz")) {
            log.info("--.tar.gz 解压开始");
            zipInfo = ".tar.gz";
            command = new String[]{
                    "/bin/sh", "-c", "tar -zxvf " + zip_path + " -C " + localFilePath
            };
        } else if (zip_path.endsWith(".tar")) {
            log.info("--.tar 解压开始");
            zipInfo = ".tar";
            command = new String[]{
                    "/bin/sh", "-c", "tar xvf " + zip_path + " -C " + localFilePath
            };
        } else if (zip_path.endsWith(".rar")) {
            log.info("--.rar 解压开始");
            zipInfo = ".rar";
            command = new String[]{
                    "/bin/sh", "-c", "unrar e " + zip_path + " " + localFilePath
            };
        }
        Scanner input = null;
        String result = "";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            try { //等待命令执行完成

                process.waitFor(15, TimeUnit.SECONDS);
                log.info("--" + zipInfo + " 解压完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("--" + zipInfo + " 解压失败");
            }
            InputStream is = process.getInputStream();
            input = new Scanner(is);
            while (input.hasNextLine()) {
                result += input.nextLine() + "\n";
            }
            result = command + "\n" + result; //加上命令本身，打印出来

        } finally {
            if (input != null) {
                input.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }


    /**
     * 解压缩
     */
    public static void deCompress(String sourceFile, String destDir) throws Exception {
        //保证文件夹路径最后是"/"或者"\"
        char lastChar = destDir.charAt(destDir.length() - 1);
        if (lastChar != '/' && lastChar != '\\') {
            destDir += File.separator;
        }
        //根据类型，进行相应的解压缩
        String type = sourceFile.substring(sourceFile.lastIndexOf(".") + 1);
        if (type.equals("zip")) {
            unzip(sourceFile, destDir);
        } else if (type.equals("rar")) {
            unRar(sourceFile, destDir);
        } else if(type.equals("gz")){
            unTar(sourceFile, destDir);
        }else {
            throw new Exception("只支持tar.gz、zip、rar格式的压缩包！");
        }
    }
}
