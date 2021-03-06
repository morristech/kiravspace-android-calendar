/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.adsense.cmdline;

import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.adsense.Adsense;
import com.google.api.services.adsense.AdsenseRequest;
import com.google.api.services.adsense.model.Accounts;
import com.google.api.services.adsense.model.AdClients;
import com.google.api.services.adsense.model.AdUnits;
import com.google.api.services.adsense.model.CustomChannels;
import com.google.api.services.samples.shared.cmdline.ClientCredentials;
import com.google.api.services.samples.shared.cmdline.oauth2.LocalServerReceiver;
import com.google.api.services.samples.shared.cmdline.oauth2.OAuth2Native;

/**
 * A sample application that runs multiple requests against the AdSense Management API.
 * These include:
 * <ul>
 * <li>Listing all AdSense accounts for a user</li>
 * <li>Listing the sub-account tree for an account</li>
 * <li>Listing all ad clients for an account</li>
 * <li>Listing all ad clients for the default account</li>
 * <li>Listing all ad units for an ad client</li>
 * <li>Listing all custom channels for an ad unit</li>
 * <li>Listing all custom channels for an ad client</li>
 * <li>Listing all ad units for a custom channel</li>
 * <li>Listing all URL channels for an ad client</li>
 * <li>Running a report for an ad client, for the past 7 days</li>
 * <li>Running a paginated report for an ad client, for the past 7 days</li>
 * </ul>
 */
public class AdSenseSample {

  private static final String SCOPE = "https://www.googleapis.com/auth/adsense.readonly";

  // Request parameters.
  private static final int MAX_LIST_PAGE_SIZE = 50;
  private static final int MAX_REPORT_PAGE_SIZE = 50;

  /**
   * Performs all necessary setup steps for running requests against the API.
   * @return An initialized Adsense service object.
   * @throws Exception
   */
  private static Adsense initializeAdsense() throws Exception {
    // Authorization.
    GoogleAccessProtectedResource accessProtectedResource =
        OAuth2Native.authorize(new LocalServerReceiver(), null, "google-chrome", SCOPE);

    // Set up AdSense Management API client.
    Adsense adsense = Adsense.builder(new NetHttpTransport(), new JacksonFactory())
        .setApplicationName("Google-AdSenseSample/1.1")
        .setHttpRequestInitializer(accessProtectedResource)
        .build();

    return adsense;
  }

  /**
   * Runs all the AdSense Management API samples.
   * @param args command-line arguments.
   */
  public static void main(String[] args) {
    try {
      try {
        Adsense adsense = initializeAdsense();

        Accounts accounts = GetAllAccounts.run(adsense, MAX_LIST_PAGE_SIZE);
        if ((accounts.getItems() != null) && !accounts.getItems().isEmpty()) {
          // Get an example account ID, so we can run the following sample.
          String exampleAccountId = accounts.getItems().get(0).getId();
          GetAccountTree.run(adsense, exampleAccountId);
          GetAllAdClientsForAccount.run(adsense, exampleAccountId, MAX_LIST_PAGE_SIZE);
        }

        AdClients adClients = GetAllAdClients.run(adsense, MAX_LIST_PAGE_SIZE);
        if ((adClients.getItems() != null) && !adClients.getItems().isEmpty()) {
          // Get an ad client ID, so we can run the rest of the samples.
          String exampleAdClientId = adClients.getItems().get(0).getId();

          AdUnits units = GetAllAdUnits.run(adsense, exampleAdClientId, MAX_LIST_PAGE_SIZE);
          if ((units.getItems() != null) && !units.getItems().isEmpty()) {
            // Get an example ad unit ID, so we can run the following sample.
            String exampleAdUnitId = units.getItems().get(0).getId();
            GetAllCustomChannelsForAdUnit.run(adsense, exampleAdClientId, exampleAdUnitId,
                MAX_LIST_PAGE_SIZE);
          }

          CustomChannels channels = GetAllCustomChannels.run(adsense, exampleAdClientId,
              MAX_LIST_PAGE_SIZE);
          if ((channels.getItems() != null) && !channels.getItems().isEmpty()) {
            // Get an example custom channel ID, so we can run the following sample.
            String exampleCustomChannelId = channels.getItems().get(0).getId();
            GetAllAdUnitsForCustomChannel.run(adsense, exampleAdClientId, exampleCustomChannelId,
                MAX_LIST_PAGE_SIZE);
          }

          GetAllUrlChannels.run(adsense, exampleAdClientId, MAX_LIST_PAGE_SIZE);
          GenerateReport.run(adsense, exampleAdClientId);
          GenerateReportWithPaging.run(adsense, exampleAdClientId, MAX_REPORT_PAGE_SIZE);
        } else {
          System.out.println("No ad clients found, unable to run remaining methods.");
        }
      } catch (GoogleJsonResponseException e) {
        // Message already includes parsed response.
        System.err.println(e.getMessage());
      } catch (HttpResponseException e) {
        // Message doesn't include parsed response.
        System.err.println(e.getMessage());
        System.err.println(e.getResponse().parseAsString());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
