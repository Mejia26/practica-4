package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckoutTest extends BaseTest {

    private WebDriverWait wait;
    private Actions actions;

    @BeforeEach
    public void initTest() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // un poco más tolerante
        actions = new Actions(driver);
    }

    private void loginIfNeeded() {
        driver.findElement(By.linkText("Signup / Login")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[data-qa='login-email']"))).sendKeys("lapampara264@gmail.com");
        driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("12345678");
        driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
    }

    /** Deja el carrito totalmente vacío */
    private void clearCartIfAny() {
        driver.findElement(By.cssSelector("a[href='/view_cart']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_info")));
        List<WebElement> deletes = driver.findElements(By.cssSelector(".cart_quantity_delete"));
        while (!deletes.isEmpty()) {
            WebElement del = deletes.get(0);
            del.click();
            wait.until(ExpectedConditions.stalenessOf(del));
            deletes = driver.findElements(By.cssSelector(".cart_quantity_delete"));
        }
    }

    private void addProductToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/products']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".features_items")));

        WebElement productCard = driver.findElement(By.cssSelector(".product-image-wrapper"));
        actions.moveToElement(productCard).perform();

        WebElement overlayBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".product-overlay .add-to-cart")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", overlayBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlayBtn);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(),'Added!')]")));
        driver.findElement(By.xpath("//button[text()='Continue Shopping']")).click();
    }

    private void goToCheckout() {
        driver.findElement(By.cssSelector("a[href='/view_cart']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_info")));

        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='Proceed To Checkout']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkoutBtn);

        // Address/Review -> Place Order
        WebElement placeOrder = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='Place Order']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", placeOrder);

        // Ya en /payment
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name_on_card")));
    }

    private void fillPaymentForm(String nameOnCard) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name_on_card"))).sendKeys(nameOnCard);
        driver.findElement(By.name("card_number")).sendKeys("4111111111111111");
        driver.findElement(By.name("cvc")).sendKeys("123");
        driver.findElement(By.name("expiry_month")).sendKeys("12");
        driver.findElement(By.name("expiry_year")).sendKeys("2025");

        WebElement payBtn = driver.findElement(By.id("submit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", payBtn);
    }

    /** Espera robusta de éxito de pedido (varias señales posibles) */
    private void waitForOrderSuccess() {
        By successP = By.xpath("//*[contains(translate(normalize-space(.), " +
                "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'order has been placed') or " +
                "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'placed successfully')]");
        By successH2 = By.xpath("//h2[contains(., 'Order Placed') or contains(., 'Order placed')]");
        By invoiceBtn = By.xpath("//a[contains(., 'Download Invoice')]");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(successP),
                ExpectedConditions.visibilityOfElementLocated(successH2),
                ExpectedConditions.visibilityOfElementLocated(invoiceBtn)
        ));
    }

    // Camino feliz
    @Test
    public void completeCheckoutSuccessfully() {
        loginIfNeeded();
        clearCartIfAny();
        addProductToCart();
        goToCheckout();
        fillPaymentForm("Test User");

        waitForOrderSuccess(); // <- en vez de p.text-center
        assertTrue(true);
    }

    // Negativa
    @Test
    public void checkoutWithoutProductsShouldFail() {
        loginIfNeeded();
        clearCartIfAny();
        driver.findElement(By.cssSelector("a[href='/view_cart']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".container")));

        boolean productPresent = driver.findElements(By.cssSelector("tr[id^='product-']")).size() > 0;
        assertFalse(productPresent, "El carrito debería estar vacío");

        assertTrue(driver.findElements(By.xpath("//a[text()='Proceed To Checkout']")).isEmpty(),
                "No debería mostrarse 'Proceed To Checkout' con carrito vacío");
    }

    // Límite
    @Test
    public void checkoutWithLongNameOnCardShouldWork() {
        loginIfNeeded();
        clearCartIfAny();
        addProductToCart();
        goToCheckout();
        fillPaymentForm("A".repeat(255));

        waitForOrderSuccess(); // <- en vez de p.text-center
        assertTrue(true);
    }
}
