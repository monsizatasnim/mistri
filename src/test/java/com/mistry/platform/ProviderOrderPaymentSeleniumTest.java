package com.mistry.platform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderOrderPaymentSeleniumTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mistry_db?useSSL=false&allowPublicKeyRetrieval=true";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    // Prottek bar ekta notun unpaid ACCEPTED order database e banay, tai id bodlate hoy na
    private String createFreshOrder() throws Exception {
        String password = System.getenv("DB_PASSWORD") == null ? "orchid" : System.getenv("DB_PASSWORD");
        try (Connection c = DriverManager.getConnection(DB_URL, "root", password);
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO shop_orders (created_at, provider_id, shop_owner_id, status, total_amount, urgent) "
                             + "VALUES (NOW(), 2, 1, 'ACCEPTED', 4500.00, 0)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return String.valueOf(keys.getLong(1));
            }
        }
    }

    @Test
    void providerCanPayForOrder() throws Exception {
        String orderId = createFreshOrder();

        driver.get("http://localhost:5173/provider/login");
        driver.findElement(By.id("email")).sendKeys("cloud@example.com");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.id("login-submit")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("priceMin")));

        driver.get("http://localhost:5173/provider/pay");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("orderId"))).sendKeys(orderId);
        driver.findElement(By.id("load-order")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("order-total")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("method")));
        new Select(driver.findElement(By.id("method"))).selectByValue("BKASH");
        driver.findElement(By.id("pay-now")).click();

        String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pay-success"))).getText();
        assertTrue(msg.contains("PAID"));
        assertEquals("PAID", driver.findElement(By.id("payment-status")).getText());
    }
}