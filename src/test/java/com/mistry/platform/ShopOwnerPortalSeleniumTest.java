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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopOwnerPortalSeleniumTest {

    private static final String BASE = "http://localhost:5173";
    // Ei shop owner admin diye age theke approve kora thakte hobe
    private static final String EMAIL = "selshop@example.com";
    private static final String PASSWORD = "secret123";

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

    private void login() {
        driver.get(BASE + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identifier"))).sendKeys(EMAIL);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/shop-owner"));
    }

    @Test
    void shopOwnerSeesApprovalStatus() {
        login();
        driver.get(BASE + "/shop-owner/profile");
        String badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".status-line .badge"))).getText();
        assertFalse(badge.isEmpty());
    }

    @Test
    void shopOwnerCanEditProfile() {
        login();
        driver.get(BASE + "/shop-owner/profile");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopName"))).clear();
        driver.findElement(By.id("shopName")).sendKeys("Sel Hardware");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-success"))).getText();
        assertTrue(msg.contains("saved"));
    }

    @Test
    void shopOwnerCanAddProduct() {
        login();
        driver.get(BASE + "/shop-owner/products");
        String name = "Hammer" + System.currentTimeMillis();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(name);
        new Select(driver.findElement(By.id("availabilityType"))).selectByValue("SALE");
        driver.findElement(By.id("salePrice")).sendKeys("500");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-success")));
        String table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.data-table"))).getText();
        assertTrue(table.contains(name));
    }

    @Test
    void languageCanSwitchToBengaliAndBack() {
        login();
        driver.get(BASE + "/shop-owner/profile");
        String before = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1"))).getText();
        driver.findElement(By.id("lang-bn")).click();
        wait.until(d -> !d.findElement(By.tagName("h1")).getText().equals(before));
        driver.findElement(By.id("lang-en")).click();
        wait.until(d -> d.findElement(By.tagName("h1")).getText().equals(before));
    }
}