package me.siebe.flux.core.system;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

public class OsInfoProvider implements SystemInfoProvider {
    @Override
    public StartupBannerSection provide() {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("OS", getOSNameAndArch());
        properties.put("Hostname", getHostName());
        return new StartupBannerSection("Operating System", properties);
    }

    private String getOSNameAndArch() {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        if (osName == null) osName = "Unknown";
        if (osArch == null) osArch = "Unknown";
        return osName + " (" + osArch + ")";
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
