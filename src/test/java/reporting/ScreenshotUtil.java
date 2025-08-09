// src/test/java/reporting/ScreenshotUtil.java
package reporting;

import org.openqa.selenium.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    public static String takeScreenshot(WebDriver driver, String testName) {
        try {
            Path dir = Path.of("target", "reports", "screenshots");
            Files.createDirectories(dir);

            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = testName.replaceAll("[^a-zA-Z0-9-_]", "_") + "_" + ts + ".png";
            Path dest = dir.resolve(fileName);

            TakesScreenshot tsDriver = (TakesScreenshot) driver;
            File src = tsDriver.getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), dest);

            return dest.toString();
        } catch (Exception e) {
            // No reventamos el test por un fallo de screenshot
            return null;
        }
    }
}
