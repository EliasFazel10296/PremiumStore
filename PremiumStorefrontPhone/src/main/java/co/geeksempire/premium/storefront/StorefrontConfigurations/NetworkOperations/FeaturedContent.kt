/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 5/15/21, 3:31 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.StorefrontConfigurations.NetworkOperations

import co.geeksempire.premium.storefront.NetworkConnections.ProductSearchEndpoint
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.Storefront
import co.geeksempire.premium.storefront.Utils.NetworkConnections.Requests.GenericJsonRequest
import co.geeksempire.premium.storefront.Utils.NetworkConnections.Requests.JsonRequestResponses
import org.json.JSONArray

fun Storefront.retrieveFeaturedContent() {

    val productSearchEndpoint: ProductSearchEndpoint = ProductSearchEndpoint(generalEndpoint)

    GenericJsonRequest(applicationContext, object : JsonRequestResponses {

        override fun jsonRequestResponseSuccessHandler(rawDataJsonArray: JSONArray) {
            super.jsonRequestResponseSuccessHandler(rawDataJsonArray)

            storefrontLiveData.processFeaturedContent(rawDataJsonArray)

        }

    }).getMethod(productSearchEndpoint.getFeaturedProductsEndpoint())

}