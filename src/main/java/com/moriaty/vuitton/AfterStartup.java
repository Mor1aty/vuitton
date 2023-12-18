package com.moriaty.vuitton;

import com.moriaty.vuitton.core.module.Module;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.module.novel.downloader.NovelDownloaderFactory;
import com.moriaty.vuitton.core.module.ModuleFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * <p>
 * 启动之后
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:58
 */
@Component
@Slf4j
public class AfterStartup implements CommandLineRunner {
    @Resource
    private ApplicationContext applicationContext;

    @Value("${server.port:#{null}}")
    private String port;

    @Value("${file-server.url}")
    private String fsUrl;


    @Override
    public void run(String... args) {
        loadBaseInfo();
        loadModule();
        loadNovelDownloader();
        loadMemoryStorage();
        loadNetworkSetting();
    }

    private void loadBaseInfo() {
        String portStr;
        if (port == null) {
            portStr = ":8080";
        } else {
            portStr = "80".equals(port) ? "" : ":" + port;
        }
        String ipAddress = findInternalIpAddress();
        String serverUrl = "http://" + ipAddress + portStr;
        log.info("网站地址: http://127.0.0.1{} {}", portStr, serverUrl);
        ServerInfo.BASE_INFO.setServerUrl(serverUrl).setServerHost(ipAddress).setServerPort(port)
                .setFileServerUrl(String.format(fsUrl, ipAddress));
    }

    private String findInternalIpAddress() {
        String fileServerIp = System.getenv("FILE_SERVER_IP");
        if (StringUtils.hasText(fileServerIp) && fileServerIp.startsWith("192.168")) {
            return fileServerIp;
        }
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("wlp2s0");
            for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                String ipAddress = address.getAddress().getHostAddress();
                if (ipAddress.contains("192.168")) {
                    return ipAddress;
                }
            }
        } catch (SocketException e) {
            log.error("获取内部 IP 地址", e);
        }
        return "未知 IP";
    }

    private void loadModule() {
        List<Module> moduleList = ModuleFactory.getAllModule();
        if (moduleList.isEmpty()) {
            log.info("无可用模块");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Module module : moduleList) {
                sb.append(module.getName()).append("(").append(module.getPath()).append(") ");
            }
            log.info("可用的模块[{}]: {}", moduleList.size(), sb);
        }
    }

    private void loadNovelDownloader() {
        Map<String, NovelDownloader> novelDownloaderBeanMap = applicationContext.getBeansOfType(NovelDownloader.class);
        if (novelDownloaderBeanMap.isEmpty()) {
            log.info("无可用小说下载器");
        } else {
            Map<String, NovelDownloader> novelDownloaderMap = HashMap.newHashMap(novelDownloaderBeanMap.size());
            StringBuilder sb = new StringBuilder();
            for (NovelDownloader novelDownloaderBean : novelDownloaderBeanMap.values()) {
                if (Boolean.TRUE.equals(novelDownloaderBean.getInfo().getDisable())) {
                    continue;
                }
                novelDownloaderMap.put(novelDownloaderBean.getInfo().getMark(), novelDownloaderBean);
                sb.append(novelDownloaderBean.getInfo().getWebName())
                        .append("(").append(novelDownloaderBean.getInfo().getMark())
                        .append("[").append(novelDownloaderBean.getInfo().getWebsite()).append("]")
                        .append(") ");
            }
            NovelDownloaderFactory.putDownloaderMap(novelDownloaderMap);
            log.info("可用的小说下载器[{}]: {}", novelDownloaderMap.size(), sb);
        }
    }

    private void loadMemoryStorage() {
        MemoryStorage.initMemoryStorage();
    }

    private void loadNetworkSetting() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Do nothing
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // Do nothing
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("加载网络设置异常", e);
        }
    }
}
