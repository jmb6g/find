package com.autonomy.abc.selenium.find.preview;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class InlinePreview {
    private final WebElement container;
    private final WebDriver driver;

    private InlinePreview(final WebElement container, final WebDriver driver) {
        this.container = container;
        this.driver = driver;
    }

    public boolean loadingIndicatorExists() {
        return !findElements(By.className("view-server-loading-indicator")).isEmpty();
    }

    public WebElement loadingIndicator(){
        return findElement(By.className("view-server-loading-indicator"));
    }

    public DetailedPreviewPage openDetailedPreview(){
        findElement(By.className("preview-mode-open-detail-button")).click();
        return new DetailedPreviewPage.Factory().create(driver);
    }

    private WebElement findElement(final By locator) {
        return container.findElement(locator);
    }

    private List<WebElement> findElements(final By locator) {
        return container.findElements(locator);
    }

    public static InlinePreview make(final WebDriver driver) {
        return new InlinePreview(driver.findElement(By.cssSelector(".preview-mode-wrapper:not(.hide)")), driver);
    }
}
