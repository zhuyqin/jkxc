package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.jeecg.common.util.filter.FileTypeFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * FTP文件上传工具类
 */
@Slf4j
public class FtpUtil {

    /**
     * 上传文件到FTP服务器（带重试机制）
     * @param file 文件
     * @param bizPath 业务路径
     * @param ftpHost FTP服务器地址
     * @param ftpPort FTP端口
     * @param ftpUsername FTP用户名
     * @param ftpPassword FTP密码
     * @param basePath FTP基础路径
     * @return 相对路径（如：order/文件名.jpg）
     */
    public static String upload(MultipartFile file, String bizPath, 
                                String ftpHost, int ftpPort, 
                                String ftpUsername, String ftpPassword, 
                                String basePath) throws Exception {
        // 重试机制：最多重试3次
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            FTPClient ftpClient = new FTPClient();
            InputStream inputStream = null;
            
            try {
                // 文件类型验证（只在第一次验证）
                if (retryCount == 0) {
                    FileTypeFilter.fileTypeFilter(file);
                }
                
                // 设置超时时间（增加超时时间以提高稳定性）
                // 连接超时30秒，数据超时60秒，默认超时60秒
                ftpClient.setConnectTimeout(30000);
                ftpClient.setDataTimeout(60000);
                ftpClient.setDefaultTimeout(60000);
                
                // 连接FTP服务器
                if (retryCount > 0) {
                    log.info("FTP连接重试，第{}次尝试连接服务器 {}:{}", retryCount + 1, ftpHost, ftpPort);
                    // 重试前等待一段时间
                    Thread.sleep(1000 * retryCount);
                }
                ftpClient.connect(ftpHost, ftpPort);
                
                // 检查连接是否成功
                int replyCode = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    ftpClient.disconnect();
                    throw new Exception("FTP服务器连接失败，返回码：" + replyCode);
                }
                
                // 连接成功后设置KeepAlive，保持连接活跃
                try {
                    ftpClient.setKeepAlive(true);
                } catch (Exception e) {
                    log.warn("设置FTP KeepAlive失败，继续执行：{}", e.getMessage());
                }
                
                // 登录
                boolean loginSuccess = ftpClient.login(ftpUsername, ftpPassword);
                if (!loginSuccess) {
                    ftpClient.disconnect();
                    throw new Exception("FTP登录失败，用户名或密码错误");
                }
                
                // 设置文件传输模式为二进制
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                // 设置被动模式（PASV模式，适合大多数防火墙环境）
                ftpClient.enterLocalPassiveMode();
                // 设置编码
                ftpClient.setControlEncoding("UTF-8");
                // 设置缓冲区大小，提高传输效率
                ftpClient.setBufferSize(1024 * 1024); // 1MB
                
                // 先切换到FTP用户根目录（登录后默认就在根目录）
                ftpClient.changeWorkingDirectory("/");
                
                // 切换到基础路径（basePath）
                if (oConvertUtils.isNotEmpty(basePath)) {
                    // 移除开头的斜杠
                    String cleanBasePath = basePath.startsWith("/") ? basePath.substring(1) : basePath;
                    if (oConvertUtils.isNotEmpty(cleanBasePath)) {
                        // 切换到basePath，如果不存在则创建
                        if (!ftpClient.changeWorkingDirectory(cleanBasePath)) {
                            createDirectory(ftpClient, cleanBasePath);
                            if (!ftpClient.changeWorkingDirectory(cleanBasePath)) {
                                throw new Exception("无法切换到FTP基础目录：" + cleanBasePath);
                            }
                        }
                    }
                }
                
                // 在basePath下创建并切换到业务路径（bizPath）
                if (oConvertUtils.isNotEmpty(bizPath)) {
                    // 直接在当前目录（basePath）下创建bizPath目录
                    if (!ftpClient.changeWorkingDirectory(bizPath)) {
                        // 目录不存在，创建单个目录（不是多级路径）
                        boolean created = ftpClient.makeDirectory(bizPath);
                        if (created) {
                            if (!ftpClient.changeWorkingDirectory(bizPath)) {
                                throw new Exception("创建业务目录成功但无法切换：" + bizPath);
                            }
                        } else {
                            // 创建失败，可能是目录已存在（并发情况），再次尝试切换
                            if (!ftpClient.changeWorkingDirectory(bizPath)) {
                                String replyString = ftpClient.getReplyString();
                                throw new Exception("创建FTP业务目录失败：" + bizPath + "，服务器返回：" + replyString);
                            }
                        }
                    }
                }
                
                // 生成文件名
                String orgName = file.getOriginalFilename();
                orgName = CommonUtils.getFileName(orgName);
                String fileName;
                if (orgName.indexOf(".") != -1) {
                    fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" 
                              + System.currentTimeMillis() 
                              + orgName.substring(orgName.lastIndexOf("."));
                } else {
                    fileName = orgName + "_" + System.currentTimeMillis();
                }
                
                // 上传文件
                inputStream = file.getInputStream();
                boolean success = ftpClient.storeFile(fileName, inputStream);
                
                if (success) {
                    // 返回相对路径
                    String dbpath;
                    if (oConvertUtils.isNotEmpty(bizPath)) {
                        dbpath = bizPath + "/" + fileName;
                    } else {
                        dbpath = fileName;
                    }
                    if (retryCount > 0) {
                        log.info("FTP上传成功（重试{}次后）", retryCount);
                    }
                    return dbpath;
                } else {
                    String errorMsg = ftpClient.getReplyString();
                    log.error("FTP文件上传失败：{}，服务器返回：{}", fileName, errorMsg);
                    throw new Exception("FTP文件上传失败：" + (oConvertUtils.isNotEmpty(errorMsg) ? errorMsg : "未知错误"));
                }
                
            } catch (java.net.ConnectException e) {
                lastException = e;
                log.warn("FTP连接失败（尝试{}/{}）：{}:{}，错误：{}", retryCount + 1, maxRetries, ftpHost, ftpPort, e.getMessage());
                if (retryCount >= maxRetries - 1) {
                    log.error("FTP连接失败，已重试{}次：{}:{}", maxRetries, ftpHost, ftpPort);
                    throw new Exception("无法连接到FTP服务器 " + ftpHost + ":" + ftpPort + "（已重试" + maxRetries + "次），请检查网络连接和服务器配置");
                }
            } catch (java.net.SocketTimeoutException e) {
                lastException = e;
                log.warn("FTP连接超时（尝试{}/{}）：{}:{}，错误：{}", retryCount + 1, maxRetries, ftpHost, ftpPort, e.getMessage());
                if (retryCount >= maxRetries - 1) {
                    log.error("FTP连接超时，已重试{}次：{}:{}", maxRetries, ftpHost, ftpPort);
                    throw new Exception("FTP连接超时（已重试" + maxRetries + "次），请检查网络连接和服务器状态");
                }
            } catch (java.io.IOException e) {
                // IO异常也可能是网络问题，进行重试
                lastException = e;
                log.warn("FTP IO异常（尝试{}/{}）：{}:{}，错误：{}", retryCount + 1, maxRetries, ftpHost, ftpPort, e.getMessage());
                if (retryCount >= maxRetries - 1) {
                    log.error("FTP IO异常，已重试{}次：{}", maxRetries, e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                // 其他异常（如登录失败、目录创建失败等）不重试
                log.error("FTP上传异常：" + e.getMessage(), e);
                throw e;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (ftpClient.isConnected()) {
                        try {
                            ftpClient.logout();
                        } catch (Exception e) {
                            log.warn("FTP登出异常：" + e.getMessage());
                        }
                        try {
                            ftpClient.disconnect();
                        } catch (Exception e) {
                            log.warn("FTP断开连接异常：" + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("关闭FTP连接异常：" + e.getMessage(), e);
                }
            }
            
            retryCount++;
        }
        
        // 如果所有重试都失败，抛出最后一次异常
        if (lastException != null) {
            throw new Exception("FTP上传失败，已重试" + maxRetries + "次：" + lastException.getMessage(), lastException);
        } else {
            throw new Exception("FTP上传失败，已重试" + maxRetries + "次");
        }
    }
    
    /**
     * 创建FTP目录（支持多级目录）
     * 从当前工作目录开始创建
     */
    private static void createDirectory(FTPClient ftpClient, String remotePath) throws Exception {
        // 移除开头的斜杠
        String path = remotePath.startsWith("/") ? remotePath.substring(1) : remotePath;
        if (oConvertUtils.isEmpty(path)) {
            return;
        }
        
        String[] dirs = path.split("/");
        String currentPath = "";
        
        for (String dir : dirs) {
            if (oConvertUtils.isEmpty(dir)) {
                continue;
            }
            
            // 构建当前路径
            if (oConvertUtils.isEmpty(currentPath)) {
                currentPath = dir;
            } else {
                currentPath = currentPath + "/" + dir;
            }
            
            // 尝试切换目录，如果不存在则创建
            if (!ftpClient.changeWorkingDirectory(currentPath)) {
                boolean created = ftpClient.makeDirectory(currentPath);
                if (created) {
                    // 创建成功后切换到该目录
                    if (!ftpClient.changeWorkingDirectory(currentPath)) {
                        String replyString = ftpClient.getReplyString();
                        throw new Exception("创建目录成功但无法切换：" + currentPath + "，服务器返回：" + replyString);
                    }
                } else {
                    // 创建失败，可能是目录已存在（并发情况），再次尝试切换
                    if (!ftpClient.changeWorkingDirectory(currentPath)) {
                        String replyString = ftpClient.getReplyString();
                        throw new Exception("创建FTP目录失败：" + currentPath + "，服务器返回：" + replyString);
                    }
                }
            }
        }
    }
    
    /**
     * 从FTP服务器下载文件到输出流
     * @param filePath 文件相对路径（如：order/文件名.jpg）
     * @param basePath FTP基础路径
     * @param outputStream 输出流
     * @param ftpHost FTP服务器地址
     * @param ftpPort FTP端口
     * @param ftpUsername FTP用户名
     * @param ftpPassword FTP密码
     */
    public static void download(String filePath, String basePath, OutputStream outputStream,
                                String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) throws Exception {
        FTPClient ftpClient = new FTPClient();
        InputStream inputStream = null;
        
        try {
            // 设置超时时间
            ftpClient.setConnectTimeout(10000);
            ftpClient.setDataTimeout(30000);
            ftpClient.setDefaultTimeout(30000);
            
            // 连接FTP服务器
            ftpClient.connect(ftpHost, ftpPort);
            
            // 检查连接是否成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                throw new Exception("FTP服务器连接失败，返回码：" + replyCode);
            }
            
            // 登录
            boolean loginSuccess = ftpClient.login(ftpUsername, ftpPassword);
            if (!loginSuccess) {
                ftpClient.disconnect();
                throw new Exception("FTP登录失败，用户名或密码错误");
            }
            
            // 设置文件传输模式为二进制
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置编码
            ftpClient.setControlEncoding("UTF-8");
            
            // 切换到基础路径
            if (oConvertUtils.isNotEmpty(basePath)) {
                String cleanBasePath = basePath.startsWith("/") ? basePath.substring(1) : basePath;
                if (oConvertUtils.isNotEmpty(cleanBasePath)) {
                    if (!ftpClient.changeWorkingDirectory(cleanBasePath)) {
                        throw new Exception("无法切换到FTP基础目录：" + cleanBasePath);
                    }
                }
            }
            
            // 解析文件路径，获取目录和文件名
            String dirPath = "";
            String fileName = filePath;
            int lastSlash = filePath.lastIndexOf("/");
            if (lastSlash > 0) {
                dirPath = filePath.substring(0, lastSlash);
                fileName = filePath.substring(lastSlash + 1);
            }
            
            // 切换到文件所在目录
            if (oConvertUtils.isNotEmpty(dirPath)) {
                if (!ftpClient.changeWorkingDirectory(dirPath)) {
                    throw new Exception("文件所在目录不存在：" + dirPath);
                }
            }
            
            // 下载文件
            inputStream = ftpClient.retrieveFileStream(fileName);
            if (inputStream == null) {
                String replyString = ftpClient.getReplyString();
                throw new Exception("无法下载文件：" + filePath + "，服务器返回：" + replyString);
            }
            
            // 将文件内容写入输出流
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            
            // 完成文件传输
            ftpClient.completePendingCommand();
            
        } catch (Exception e) {
            log.error("FTP下载文件异常：" + e.getMessage(), e);
            throw e;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭FTP连接异常：" + e.getMessage(), e);
            }
        }
    }
}

