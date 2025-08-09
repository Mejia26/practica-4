//package tests;
//
//import com.aventstack.extentreports.ExtentReports;
//import com.aventstack.extentreports.ExtentTest;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.openqa.selenium.*;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.openqa.selenium.chrome.ChromeDriver;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.StandardCopyOption;
//import java.time.LocalDateTime;
//
//public class BaseTest {
//
//    protected WebDriver driver;
//    protected static ExtentReports report;
//    protected ExtentTest test;
//
//    @BeforeEach
//    public void setup() {
//        WebDriverManager.chromedriver().setup();
//        driver = new ChromeDriver();
//        driver.manage().window().maximize();
//        driver.get("https://automationexercise.com");
//
//        if (report == null) {
//            report = TestReportManager.getInstance();
//        }
//    }
//
//    @AfterEach
//    public void tearDown() {
//        if (driver != null) {
//            try {
//                takeScreenshot("Final");
//            } catch (Exception ignored) {}
//            driver.quit();
//        }
//        if (report != null) {
//            report.flush();
//        }
//    }
//
//    public void takeScreenshot(String name) throws IOException {
//        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//        String filename = "test-output/screenshots/" + name + "_" + LocalDateTime.now().toString().replace(":", "-") + ".png";
//        File dest = new File(filename);
//        dest.getParentFile().mkdirs();
//        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//    }
//}







// src/test/java/tests/BaseTest.java
package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import reporting.ExtentReportListener;

import java.time.Duration;

@ExtendWith(ExtentReportListener.class)
public abstract class BaseTest {

    protected WebDriver driver;
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    /** Permite al listener acceder al driver para tomar screenshots */
    public static WebDriver getDriver() {
        return TL_DRIVER.get();
    }

    /** Cambia esto si prefieres inyectar por variable de entorno o properties */
    protected String baseUrl = "https://automationexercise.com";

    @BeforeEach
    public void setUp() {
        // Si usas WebDriverManager, asegúrate de tener la dependencia y descomentar:
        // io.github.bonigarcia:webdrivermanager
        // WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Sugerencias para reducir interferencias/overlays
        options.addArguments(
                "--start-maximized",
                "--disable-notifications",
                "--disable-infobars",
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        );
        // Algunas versiones de Chrome requieren esto si hay errores de CORS:
        // options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        TL_DRIVER.set(driver);

        // Tiempo de pageLoad más razonable (tests manejan waits explícitos)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // usamos waits explícitos

        driver.get(baseUrl);
    }

    @AfterEach
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            TL_DRIVER.remove();
        }
    }
}
