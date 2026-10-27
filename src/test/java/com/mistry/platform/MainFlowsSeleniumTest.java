package com.mistry.platform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Backend (localhost:8080) ar frontend (localhost:5173) cholte hobe.
// Local DB-r test account lagbe: customer 01711111111 / test1234, shop shop1@test.com / shop1234
class MainFlowsSeleniumTest {

    private static final String BASE = "http://localhost:5173";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    private void login(String identifier, String password) {
        driver.get(BASE + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identifier"))).sendKeys(identifier);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Test
    void customerCanLogIn() {
        login("01711111111", "test1234");
        wait.until(ExpectedConditions.urlContains("/customer"));
        assertTrue(driver.getCurrentUrl().contains("/customer"));
    }

    @Test
    void wrongPasswordShowsError() {
        login("01711111111", "wrong-password");
        String msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-error"))).getText();
        assertFalse(msg.isEmpty());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    void customerCanSearchAndBookAProvider() {
        login("01711111111", "test1234");
        wait.until(ExpectedConditions.urlContains("/customer"));

        // search: ranked provider list
        driver.get(BASE + "/customer/providers");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.data-table")));

        // book the first provider in the dropdown
        driver.get(BASE + "/customer/book");
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("book-provider")));
        wait.until(d -> new Select(dropdown).getOptions().size() > 1);
        new Select(dropdown).selectByIndex(1);

        String service = "Selenium test " + System.currentTimeMillis();
        driver.findElement(By.id("book-service")).sendKeys(service);

        // datetime-local is hard to type, so set it with JavaScript and fire the input event
        WebElement time = driver.findElement(By.id("book-time"));
        ((JavascriptExecutor) driver).executeScript(
                "var setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
                        + "setter.call(arguments[0], arguments[1]);"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
                time, "2027-01-15T10:00");

        driver.findElement(By.id("book-submit")).click();
        String done = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form-success"))).getText();
        assertTrue(done.contains("#"));

        // the booking shows in My bookings
        driver.get(BASE + "/customer/bookings");
        String cards = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-card"))).getText();
        assertFalse(cards.isEmpty());
        assertTrue(driver.getPageSource().contains(service));
    }

    @Test
    void shopOwnerCanOpenOrdersAndFilter() {
        login("shop1@test.com", "shop1234");
        wait.until(ExpectedConditions.urlContains("/shop-owner"));

        driver.get(BASE + "/shop-owner/orders");
        WebElement filter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statusFilter")));
        new Select(filter).selectByValue("PLACED");

        // either order cards or the empty message must show, and no error
        wait.until(d -> !d.findElements(By.cssSelector(".order-card")).isEmpty()
                || d.getPageSource().contains("No orders found"));
        assertTrue(driver.findElements(By.cssSelector(".form-error")).isEmpty());
    }
}