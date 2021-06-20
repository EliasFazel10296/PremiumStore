/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 6/20/21, 8:34 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface

import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import co.geeksempire.premium.storefront.AccountManager.SignInProcess.AccountData
import co.geeksempire.premium.storefront.AccountManager.SignInProcess.AccountSignIn
import co.geeksempire.premium.storefront.AccountManager.SignInProcess.SignInInterface
import co.geeksempire.premium.storefront.Actions.Operation.ActionCenterOperations
import co.geeksempire.premium.storefront.Actions.View.PrepareActionCenterUserInterface
import co.geeksempire.premium.storefront.Database.Preferences.Theme.ThemePreferences
import co.geeksempire.premium.storefront.FavoriteProductsConfigurations.IO.FavoriteProductQueryInterface
import co.geeksempire.premium.storefront.FavoriteProductsConfigurations.IO.FavoritedProcess
import co.geeksempire.premium.storefront.NetworkConnections.GeneralEndpoint
import co.geeksempire.premium.storefront.PremiumStorefrontApplication
import co.geeksempire.premium.storefront.ProductsDetailsConfigurations.UserInterface.ProductDetailsFragment
import co.geeksempire.premium.storefront.R
import co.geeksempire.premium.storefront.StorefrontConfigurations.ContentFiltering.Filter.FilterAllContent
import co.geeksempire.premium.storefront.StorefrontConfigurations.ContentFiltering.Filter.FilteringOptions
import co.geeksempire.premium.storefront.StorefrontConfigurations.ContentFiltering.FilterAdapter.FilterOptionsAdapter
import co.geeksempire.premium.storefront.StorefrontConfigurations.DataStructure.StorefrontContentsData
import co.geeksempire.premium.storefront.StorefrontConfigurations.DataStructure.StorefrontLiveData
import co.geeksempire.premium.storefront.StorefrontConfigurations.Extensions.setupStorefrontUserInterface
import co.geeksempire.premium.storefront.StorefrontConfigurations.Extensions.userInteractionSetup
import co.geeksempire.premium.storefront.StorefrontConfigurations.NetworkOperations.*
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.AllContent.Adapter.AllContentAdapter
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.CategoryContent.Adapter.CategoriesAdapter
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.FeaturedContent.Adapter.FeaturedContentAdapter
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.NewContent.Adapter.NewContentAdapter
import co.geeksempire.premium.storefront.Utils.InApplicationUpdate.InApplicationUpdateProcess
import co.geeksempire.premium.storefront.Utils.InApplicationUpdate.UpdateResponse
import co.geeksempire.premium.storefront.Utils.NetworkConnections.NetworkCheckpoint
import co.geeksempire.premium.storefront.Utils.NetworkConnections.NetworkConnectionListener
import co.geeksempire.premium.storefront.Utils.NetworkConnections.NetworkConnectionListenerInterface
import co.geeksempire.premium.storefront.Utils.Notifications.SnackbarActionHandlerInterface
import co.geeksempire.premium.storefront.Utils.Notifications.SnackbarBuilder
import co.geeksempire.premium.storefront.Utils.PopupShortcuts.PopupShortcutsCreator
import co.geeksempire.premium.storefront.Utils.UI.Display.columnCount
import co.geeksempire.premium.storefront.Utils.UI.Display.displayY
import co.geeksempire.premium.storefront.Utils.UI.SmoothScrollers.RecycleViewSmoothLayoutGrid
import co.geeksempire.premium.storefront.Utils.UI.SmoothScrollers.RecycleViewSmoothLayoutList
import co.geeksempire.premium.storefront.Utils.UI.Views.Fragment.FragmentInterface
import co.geeksempire.premium.storefront.databinding.StorefrontLayoutBinding
import com.abanabsalan.aban.magazine.Utils.System.hideKeyboard
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.storefront_layout.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.geeksempire.balloon.optionsmenu.library.BalloonOptionsMenu
import net.geeksempire.balloon.optionsmenu.library.Utils.dpToInteger

