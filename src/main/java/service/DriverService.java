package service;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DriverService {
    private static DriverService driverService;

    private DriverService() {
    }

    public static DriverService getInstance() {
        if (driverService == null) {
            driverService = new DriverService();
            return driverService;
        } else {
            return driverService;
        }
    }

    public WebDriver getWebDriver(String pathDriver) {
        boolean isOsLinux = SystemUtils.IS_OS_LINUX;
        boolean isOsWindows = SystemUtils.IS_OS_WINDOWS;
        if (isOsLinux) {
            System.setProperty("webdriver.chrome.driver", pathDriver);
            System.setProperty("webdriver.gecko.driver", pathDriver);

        }

        if (isOsWindows) {
            System.setProperty("webdriver.chrome.driver", pathDriver);
            System.setProperty("webdriver.gecko.driver", pathDriver);
        }
        WebDriver driver = null;

        int driverNumber = 1;
        if (driverNumber == 1) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments(Arrays.asList(
                    "--disable-gpu",
                    "--start-maximized",
                    "--disk-cache-size=1",
                    "--media-cache-size=1",
                    "--disable-gpu-memory-buffer-video-frames",
                    "--disable-gpu-shader-disk-cache",
                    "--disable-flash-stage3d",
                    "--disable-gpu-program-cache",
                    "--disable-gpu-sandbox",
                    "--disable-gpu-shader-disk-cache",
                    "--disable-media-suspend",
                    "--disable-pepper-3d",
                    "--disable-pepper-3d-image-chromium",
                    "--disable-2d-canvas-image-chromium",
                    "--disable-3d-apis",
                    "--disable-audio-support-for-desktop-share",
                    "--disable-boot-animation",
                    "--disable-audio",
                    "--disable-bookmark-autocomplete-provider",
                    "--disable-bundled-ppapi-flash",
                    "--disable-cloud-policy-service",
                    "--disable-desktop-notifications",
                    "--disable-extensions",
                    "--disable-full-history-sync",
                    "--disable-gpu",
                    "--disable-improved-download-protection",
                    "--disable-java",
                    "--disable-media-history",
                    "--disable-media-source",
                    "--disable-ntp-other-sessions-menu",
                    "--disable-pepper-3d",
                    "--disable-plugins",
                    "--disable-popup-blocking",
                    "--disable-print-preview",
                    "--disable-restore-background-contents",
                    "--disable-scripted-print-throttling",
                    "--disable-smooth-scrolling",
                    "--disable-speech-input",
                    "--disable-web-media-player-ms",
                    "--disable-web-security",
                    "--disable-webaudio",
                    "--disable-webgl",
                    "--disable-xss-auditor",
                    "--incognito"));
            HashMap<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("profile.managed_default_content_settings.images", 2);
            options.setExperimentalOption("prefs", prefs);
            DesiredCapabilities chrome = DesiredCapabilities.chrome();
            chrome.setJavascriptEnabled(true);
            chrome.setCapability(ChromeOptions.CAPABILITY, options);
            driver = new ChromeDriver(chrome);
        } else if (driverNumber == 2) {
            driver = new FirefoxDriver();
        }

        driver.manage().timeouts().implicitlyWait(7, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(25, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(15, TimeUnit.SECONDS);
        return driver;
    }
}
