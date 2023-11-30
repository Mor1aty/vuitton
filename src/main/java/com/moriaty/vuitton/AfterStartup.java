package com.moriaty.vuitton;

import com.moriaty.vuitton.bean.view.Module;
import com.moriaty.vuitton.core.storage.MemoryStorage;
import com.moriaty.vuitton.service.novel.downloader.NovelDownloader;
import com.moriaty.vuitton.service.novel.NovelFactory;
import com.moriaty.vuitton.service.view.ModuleFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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
        Map<String, Object> moduleBeanMap
                = applicationContext.getBeansWithAnnotation(com.moriaty.vuitton.core.module.Module.class);
        if (moduleBeanMap.isEmpty()) {
            log.info("无可用模块");
        } else {
            List<Module> moduleList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (Object moduleBean : moduleBeanMap.values()) {
                com.moriaty.vuitton.core.module.Module module = moduleBean.getClass()
                        .getAnnotation(com.moriaty.vuitton.core.module.Module.class);
                moduleList.add(new Module()
                        .setId(module.id())
                        .setName(module.name())
                        .setPath(module.path()));
                sb.append(module.name()).append("(").append(module.path()).append(") ");
            }
            ModuleFactory.addModule(moduleList);
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
                novelDownloaderMap.put(novelDownloaderBean.getInfo().getMark(), novelDownloaderBean);
                sb.append(novelDownloaderBean.getInfo().getWebName())
                        .append("(").append(novelDownloaderBean.getInfo().getMark())
                        .append("[").append(novelDownloaderBean.getInfo().getWebsite()).append("]")
                        .append(") ");
            }
            NovelFactory.putDownloaderMap(novelDownloaderMap);
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
