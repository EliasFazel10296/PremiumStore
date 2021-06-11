/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 6/11/21, 10:27 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.ProductsDetailsConfigurations.UserInterface

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import co.geeksempire.premium.storefront.BuiltInBrowserConfigurations.BuiltInBrowser
import co.geeksempire.premium.storefront.Database.Preferences.Theme.ThemePreferences
import co.geeksempire.premium.storefront.Database.Preferences.Theme.ThemeType
import co.geeksempire.premium.storefront.FavoriteProductsConfigurations.Extensions.startFavoriteProcess
import co.geeksempire.premium.storefront.FavoriteProductsConfigurations.IO.FavoritedProcess
import co.geeksempire.premium.storefront.ProductsDetailsConfigurations.YoutubeConfigurations.SetupYoutubePlayer
import co.geeksempire.premium.storefront.R
import co.geeksempire.premium.storefront.StorefrontConfigurations.DataStructure.ProductDataKey
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.Storefront
import co.geeksempire.premium.storefront.Utils.NetworkConnections.NetworkCheckpoint
import co.geeksempire.premium.storefront.Utils.UI.Colors.extractDominantColor
import co.geeksempire.premium.storefront.Utils.UI.Colors.extractVibrantColor
import co.geeksempire.premium.storefront.Utils.UI.Colors.setColorAlpha
import co.geeksempire.premium.storefront.databinding.ProductDetailsLayoutBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProductDetailsFragment : Fragment() {

    var storefrontInstance: Storefront? = null

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(requireActivity() as AppCompatActivity)
    }

    val networkCheckpoint: NetworkCheckpoint by lazy {
        NetworkCheckpoint(requireContext())
    }

    val favoritedProcess: FavoritedProcess by lazy {
        FavoritedProcess(requireActivity() as AppCompatActivity)
    }

    var isShowing = false

    lateinit var productDetailsLayoutBinding: ProductDetailsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            storefrontInstance?.themePreferences?.checkThemeLightDark()?.collect {
                (activity as Storefront).prepareActionCenterUserInterface.setupIconsForDetails(it)
            }
        }

    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        productDetailsLayoutBinding = ProductDetailsLayoutBinding.inflate(layoutInflater)

        return productDetailsLayoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {

            themePreferences.checkThemeLightDark().collect {

                applyShadowEffectsForContentBackground(it)

                applyNegativeSpaceEffectsForFavorite(it)

            }

        }

        arguments?.apply {

            val productId = getString(ProductDataKey.ProductId)
            val applicationPackageName = getString(ProductDataKey.ProductPackageName)

            getString(ProductDataKey.ProductIcon)?.let { productIcon ->

                Glide.with(requireContext())
                    .asDrawable()
                    .load(productIcon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(CircleCrop())
                    .listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(exception: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {



                            return true
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                            resource?.let {

                                val dominantColor = extractDominantColor(requireContext(), resource)
                                val vibrantColor = extractVibrantColor(requireContext(), resource)

                                requireActivity().runOnUiThread {

                                    lifecycleScope.launch {

                                        themePreferences.checkThemeLightDark().collect {

                                            applyGlowingEffectsForRatingBackground(themeType = it, glowingColor = vibrantColor)

                                            if (getString(ProductDataKey.ProductCoverImage) == null) {

                                                productDetailsLayoutBinding.applicationFeaturedImageBlurView.visibility = View.VISIBLE

                                                productDetailsLayoutBinding.applicationFeaturedImageBlurView.setOverlayColor(when (it) {
                                                    ThemeType.ThemeLight -> {
                                                        requireContext().getColor(R.color.light_transparent)
                                                    }
                                                    ThemeType.ThemeDark -> {
                                                        requireContext().getColor(R.color.dark_transparent)
                                                    }
                                                    else -> requireContext().getColor(R.color.light_transparent)
                                                })

                                                productDetailsLayoutBinding.applicationIconBlurView.setSecondOverlayColor(when (it) {
                                                    ThemeType.ThemeLight -> {
                                                        requireContext().getColor(R.color.light_transparent_high)
                                                    }
                                                    ThemeType.ThemeDark -> {
                                                        requireContext().getColor(R.color.dark_transparent_high)
                                                    }
                                                    else -> requireContext().getColor(R.color.light_transparent_high)
                                                })

                                                val gradientFeaturedBackground = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(dominantColor, vibrantColor))

                                                productDetailsLayoutBinding.applicationFeaturedImageView.background = gradientFeaturedBackground

                                                Glide.with(requireContext())
                                                    .load(resource)
                                                    .transform(CenterCrop())
                                                    .into(productDetailsLayoutBinding.applicationFeaturedImageView)

                                            } else {

                                                getString(ProductDataKey.ProductCoverImage)?.let { productCoverImage ->

                                                    Glide.with(requireContext())
                                                        .asDrawable()
                                                        .load(productCoverImage)
                                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                        .into(productDetailsLayoutBinding.applicationFeaturedImageView)

                                                }

                                            }

                                        }

                                    }

                                    productDetailsLayoutBinding.applicationRatingTextView.setTextColor(vibrantColor)
                                    productDetailsLayoutBinding.applicationRatingTextView.setShadowLayer(productDetailsLayoutBinding.applicationRatingTextView.shadowRadius, productDetailsLayoutBinding.applicationRatingTextView.shadowDx, productDetailsLayoutBinding.applicationRatingTextView.shadowDy, vibrantColor)

                                    productDetailsLayoutBinding.applicationIconBlurView.setOverlayColor(setColorAlpha(dominantColor, 222f))

                                    productDetailsLayoutBinding.applicationIconImageView.setImageDrawable(resource)

                                }

                            }

                            return true
                        }

                    })
                    .submit()

            }

            val productName = getString(ProductDataKey.ProductName)?.let { productName ->

                productDetailsLayoutBinding.applicationNameTextView.text = Html.fromHtml(productName, Html.FROM_HTML_MODE_COMPACT)

                productName
            }

            val productSummary = getString(ProductDataKey.ProductSummary)

            val productDescription = getString(ProductDataKey.ProductDescription)?.let { productDescription ->

                productDetailsLayoutBinding.applicationDescriptionTextView.text = Html.fromHtml(productDescription, Html.FROM_HTML_MODE_COMPACT)

                productDescription
            }

            val applicationYoutubeIntroduction = getString(ProductDataKey.ProductYoutubeIntroduction)?.let { applicationYoutubeIntroduction ->

                productDetailsLayoutBinding.applicationYoutubeView.visibility = View.VISIBLE
                productDetailsLayoutBinding.playYoutubeView.visibility = View.VISIBLE

                productDetailsLayoutBinding.playYoutubeView.setOnClickListener {

                    BuiltInBrowser.show(
                        context = requireContext(),
                        linkToLoad = applicationYoutubeIntroduction,
                        gradientColorOne = null,
                        gradientColorTwo = null
                    )

                }

                SetupYoutubePlayer(productDetailsLayoutBinding.applicationYoutubeView)
                    .initialize(applicationYoutubeIntroduction)

                applicationYoutubeIntroduction
            }

            storefrontInstance?.actionCenterOperations?.setupForProductDetails(
                applicationPackageName = applicationPackageName?:requireContext().packageName,
                applicationName = productName?:requireContext().getString(R.string.applicationName),
                applicationSummary = productSummary?:requireContext().getString(R.string.applicationSummary))

            productDetailsLayoutBinding.favoriteView.setOnClickListener {

                productId?.let { idOfProduct -> startFavoriteProcess(idOfProduct) }

            }

            productDetailsLayoutBinding.goBackView.setOnClickListener {

                storefrontInstance?.apply {

                    Handler(Looper.getMainLooper()).postDelayed({

                        supportFragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .remove(productDetailsFragment)
                            .commit()

                        storefrontLayoutBinding.contentDetailsContainer.visibility = View.GONE

                        prepareActionCenterUserInterface.setupIconsForStorefront()

                    }, 357)

                }

            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        isShowing = false

    }

}