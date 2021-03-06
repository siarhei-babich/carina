/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.appium.status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.openqa.selenium.support.ui.FluentWait;

import com.qaprosoft.carina.core.foundation.utils.JsonUtils;
import com.qaprosoft.carina.core.foundation.webdriver.appium.status.model.Status;

public final class AppiumStatus {

    private static final Logger LOGGER = Logger.getLogger(AppiumStatus.class);

    private static final String APPIUM_STATUS_PATH = "/status";

    private AppiumStatus() {
    }

    public static boolean isStarted(String appiumServer) {

        LOGGER.debug("Checking Appium is stared on " + appiumServer);

        String response = null;

        try {
            response = Request.Get(appiumServer + APPIUM_STATUS_PATH)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute().returnContent().asString();
        } catch (IOException e) {
            return false;
        }

        Status status = null;
        try {

            if (response == null) {
                LOGGER.debug("Cannot determine if Appium session started - got NULL response from Appium server");
                return false;
            }
            status = JsonUtils.fromJson(response, Status.class);
            if (status == null) {
                LOGGER.debug("Appium session on url " + appiumServer + " is not started yet");
                return false;
            }

        } catch (Exception e) {
            LOGGER.error("Cannot get correct Appium status", e);
        }

        return status.getStatus() >= 0;
    }

    public static void waitStartup(final String appiumHost, long waitTimeoutSeconds) {

        new FluentWait<>(appiumHost).withTimeout(waitTimeoutSeconds, TimeUnit.SECONDS).withMessage("Appium '" + appiumHost +
                "' isn't started during " + waitTimeoutSeconds + "seconds. ")
                .until(host -> AppiumStatus.isStarted(host));

        LOGGER.debug("Appium started!");
    }
}