class Storefront : AppCompatActivity(), NetworkConnectionListenerInterface, SignInInterface, FragmentInterface {

    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(this@Storefront)
    }

    val generalEndpoint: GeneralEndpoint = GeneralEndpoint()

    val storefrontLiveData: StorefrontLiveData by lazy {
        ViewModelProvider(this@Storefront).get(StorefrontLiveData::class.java)
    }

    val allContent: AllContent by lazy {
        AllContent(applicationContext, storefrontLiveData)
    }

    val prepareActionCenterUserInterface: PrepareActionCenterUserInterface by lazy {
        PrepareActionCenterUserInterface(context = applicationContext, actionCenterView = storefrontLayoutBinding.actionCenterView, actionLeftView = storefrontLayoutBinding.leftActionView, actionMiddleView = storefrontLayoutBinding.middleActionView, actionRightView = storefrontLayoutBinding.rightActionView)
    }

    val balloonOptionsMenu: BalloonOptionsMenu by lazy {
        BalloonOptionsMenu(context = this@Storefront,
            rootView = storefrontLayoutBinding.rootView)
    }

    val actionCenterOperations: ActionCenterOperations by lazy {
        ActionCenterOperations(this@Storefront)
    }

    val filterAllContent: FilterAllContent by lazy {
        FilterAllContent(storefrontLiveData)
    }

    val featuredContentAdapter: FeaturedContentAdapter by lazy {
        FeaturedContentAdapter(this@Storefront)
    }
    val allContentAdapter: AllContentAdapter by lazy {
        AllContentAdapter(this@Storefront)
    }
    val newContentAdapter: NewContentAdapter by lazy {
        NewContentAdapter(this@Storefront)
    }
    val categoriesAdapter: CategoriesAdapter by lazy {
        CategoriesAdapter(this@Storefront, filterAllContent)
    }

    val favoritedProcess: FavoritedProcess by lazy {
        FavoritedProcess(this@Storefront)
    }

    val networkCheckpoint: NetworkCheckpoint by lazy {
        NetworkCheckpoint(applicationContext)
    }

    private val networkConnectionListener: NetworkConnectionListener by lazy {
        NetworkConnectionListener(this@Storefront, storefrontLayoutBinding.rootView, networkCheckpoint)
    }

    val productDetailsFragment: ProductDetailsFragment by lazy {
        ProductDetailsFragment()
    }

    val filterOptionsAdapter: FilterOptionsAdapter by lazy {
        FilterOptionsAdapter(this@Storefront, FilteringOptions.FilterByCountry)
    }

    val storefrontAllUntouchedContents: ArrayList<StorefrontContentsData> = ArrayList<StorefrontContentsData>()
    val storefrontAllUnfilteredContents: ArrayList<StorefrontContentsData> = ArrayList<StorefrontContentsData>()

    /* Start - Sign In */
    val accountSignIn: AccountSignIn by lazy {
        AccountSignIn(this@Storefront, this@Storefront)
    }

    val accountSelector: ActivityResultLauncher<Any?> = registerForActivityResult(accountSignIn.createProcess()) {



    }
    /* End - Sign In */

    val firebaseAuthentication = Firebase.auth
    var firebaseUser = firebaseAuthentication.currentUser

    lateinit var storefrontLayoutBinding: StorefrontLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storefrontLayoutBinding = StorefrontLayoutBinding.inflate(layoutInflater)
        setContentView(storefrontLayoutBinding.root)

        networkConnectionListener.networkConnectionListenerInterface = this@Storefront

        setupStorefrontUserInterface()

        storefrontLayoutBinding.root.post {

            storefrontLayoutBinding.loadingView.visibility = View.VISIBLE

            userInteractionSetup()

            lifecycleScope.launch {

                themePreferences.checkThemeLightDark().collect {

                    actionCenterOperations.setupForStorefront(it)

                }

            }

            storefrontLayoutBinding.featuredContentRecyclerView.layoutManager = RecycleViewSmoothLayoutList(applicationContext, RecyclerView.HORIZONTAL, false)
            storefrontLayoutBinding.featuredContentRecyclerView.adapter = featuredContentAdapter

            storefrontLayoutBinding.allContentRecyclerView.layoutManager = RecycleViewSmoothLayoutGrid(applicationContext, columnCount(applicationContext, 307), RecyclerView.VERTICAL,false)
            storefrontLayoutBinding.allContentRecyclerView.adapter = allContentAdapter

            storefrontLayoutBinding.newContentRecyclerView.layoutManager = RecycleViewSmoothLayoutList(applicationContext, RecyclerView.HORIZONTAL, false)
            storefrontLayoutBinding.newContentRecyclerView.adapter = newContentAdapter

            storefrontLayoutBinding.categoriesRecyclerView.layoutManager = RecycleViewSmoothLayoutList(applicationContext, RecyclerView.VERTICAL, false)
            storefrontLayoutBinding.categoriesRecyclerView.adapter = categoriesAdapter

            storefrontLiveData.allContentItemData.observe(this@Storefront, {

                if (it.isNotEmpty()) {

                    storefrontLayoutBinding.loadingView.visibility = View.GONE

                    storefrontAllUntouchedContents.clear()
                    storefrontAllUntouchedContents.addAll(it)

                    storefrontAllUnfilteredContents.clear()
                    storefrontAllUnfilteredContents.addAll(it)

                    allContentAdapter.storefrontContents.clear()
                    allContentAdapter.storefrontContents.addAll(it)

                    allContentAdapter.notifyDataSetChanged()

                    storefrontLayoutBinding.allContentRecyclerView.visibility = View.VISIBLE

                    retrieveCategories()

                    storefrontLiveData.checkInstalledApplications(applicationContext, allContentAdapter, it)

                } else {



                }

            })

            storefrontLiveData.allContentMoreItemData.observe(this@Storefront, {
                Log.d(this@Storefront.javaClass.simpleName, "More Products Data Loaded")

                storefrontAllUntouchedContents.addAll(it)

                storefrontAllUnfilteredContents.addAll(it)

                if (allContent.allLoadingFinished && allContentAdapter.storefrontContents.isNotEmpty()) {

                    storefrontLayoutBinding.loadMoreView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    storefrontLayoutBinding.loadMoreView.visibility = View.VISIBLE

                }

            })

            storefrontLiveData.presentMoreItemData.observe(this@Storefront, {

                allContentAdapter.storefrontContents.add(it)

                allContentAdapter.notifyItemInserted(allContentAdapter.storefrontContents.size - 1)

                storefrontLayoutBinding.loadMoreView.apply {

                    speed = 1f
                    setMinAndMaxFrame(130, 165)

                    if (!isAnimating) {
                        playAnimation()
                    }

                }

                if (allContentAdapter.storefrontContents.size == storefrontAllUntouchedContents.size) {

                    storefrontLayoutBinding.loadMoreView.visibility = View.GONE

                }

            })

            storefrontLiveData.allFilteredContentItemData.observe(this@Storefront, {

                if (it.isNotEmpty()) {

                    allContentAdapter.storefrontContents.clear()
                    allContentAdapter.storefrontContents.addAll(it)

                    allContentAdapter.notifyDataSetChanged()

                    storefrontAllUnfilteredContents.clear()
                    storefrontAllUnfilteredContents.addAll(storefrontAllUntouchedContents)

                } else {



                }

            })

            storefrontLiveData.featuredContentItemData.observe(this@Storefront, {

                if (it.isNotEmpty()) {

                    val numberOfItemsToLoad = displayY(applicationContext) / (dpToInteger(applicationContext, 307))
                    Log.d(this@Storefront.javaClass.simpleName, "Number Of Items To Load | Featured Content: ${numberOfItemsToLoad}")

                    val dataToSetup = it.subList(0, numberOfItemsToLoad)

                    featuredContentAdapter.storefrontContents.clear()
                    featuredContentAdapter.storefrontContents.addAll(it)

                    featuredContentAdapter.notifyDataSetChanged()

                    storefrontLayoutBinding.featuredContentRecyclerView.visibility = View.VISIBLE

                    PopupShortcutsCreator(applicationContext)
                        .configure(it.subList(0, 5).toList())

                } else {



                }

            })

            storefrontLiveData.newContentItemData.observe(this@Storefront, {

                if (it.isNotEmpty()) {

                    newContentAdapter.storefrontContents.clear()
                    newContentAdapter.storefrontContents.addAll(it)

                    newContentAdapter.notifyDataSetChanged()

                    storefrontLayoutBinding.newContentRecyclerView.visibility = View.VISIBLE

                } else {



                }

            })

            storefrontLiveData.categoriesItemData.observe(this@Storefront, {

                if (it.isNotEmpty()) {

                    categoriesAdapter.storefrontCategories.clear()
                    categoriesAdapter.storefrontCategories.addAll(it)

                    categoriesAdapter.notifyDataSetChanged()

                    storefrontLayoutBinding.categoriesRecyclerView.visibility = View.VISIBLE

                    storefrontLayoutBinding.categoryIndicatorTextView.visibility = View.VISIBLE

                    (application as PremiumStorefrontApplication).categoryData.clearData()
                    (application as PremiumStorefrontApplication).categoryData.prepareAllCategoriesData(it)

                } else {



                }

            })

            storefrontLayoutBinding.nestedScrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->

                if (scrollY > oldScrollY) {
                    Log.d(this@Storefront.javaClass.simpleName, "Scrolling Down")

                    balloonOptionsMenu.removeBalloonOption()

                } else if (scrollY < oldScrollY) {
                    Log.d(this@Storefront.javaClass.simpleName, "Scrolling Up")

                    balloonOptionsMenu.removeBalloonOption()

                }

            }

            storefrontLayoutBinding.categoriesRecyclerView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->

                if (scrollY > oldScrollY) {
                    Log.d(this@Storefront.javaClass.simpleName, "Scrolling Down")

                    balloonOptionsMenu.removeBalloonOption()

                } else if (scrollY < oldScrollY) {
                    Log.d(this@Storefront.javaClass.simpleName, "Scrolling Up")

                    balloonOptionsMenu.removeBalloonOption()

                }

            }

            storefrontLayoutBinding.loadMoreView.setOnClickListener {

                storefrontLiveData.loadMoreDataIntoPresenter(storefrontAllUntouchedContents, allContentAdapter.storefrontContents)

                storefrontLayoutBinding.loadMoreView.apply {

                    speed = 1f
                    setMinAndMaxFrame(1, 130)

                    if (!isAnimating) {
                        playAnimation()
                    }

                }

            }

        }

    }

    override fun onStart() {
        super.onStart()

        InApplicationUpdateProcess(this@Storefront, storefrontLayoutBinding.rootView)
            .initialize(object : UpdateResponse {

                override fun newUpdateAvailable() {
                    super.newUpdateAvailable()



                }

                override fun latestVersionAlreadyInstalled() {
                    super.latestVersionAlreadyInstalled()



                }

            })

    }

    override fun onResume() {
        super.onResume()

        accountSignIn.firebaseUser?.let {

            Glide.with(applicationContext)
                .load(it.photoUrl)
                .transform(CircleCrop())
                .into(storefrontLayoutBinding.profileView)

            favoritedProcess.isFavoriteProductsExist(accountSignIn.firebaseUser!!.uid,
                object : FavoriteProductQueryInterface {

                    override fun favoriteProductsExist(isFavoriteProductsExist: Boolean) {
                        super.favoriteProductsExist(isFavoriteProductsExist)

                        storefrontLayoutBinding.favoritesView.visibility = if (isFavoriteProductsExist) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    }

                })

        }

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {

        if (productDetailsFragment.isShowing) {

            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .remove(productDetailsFragment)
                .commitNow()

        } else {

            startActivity(Intent(Intent.ACTION_MAIN).apply {
                this.addCategory(Intent.CATEGORY_HOME)
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())

        }

    }

    override fun networkAvailable() {
        Log.d(this@Storefront.javaClass.simpleName, "Network Available @ ${this@Storefront.javaClass.simpleName}")

        retrieveFeaturedContent()

        allContent.retrieveAllContent()

        retrieveNewContent()

    }

    override fun networkLost() {
        Log.d(this@Storefront.javaClass.simpleName, "No Network @ ${this@Storefront.javaClass.simpleName}")

        hideKeyboard(applicationContext, storefrontLayoutBinding.searchView)

    }

    override fun userCreated(accountData: AccountData) {
        super.userCreated(accountData)

        val messageText = "Your Details On www.GeeksEmpire.co \n" +
                "Username: ${accountData.usernameId} | Password: ${accountData.userPassword}"

        SnackbarBuilder(applicationContext).show (
            rootView = storefrontLayoutBinding.rootView,
            messageText = messageText,
            messageDuration = Snackbar.LENGTH_INDEFINITE,
            actionButtonText = R.string.copyText,
            snackbarActionHandlerInterface = object : SnackbarActionHandlerInterface {

                override fun onActionButtonClicked(snackbar: Snackbar) {
                    super.onActionButtonClicked(snackbar)

                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    val clipData = ClipData.newPlainText("Geeks Empire Account Data", messageText)

                    clipboardManager.setPrimaryClip(clipData)

                    snackbar.dismiss()

                    Toast.makeText(applicationContext, "Copied!", Toast.LENGTH_LONG).show()

                }

            }
        )

    }

    override fun signInProcessSucceed(authenticationResult: AuthResult) {
        super.signInProcessSucceed(authenticationResult)

        firebaseUser = authenticationResult.user
        firebaseUser?.reload()

        Glide.with(applicationContext)
            .load(authenticationResult.user?.photoUrl)
            .transform(CircleCrop())
            .into(storefrontLayoutBinding.profileView)

        favoritedProcess.isFavoriteProductsExist(accountSignIn.firebaseUser!!.uid,
            object : FavoriteProductQueryInterface {

                override fun favoriteProductsExist(isFavoriteProductsExist: Boolean) {
                    super.favoriteProductsExist(isFavoriteProductsExist)

                    storefrontLayoutBinding.favoritesView.visibility = if (isFavoriteProductsExist) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                }

            })

    }

    override fun fragmentCreated(applicationPackageName: String, applicationName: String, applicationSummary: String) {
        super.fragmentCreated(applicationPackageName, applicationName, applicationSummary)

        actionCenterOperations.setupForProductDetails(
            applicationPackageName = applicationPackageName?:packageName,
            applicationName = applicationName?:getString(R.string.applicationName),
            applicationSummary = applicationSummary?:getString(R.string.applicationSummary))

        lifecycleScope.launch {
            themePreferences.checkThemeLightDark().collect {
                prepareActionCenterUserInterface.setupIconsForDetails(it)
            }
        }

    }

    override fun fragmentDestroyed() {
        super.fragmentDestroyed()

        lifecycleScope.launch {

            themePreferences.checkThemeLightDark().collect {

                prepareActionCenterUserInterface.setupIconsForStorefront(it)

                actionCenterOperations.setupForStorefront(it)

            }

        }

        if (!storefrontLayoutBinding.favoritesView.isShown) {

            accountSignIn.firebaseUser?.let {

                Glide.with(applicationContext)
                    .load(it.photoUrl)
                    .transform(CircleCrop())
                    .into(storefrontLayoutBinding.profileView)

                favoritedProcess.isFavoriteProductsExist(accountSignIn.firebaseUser!!.uid,
                    object : FavoriteProductQueryInterface {

                        override fun favoriteProductsExist(isFavoriteProductsExist: Boolean) {
                            super.favoriteProductsExist(isFavoriteProductsExist)

                            storefrontLayoutBinding.favoritesView.visibility = if (isFavoriteProductsExist) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }

                        }

                    })

            }

        }

    }

}