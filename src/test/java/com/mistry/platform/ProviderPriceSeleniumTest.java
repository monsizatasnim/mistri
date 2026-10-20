package com.mistry.platform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderPriceSeleniumTest {

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
    void providerCanSetPriceRange() {
        driver.get("http://localhost:5173/provider/login");
        driver.findElement(By.id("email")).sendKeys("cloud@example.com");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.id("login-submit")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("priceMin")));
        driver.findElement(By.id("priceMin")).sendKeys("500");
        driver.findElement(By.id("priceMax")).sendKeys("2000");
        driver.findElement(By.id("priceNote")).sendKeys("Depends on the work");
        driver.findElement(By.id("save-price")).click();

        String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("price-success"))).getText();
        assertTrue(msg.contains("saved"));
    }

    @Test
    void maxBelowMinShowsError() {
        driver.get("http://localhost:5173/provider/login");
        driver.findElement(By.id("email")).sendKeys("cloud@example.com");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.id("login-submit")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("priceMin")));
        driver.findElement(By.id("priceMin")).sendKeys("1000");
        driver.findElement(By.id("priceMax")).sendKeys("500");
        driver.findElement(By.id("save-price")).click();

        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("price-error"))).getText().contains("Maximum"));
    }
}