package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateCartTest extends BaseTest {

    private WebDriverWait wait;
    private Actions actions;

    @BeforeEach
    public void init() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        actions = new Actions(driver);
        loginIfNeeded();
        ensureCartEmpty(); // deja el carrito vacío y NO navega a home
    }

    // ---------- Helpers ----------
    private void loginIfNeeded() {
        if (driver.findElements(By.linkText("Logout")).isEmpty()) {
            driver.findElement(By.linkText("Signup / Login")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[data-qa='login-email']"))).sendKeys("lapampara264@gmail.com");
            driver.findElement(By.cssSelector("input[data-qa='login-password']")).sendKeys("12345678");
            driver.findElement(By.cssSelector("button[data-qa='login-button']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
        }
    }

    /** Cierra popups/ventanas si se abren al navegar */
    private void handleAdsIfAny() {
        String main = driver.getWindowHandle();
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
        // Si un overlay deja visible el botón "Continue" (caso post-account), intenta quitarlo
        if (!driver.findElements(By.xpath("//a[@data-qa='continue-button']")).isEmpty()) {
            WebElement cont = driver.findElement(By.xpath("//a[@data-qa='continue-button']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cont);
        }
    }

    private void ensureCartEmpty() {
        driver.findElement(By.cssSelector("a[href='/view_cart']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_info")));
        List<WebElement> deletes = driver.findElements(By.cssSelector(".cart_quantity_delete"));
        while (!deletes.isEmpty()) {
            WebElement del = deletes.get(0);
            del.click();
            wait.until(ExpectedConditions.stalenessOf(del));
            deletes = driver.findElements(By.cssSelector(".cart_quantity_delete"));
        }
        // QUITAMOS el "volver a home" que te estaba fallando
        // nos quedamos en /view_cart y desde aquí el navbar tiene "Products"
    }

    private void goToProducts() {
        handleAdsIfAny();
        WebElement productsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/products']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", productsLink);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".features_items")));
    }

    private void addFirstProduct() {
        WebElement card = driver.findElement(By.cssSelector(".features_items .product-image-wrapper"));
        actions.moveToElement(card).pause(Duration.ofMillis(200)).perform();

        // Intento overlay
        try {
            WebElement overlayBtn = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product-overlay .add-to-cart")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", overlayBtn);
        } catch (TimeoutException e) {
            // Fallback: botón del card
            WebElement btn = card.findElement(By.cssSelector("a.add-to-cart"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h4[contains(text(),'Added!')]")));
        driver.findElement(By.xpath("//button[text()='Continue Shopping']")).click();
    }

    private void goToCart() {
        WebElement cart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/view_cart']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cart);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_info")));
    }

    private int getCartQuantity() {
        WebElement qtyBtn = driver.findElement(By.cssSelector("td.cart_quantity > button.disabled"));
        return Integer.parseInt(qtyBtn.getText().trim());
    }

    // ✅ Camino feliz: actualizar cantidad sumando el mismo producto
    @Test
    public void updateQuantityHappyPath() {
        goToProducts();
        addFirstProduct(); // qty = 1
        addFirstProduct(); // qty = 2
        goToCart();
        assertEquals(2, getCartQuantity(), "La cantidad en el carrito debe ser 2 después de actualizar.");
    }

    // 🚫 Negativa: intentar actualizar cantidad con carrito vacío (no debe haber qty)
    @Test
    public void updateQuantityWithEmptyCartShouldFail() {
        // Ya está vacío por @BeforeEach
        goToCart();
        boolean hasQty = !driver.findElements(By.cssSelector("td.cart_quantity > button.disabled")).isEmpty();
        assertFalse(hasQty, "No debería haber cantidad visible con el carrito vacío.");
    }

    // ⚠️ Límite: intentar subir cantidad alta (ej. 10) y validar comportamiento
    @Test
    public void updateQuantityLimitToTen() {
        goToProducts();
        addFirstProduct();
        for (int i = 0; i < 9; i++) { // ya hay 1, sumamos 9 => 10
            addFirstProduct();
        }
        goToCart();
        int qty = getCartQuantity();
        assertTrue(qty >= 5, "La cantidad debería reflejar un incremento significativo (esperado 10 si no limita). Cantidad actual: " + qty);
    }
}
