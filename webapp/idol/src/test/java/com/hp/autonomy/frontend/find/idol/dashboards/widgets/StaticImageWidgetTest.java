/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticImageWidgetTest extends ConfigurationComponentTest<StaticImageWidget> {
    @Test(expected = ConfigException.class)
    public void noWidgetSettings() throws ConfigException {
        StaticImageWidget.builder()
                .build()
                .basicValidate(null);
    }

    @Test(expected = ConfigException.class)
    public void noUrl() throws ConfigException {
        StaticImageWidget.builder()
                .widgetSettings(StaticImageWidgetSettings.builder()
                        .build())
                .build()
                .basicValidate(null);
    }

    @Override
    protected Class<StaticImageWidget> getType() {
        return StaticImageWidget.class;
    }

    @Override
    protected StaticImageWidget constructComponent() {
        return StaticImageWidget.builder()
                .name("Test Widget")
                .type("StaticImageWidget")
                .x(1)
                .y(1)
                .width(1)
                .height(1)
                .widgetSettings(StaticImageWidgetSettings.builder()
                        .url("http://placehold.it/800x300")
                        .widgetSetting("testing", "testing")
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(
                getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/dashboards/widgets/staticImageWidget.json")
        );
    }

    @Override
    protected void validateJson(final JsonContent<StaticImageWidget> jsonContent) {
        jsonContent.assertThat()
                .hasJsonPathStringValue("$.name", "Test Widget")
                .hasJsonPathStringValue("$.type", "StaticContentWidget")
                .hasJsonPathNumberValue("$.x", 1)
                .hasJsonPathNumberValue("$.y", 1)
                .hasJsonPathNumberValue("$.width", 1)
                .hasJsonPathNumberValue("$.height", 1)
                .hasJsonPathStringValue("$.widgetSettings.url", "http://placehold.it/800x300")
                .hasJsonPathStringValue("$.widgetSettings.testing", "testing");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<StaticImageWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                StaticImageWidget.builder()
                        .name("Static Image")
                        .type("StaticImageWidget")
                        .x(1)
                        .y(1)
                        .width(2)
                        .height(2)
                        .widgetSettings(StaticImageWidgetSettings.builder()
                                .url("http://placehold.it/800x300")
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<StaticImageWidget> objectContent) {
        objectContent.assertThat().isEqualTo(
                StaticImageWidget.builder()
                        .name("Test Widget")
                        .type("StaticImageWidget")
                        .x(1)
                        .y(1)
                        .width(1)
                        .height(1)
                        .widgetSettings(StaticImageWidgetSettings.builder()
                                .url("http://placehold.it/800x300")
                                .widgetSetting("testing", "testing")
                                .build())
                        .build()
        );
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("name"));
    }
}
