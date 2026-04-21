package org.jeecg.common.util.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @author: lsq
 * @date: 2021年08月09日 15:29
 */
public class FileTypeFilter {

    // 禁止上传的文件类型（黑名单）
    private static String[] forbidType = {
        "jsp", "php", "asp", "aspx", "jspx", "jspf", "jspa", "jsw", "jsv",
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "vbe", "ws", "wsf",
        "wsh", "msc", "jar", "sh", "class", "dll", "so", "dylib",
        "html", "htm", "js", "css", "xml", "sql", "java", "py", "rb", "pl",
        "sh", "ps1", "psm1", "psd1", "psc1", "msh", "msh1", "msh2", "mshxml",
        "msh1xml", "msh2xml", "cpl", "msi", "msp", "mst", "ade", "adp",
        "app", "bas", "chm", "crt", "csh", "fxp", "hlp", "hta", "inf", "ins",
        "isp", "its", "js", "jse", "ksh", "lnk", "mad", "maf", "mag", "mam",
        "maq", "mar", "mas", "mat", "mau", "mav", "maw", "mda", "mdb", "mde",
        "mdt", "mdw", "mdz", "msc", "msh", "msh1", "msh2", "mshxml", "msh1xml",
        "msh2xml", "msi", "msp", "mst", "ops", "pcd", "pif", "pl", "plg", "prf",
        "prg", "pst", "reg", "scf", "scr", "sct", "shb", "shs", "tmp", "url",
        "vb", "vbe", "vbs", "wsc", "wsf", "wsh", "xnk"
    };

    // 允许上传的文件类型（白名单）
    private static String[] allowedType = {
        // 图片
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tif", "tiff",
        // 文档
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp",
        // 压缩文件
        "zip", "rar", "7z", "tar", "gz",
        // 视频
        "mp4", "avi", "mov", "wmv", "flv", "mkv", "m4v", "3gp",
        // 音频
        "mp3", "wav", "wma", "flac", "aac", "ogg", "m4a"
    };

    // 初始化文件头类型，不够的自行补充
    final static HashMap<String, String> fileTypeMap = new HashMap<>();

    static {
        // 危险文件类型
        fileTypeMap.put("3c25402070616765206c", "jsp");
        fileTypeMap.put("3c3f7068700a0a2f2a2a0a202a205048", "php");
        fileTypeMap.put("3c25402070616765206c", "jsp");
        fileTypeMap.put("3c3f786d6c2076657273", "xml");
        fileTypeMap.put("4d5a9000030000000400", "exe");
        fileTypeMap.put("504b03040a0000000000", "jar");
        fileTypeMap.put("cafebabe0000002e0041", "class");
        fileTypeMap.put("406563686f206f66660d", "bat");
        
        // 允许的文件类型（图片）
        fileTypeMap.put("ffd8ffe000104a464946", "jpg");
        fileTypeMap.put("89504e470d0a1a0a0000", "png");
        fileTypeMap.put("47494638396126026f01", "gif");
        fileTypeMap.put("49492a00227105008037", "tif");
        fileTypeMap.put("424d228c010000000000", "bmp");
        fileTypeMap.put("424d8240090000000000", "bmp");
        fileTypeMap.put("424d8e1b030000000000", "bmp");
        
        // 允许的文件类型（文档）
        fileTypeMap.put("255044462d312e350d0a", "pdf");
        fileTypeMap.put("d0cf11e0a1b11ae10000", "doc");
        fileTypeMap.put("504b0304140006000800", "docx");
        fileTypeMap.put("504b0304140006000800", "xlsx");
        fileTypeMap.put("504b0304140006000800", "pptx");
        
        // 允许的文件类型（压缩文件）
        fileTypeMap.put("504b0304140000000800", "zip");
        fileTypeMap.put("526172211a0700cf9073", "rar");
        
        // 允许的文件类型（视频）
        fileTypeMap.put("00000020667479706d70", "mp4");
        fileTypeMap.put("52494646d07d60074156", "avi");
        fileTypeMap.put("6D6F6F76", "mov");
        fileTypeMap.put("464c5601050000000900", "flv");
        
        // 允许的文件类型（音频）
        fileTypeMap.put("49443303000000002176", "mp3");
        fileTypeMap.put("52494646e27807005741", "wav");
       /* fileTypeMap.put("ffd8ffe000104a464946", "jpg");
        fileTypeMap.put("89504e470d0a1a0a0000", "png");
        fileTypeMap.put("47494638396126026f01", "gif");
        fileTypeMap.put("49492a00227105008037", "tif");
        fileTypeMap.put("424d228c010000000000", "bmp");
        fileTypeMap.put("424d8240090000000000", "bmp");
        fileTypeMap.put("424d8e1b030000000000", "bmp");
        fileTypeMap.put("41433130313500000000", "dwg");
        fileTypeMap.put("3c21444f435459504520", "html");
        fileTypeMap.put("3c21646f637479706520", "htm");
        fileTypeMap.put("48544d4c207b0d0a0942", "css");
        fileTypeMap.put("696b2e71623d696b2e71", "js");
        fileTypeMap.put("7b5c727466315c616e73", "rtf");
        fileTypeMap.put("38425053000100000000", "psd");
        fileTypeMap.put("46726f6d3a203d3f6762", "eml");
        fileTypeMap.put("d0cf11e0a1b11ae10000", "doc");
        fileTypeMap.put("5374616E64617264204A", "mdb");
        fileTypeMap.put("252150532D41646F6265", "ps");
        fileTypeMap.put("255044462d312e350d0a", "pdf");
        fileTypeMap.put("2e524d46000000120001", "rmvb");
        fileTypeMap.put("464c5601050000000900", "flv");
        fileTypeMap.put("00000020667479706d70", "mp4");
        fileTypeMap.put("49443303000000002176", "mp3");
        fileTypeMap.put("000001ba210001000180", "mpg");
        fileTypeMap.put("3026b2758e66cf11a6d9", "wmv");
        fileTypeMap.put("52494646e27807005741", "wav");
        fileTypeMap.put("52494646d07d60074156", "avi");
        fileTypeMap.put("4d546864000000060001", "mid");
        fileTypeMap.put("504b0304140000000800", "zip");
        fileTypeMap.put("526172211a0700cf9073", "rar");
        fileTypeMap.put("235468697320636f6e66", "ini");
        fileTypeMap.put("504b03040a0000000000", "jar");
        fileTypeMap.put("4d5a9000030000000400", "exe");
        fileTypeMap.put("3c25402070616765206c", "jsp");
        fileTypeMap.put("4d616e69666573742d56", "mf");
        fileTypeMap.put("3c3f786d6c2076657273", "xml");
        fileTypeMap.put("494e5345525420494e54", "sql");
        fileTypeMap.put("7061636b616765207765", "java");
        fileTypeMap.put("406563686f206f66660d", "bat");
        fileTypeMap.put("1f8b0800000000000000", "gz");
        fileTypeMap.put("6c6f67346a2e726f6f74", "properties");
        fileTypeMap.put("cafebabe0000002e0041", "class");
        fileTypeMap.put("49545346030000006000", "chm");
        fileTypeMap.put("04000000010000001300", "mxp");
        fileTypeMap.put("504b0304140006000800", "docx");
        fileTypeMap.put("6431303a637265617465", "torrent");
        fileTypeMap.put("6D6F6F76", "mov");
        fileTypeMap.put("FF575043", "wpd");
        fileTypeMap.put("CFAD12FEC5FD746F", "dbx");
        fileTypeMap.put("2142444E", "pst");
        fileTypeMap.put("AC9EBD8F", "qdf");
        fileTypeMap.put("E3828596", "pwl");
        fileTypeMap.put("2E7261FD", "ram");*/
    }

