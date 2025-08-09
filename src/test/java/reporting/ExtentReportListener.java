// src/test/java/reporting/ExtentReportListener.java
package reporting;

import com.aventstack.extentreports.*;
//import com.aventstack.extentreports.media.entity.MediaEntityBuilder;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.WebDriver;
import tests.BaseTest; // <-- usa tu BaseTest

public class ExtentReportListener implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback, TestWatcher {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> current = new ThreadLocal<>();

    @Override
    public void beforeAll(ExtensionContext context) {
        extent = ExtentManager.getInstance();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        String displayName = context.getDisplayName();
        ExtentTest test = extent.createTest(displayName);
        current.set(test);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        WebDriver driver = BaseTest.getDriver(); // <-- necesitamos este getter estático
        String path = ScreenshotUtil.takeScreenshot(driver, context.getRequiredTestMethod().getName() + "_PASS");
        if (path != null) {
            current.get().pass("OK",
                    MediaEntityBuilder.createScreenCaptureFromPath(path).build());
        } else {
            current.get().pass("OK (sin screenshot)");
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        WebDriver driver = BaseTest.getDriver();
        String path = ScreenshotUtil.takeScreenshot(driver, context.getRequiredTestMethod().getName() + "_FAIL");
        if (path != null) {
            current.get().fail(cause,
                    MediaEntityBuilder.createScreenCaptureFromPath(path).build());
        } else {
            current.get().fail(cause);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // nada extra
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (extent != null) extent.flush();
    }
}
