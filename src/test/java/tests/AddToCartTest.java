package tests;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AddToCartTest extends BaseTest {

    /**
     * ✅ Camino feliz: Agrega producto exitosamente al carrito
     */
    @Test
    public void addProductToCartSuccessfully() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        Actions actions = new Actions(driver);

        login(wait);
        goToProducts(wait);

        WebElement productCard = driver.findElement(By.cssSelector(".product-image-wrapper"));
        actions.moveToElement(productCard).perform();

        WebElement overlayBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".product-overlay .add-to-cart")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlayBtn);

        WebElement addedMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h4[contains(text(),'Added!')]")));
        assertTrue(addedMsg.isDisplayed());
    }

    /**
     * 🚫 Prueba negativa: Intentar agregar producto sin iniciar sesión
     */
    @Test
    public void addProductWithoutLoginShouldStillWork() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        Actions actions = new Actions(driver);

        goToProducts(wait);

        WebElement productCard = driver.findElement(By.cssSelector(".product-image-wrapper"));
        actions.moveToElement(productCard).perform();

        WebElement overlayBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".product-overlay .add-to-cart")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlayBtn);

        WebElement addedMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h4[contains(text(),'Added!')]")));
        assertTrue(addedMsg.isDisplayed());
    }

    /**
     * ⚠️ Prueba de límite: Agregar el mismo producto 10 veces y verificar si se refleja en el carrito
     */
    @Test
    public void addProductMultipleTimesLimitTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        Actions actions = new Actions(driver);

        login(wait);
        goToProducts(wait);

        for (int i = 0; i < 10; i++) {
            WebElement productCard = driver.findElement(By.cssSelector(".product-image-wrapper"));
            actions.moveToElement(productCard).perform();

            WebElement overlayBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".product-overlay .add-to-cart")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlayBtn);

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h4[contains(text(),'Added!')]")));
            driver.findElement(By.xpath("//button[text()='Continue Shopping']")).click();
        }

        driver.findElement(By.cssSelector("a[href='/view_cart']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_info")));

//        WebElement quantity = driver.findElement(By.cssSelector("button.disabled"));
//        assertTrue(quantity.getText().equals("10"));
        WebElement quantityElement = driver.findElement(By.cssSelector("td.cart_quantity > button.disabled"));
        String quantity = quantityElement.getText();
        assertEquals("11", quantity); // 11 porque arribe se agrego uno
    }

    // Helpers
    private void login(WebDriverWait wait) {
        driver.findElement(By.linkText("Signup / Login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[data-qa='login-email']"))).sendKeys("lapampara264@gmail.com");
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("12345678");
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
    }

    private void goToProducts(WebDriverWait wait) {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/products']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".features_items")));
    }
}