    /**
     * @param fileName
     * @return String
     * @description 通过文件后缀名获取文件类型
     */
    private static String getFileTypeBySuffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            // 没有扩展名或扩展名为空
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 文件类型过滤（使用白名单机制）
     *
     * @param file
     */
    public static void fileTypeFilter(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("上传失败，文件为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new Exception("上传失败，文件名为空");
        }
        
        // 获取文件扩展名（小写）
        String suffix = getFileTypeBySuffix(fileName).toLowerCase();
        
        // 如果没有扩展名，拒绝上传
        if (StringUtils.isBlank(suffix)) {
            throw new Exception("上传失败，文件必须包含扩展名");
        }
        
        // 检查是否在禁止列表中
        for (String type : forbidType) {
            if (type.equalsIgnoreCase(suffix)) {
                throw new Exception("上传失败，不允许上传该类型文件：" + suffix);
            }
        }
        
        // 白名单验证：只允许白名单中的文件类型
        boolean isAllowed = false;
        for (String type : allowedType) {
            if (type.equalsIgnoreCase(suffix)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new Exception("上传失败，仅允许上传以下类型文件：图片(jpg/png/gif等)、文档(pdf/doc/xls等)、压缩文件(zip/rar等)、视频(mp4/avi等)、音频(mp3/wav等)");
        }
    }

    /**
     * 通过读取文件头部获得文件类型
     *
     * @param file
     * @return 文件类型
     * @throws Exception
     */

    private static String getFileType(MultipartFile file) throws Exception {
        String fileExtendName = null;
        InputStream is;
        try {
            //is = new FileInputStream(file);
            is = file.getInputStream();
            byte[] b = new byte[10];
            is.read(b, 0, b.length);
            String fileTypeHex = String.valueOf(bytesToHexString(b));
            Iterator<String> keyIter = fileTypeMap.keySet().iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                // 验证前5个字符比较
                if (key.toLowerCase().startsWith(fileTypeHex.toLowerCase().substring(0, 5))
                        || fileTypeHex.toLowerCase().substring(0, 5).startsWith(key.toLowerCase())) {
                    fileExtendName = fileTypeMap.get(key);
                    break;
                }
            }
            // 如果不是上述类型，则判断扩展名
            if (StringUtils.isBlank(fileExtendName)) {
                String fileName = file.getOriginalFilename();
                return getFileTypeBySuffix(fileName);
            }
            is.close();
            return fileExtendName;
        } catch (Exception exception) {
            throw new Exception(exception.getMessage(), exception);
        }
    }

    /**
     * 获得文件头部字符串
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
