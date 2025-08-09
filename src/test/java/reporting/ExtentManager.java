package reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.nio.file.Files;
import java.nio.file.Path;

public class ExtentManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            extent = createInstance();
        }
        return extent;
    }

    private static ExtentReports createInstance() {
        try {
            Path outDir = Path.of("target", "reports");
            Files.createDirectories(outDir);
            String reportPath = outDir.resolve("AutomationReport.html").toString();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setDocumentTitle("Reporte de Pruebas");
            spark.config().setReportName("UI Automation - Selenium JUnit5");

            ExtentReports er = new ExtentReports();
            er.attachReporter(spark);
            return er;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el reporte Extent", e);
        }
    }
}
