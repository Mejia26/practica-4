package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest extends BaseTest {

    private WebDriverWait wait;

    @BeforeEach
    public void init() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // --------- Helpers ---------
    private void ensureLoggedOut() {
        try {
            if (!driver.findElements(By.linkText("Logout")).isEmpty()) {
                driver.findElement(By.linkText("Logout")).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Signup / Login")));
            }
        } catch (Exception ignored) {}
    }

    private void goToLogin() {
        driver.findElement(By.linkText("Signup / Login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[data-qa='login-email']")));
    }

    // ✅ Camino feliz
    @Test
    public void loginHappyPath_succeeds() {
        ensureLoggedOut();
        goToLogin();

        driver.findElement(By.cssSelector("input[data-qa='login-email']")).sendKeys("lapampara264@gmail.com");
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("12345678");
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();

        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.linkText("Logout"))).isDisplayed());
    }

    // 🚫 Negativa: contraseña incorrecta
    @Test
    public void loginWithWrongPassword_showsError() {
        ensureLoggedOut();
        goToLogin();

        driver.findElement(By.cssSelector("input[data-qa='login-email']")).sendKeys("lapampara264@gmail.com");
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();

        assertTrue(driver.getPageSource().toLowerCase().contains("your email or password is incorrect"),
                "Debe mostrar mensaje de credenciales incorrectas");
    }

    // ⚠️ Límite: email muy largo
    @Test
    public void loginWithVeryLongEmail_limit() {
        ensureLoggedOut();
        goToLogin();

        String longEmail = "a".repeat(250) + "@mail.com";
        driver.findElement(By.cssSelector("input[data-qa='login-email']")).sendKeys(longEmail);
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("12345678");
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();

        // Esperamos que no loguee y muestre error genérico
        assertTrue(driver.getPageSource().toLowerCase().contains("incorrect"),
                "No debería permitir login con email inválido o excesivo");
    }
}
