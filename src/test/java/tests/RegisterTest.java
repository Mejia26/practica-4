package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTest extends BaseTest {

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

    private void goToSignupLogin() {
        driver.findElement(By.linkText("Signup / Login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[data-qa='login-email']")));
    }

    private void deleteAccountIfVisible() {
        try {
            if (!driver.findElements(By.linkText("Delete Account")).isEmpty()) {
                WebElement del = driver.findElement(By.linkText("Delete Account"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", del);
                WebElement deleted = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h2[@data-qa='account-deleted']")));
                assertTrue(deleted.isDisplayed());
                driver.findElement(By.xpath("//a[@data-qa='continue-button']")).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Signup / Login")));
            }
        } catch (Exception ignored) {}
    }

    private void fillMandatoryRegistrationForm(String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(password);
        // Campos mínimos obligatorios
        driver.findElement(By.id("first_name")).sendKeys("Test");
        driver.findElement(By.id("last_name")).sendKeys("User");
        driver.findElement(By.id("address1")).sendKeys("123 Fake St");
        driver.findElement(By.id("state")).sendKeys("State");
        driver.findElement(By.id("city")).sendKeys("City");
        driver.findElement(By.id("zipcode")).sendKeys("12345");
        driver.findElement(By.id("mobile_number")).sendKeys("8091234567");
        driver.findElement(By.xpath("//button[text()='Create Account']")).click();
    }

    /** Click en Continue con manejo de anuncios/ventanas y reintento */
    private void clickContinueAndHandleAd() {
        String main = driver.getWindowHandle();

        WebElement cont = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@data-qa='continue-button']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cont);

        // Si se abrió ventana de anuncio, ciérrala
        for (int i = 0; i < 3; i++) {
            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                for (String h : handles) {
                    if (!h.equals(main)) {
                        driver.switchTo().window(h);
                        try { driver.close(); } catch (Exception ignored) {}
                    }
                }
                driver.switchTo().window(main);
            }

            // Si aún sigue el botón Continue, reintenta el click
            if (!driver.findElements(By.xpath("//a[@data-qa='continue-button']")).isEmpty()) {
                WebElement cont2 = driver.findElement(By.xpath("//a[@data-qa='continue-button']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cont2);
            } else {
                break;
            }

            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }

        // Espera por Logout o "Logged in as"
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(.,'Logged in as')]"))
        ));
    }

    // Camino feliz
    @Test
    public void registerHappyPath_createsAccountSuccessfully() {
        ensureLoggedOut();
        driver.findElement(By.linkText("Signup / Login")).click();

        String name = "User" + UUID.randomUUID().toString().substring(0,6);
        String email = "test_" + UUID.randomUUID().toString().substring(0,8) + "@mail.com";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys(name);
        driver.findElement(By.cssSelector("input[data-qa='signup-email']")).sendKeys(email);
        driver.findElement(By.xpath("//button[text()='Signup']")).click();

        fillMandatoryRegistrationForm("123456");

        // Ver “ACCOUNT CREATED!”
        WebElement created = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[@data-qa='account-created']")));
        assertTrue(created.isDisplayed());

        // Continue con manejo de popups
        clickContinueAndHandleAd();

        // Limpieza
        deleteAccountIfVisible();
    }

    // Negativa: email existente
    @Test
    public void registerWithExistingEmail_showsError() {
        ensureLoggedOut();
        goToSignupLogin();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys("AnyUser");
        driver.findElement(By.cssSelector("input[data-qa='signup-email']")).sendKeys("lapampara264@gmail.com");
        driver.findElement(By.xpath("//button[text()='Signup']")).click();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Email Address already exist')]")));
        assertTrue(err.isDisplayed());
    }

    // Límite: nombre de 255 chars
    @Test
    public void registerWithLongName_limit255() {
        ensureLoggedOut();
        driver.findElement(By.linkText("Signup / Login")).click();

        String longName = "A".repeat(255);
        String email = "test_" + UUID.randomUUID().toString().substring(0,8) + "@mail.com";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name"))).sendKeys(longName);
        driver.findElement(By.cssSelector("input[data-qa='signup-email']")).sendKeys(email);
        driver.findElement(By.xpath("//button[text()='Signup']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))); // form cargó
        fillMandatoryRegistrationForm("123456");

        WebElement created = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[@data-qa='account-created']")));
        assertTrue(created.isDisplayed());

        // Continue con manejo de popups/ventanas y OR de condiciones
        clickContinueAndHandleAd();

        // Limpieza
        deleteAccountIfVisible();
    }
}
