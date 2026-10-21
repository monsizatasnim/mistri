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

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderOrderPaymentSeleniumTest {

    // Ekta unpaid, ACCEPTED order er id boshao
    private static final String ORDER_ID = "3";

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

    @Test
    void providerCanPayForOrder() {
        driver.get("http://localhost:5173/provider/login");
        driver.findElement(By.id("email")).sendKeys("cloud@example.com");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.id("login-submit")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("priceMin")));

        driver.get("http://localhost:5173/provider/pay");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("orderId"))).sendKeys(ORDER_ID);
        driver.findElement(By.id("load-order")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("order-total")));
        new Select(driver.findElement(By.id("method"))).selectByValue("BKASH");
        driver.findElement(By.id("pay-now")).click();

        String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pay-success"))).getText();
        assertTrue(msg.contains("PAID"));
        assertTrue(driver.findElement(By.id("payment-status")).getText().equals("PAID"));
    }
}