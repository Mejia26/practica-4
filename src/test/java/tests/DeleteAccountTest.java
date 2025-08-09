package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteAccountTest extends BaseTest {

    private WebDriverWait wait;

    @BeforeEach
    public void init() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // -------------------- Helpers --------------------

    private void ensureLoggedOut() {
        try {
            WebElement logout = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Logout")));
            if (logout.isDisplayed()) {
                logout.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Signup / Login")));
            }
        } catch (TimeoutException | NoSuchElementException ignored) {
            // No estaba logueado
        }
    }

    private String[] registerTempUser() {
        String name = "User" + UUID.randomUUID().toString().substring(0, 6);
        String email = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com";
        String password = "P@ssw0rd";

        // Ir a signup/login
        driver.findElement(By.linkText("Signup / Login")).click();

        // Paso 1: formulario inicial
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys(name);
        driver.findElement(By.cssSelector("input[data-qa='signup-email']")).sendKeys(email);
        driver.findElement(By.xpath("//button[text()='Signup']")).click();

        // Paso 2: formulario largo (mínimos obligatorios)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(password);
        driver.findElement(By.id("first_name")).sendKeys("Test");
        driver.findElement(By.id("last_name")).sendKeys("User");
        driver.findElement(By.id("address1")).sendKeys("123 Fake St");
        driver.findElement(By.id("state")).sendKeys("State");
        driver.findElement(By.id("city")).sendKeys("City");
        driver.findElement(By.id("zipcode")).sendKeys("12345");
        driver.findElement(By.id("mobile_number")).sendKeys("8091234567");
        driver.findElement(By.xpath("//button[text()='Create Account']")).click();

        // Confirmación de creación
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[@data-qa='account-created']"))).isDisplayed());

        // Continuar → ya queda logueado
        driver.findElement(By.xpath("//a[@data-qa='continue-button']")).click();

        // Señal de login correcto
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Delete Account")));

        return new String[]{email, password};
    }

    private void login(String email, String password) {
        driver.findElement(By.linkText("Signup / Login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[data-qa='login-email']"))).sendKeys(email);
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Delete Account")));
    }

    private void deleteAccount() {
        WebElement del = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Delete Account")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", del);

        // Ver "ACCOUNT DELETED!"
        WebElement deleted = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[@data-qa='account-deleted']")));
        assertTrue(deleted.isDisplayed());

        // Continuar a home
        driver.findElement(By.xpath("//a[@data-qa='continue-button']")).click();

        // Ya no debería estar logueado
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Signup / Login")));
    }

    // -------------------- Tests --------------------

    /**
     * ✅ Camino feliz: crear cuenta temporal y eliminarla.
     */
    @Test
    public void deleteAccountHappyPath() {
        ensureLoggedOut();
        registerTempUser();
        deleteAccount();

        // Verificar que ya no exista el enlace de "Delete Account"
        assertTrue(driver.findElements(By.linkText("Delete Account")).isEmpty());
    }

    /**
     * 🚫 Negativa: sin login no debe existir la opción de eliminar cuenta.
     */
    @Test
    public void deleteAccountWithoutLoginShouldNotBeVisible() {
        ensureLoggedOut();
        // En home sin login, no hay "Delete Account"
        assertTrue(driver.findElements(By.linkText("Delete Account")).isEmpty());

        // Intento forzado: ir a /delete_account (si existiera) debe redirigir o no mostrar opción.
        driver.get("https://automationexercise.com");
        assertTrue(driver.findElement(By.linkText("Signup / Login")).isDisplayed(),
                "Debería requerir autenticación para eliminar cuenta");
    }

    /**
     * ⚠️ Límite: eliminar y luego intentar usar la cuenta de nuevo (no debería permitir login).
     */
    @Test
    public void deleteAccountThenLoginAgainShouldFail() {
        ensureLoggedOut();
        String[] creds = registerTempUser(); // crea y queda logueado
        deleteAccount();                     // elimina

        // Intento de login con credenciales eliminadas
        driver.findElement(By.linkText("Signup / Login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[data-qa='login-email']")))
                .sendKeys(creds[0]);
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys(creds[1]);
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();

        // Mensaje de error esperado
        assertTrue(driver.getPageSource().toLowerCase().contains("your email or password is incorrect"),
                "No debería permitir login con una cuenta eliminada");
    }
}
