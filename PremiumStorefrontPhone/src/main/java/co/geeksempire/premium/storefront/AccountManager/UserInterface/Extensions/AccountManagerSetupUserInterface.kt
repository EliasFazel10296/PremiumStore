/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 6/7/21, 9:21 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.AccountManager.UserInterface.Extensions

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import co.geeksempire.premium.storefront.AccountManager.DataStructure.AccountDataStructure
import co.geeksempire.premium.storefront.AccountManager.DataStructure.UserInformationProfileData
import co.geeksempire.premium.storefront.AccountManager.UserInterface.AccountInformation
import co.geeksempire.premium.storefront.BuildConfig
import co.geeksempire.premium.storefront.Invitations.Send.SendInvitation
import co.geeksempire.premium.storefront.PremiumStorefrontApplication
import co.geeksempire.premium.storefront.R
import co.geeksempire.premium.storefront.Utils.UI.Colors.extractDominantColor
import co.geeksempire.premium.storefront.Utils.UI.Colors.extractVibrantColor
import co.geeksempire.premium.storefront.Utils.UI.Colors.isColorDark
import co.geeksempire.premium.storefront.Utils.UI.Display.statusBarHeight
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

fun AccountInformation.accountManagerSetupUserInterface() {

    accountInformationLayoutBinding.profileBlurView.setOverlayColor(getColor(R.color.light_blurry_color))

    accountInformationLayoutBinding.welcomeTextView.setTextColor(getColor(R.color.dark))

    accountInformationLayoutBinding.instagramAddressView.setTextColor(getColor(R.color.dark))
    accountInformationLayoutBinding.instagramAddressLayout.boxBackgroundColor = (getColor(R.color.white))

    accountInformationLayoutBinding.twitterAddressView.setTextColor(getColor(R.color.dark))
    accountInformationLayoutBinding.twitterAddressLayout.boxBackgroundColor = (getColor(R.color.white))

    accountInformationLayoutBinding.phoneNumberAddressView.setTextColor(getColor(R.color.dark))
    accountInformationLayoutBinding.phoneNumberAddressLayout.boxBackgroundColor = (getColor(R.color.white))

    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.navigationBarColor = Color.TRANSPARENT
    window.statusBarColor = Color.TRANSPARENT

    accountInformationLayoutBinding.welcomeTextView.text = getString(R.string.welcomeText, Firebase.auth.currentUser?.displayName?:"")

    var dominantColor = getColor(R.color.yellow)
    var vibrantColor = getColor(R.color.default_color_light)

    window.setBackgroundDrawable(GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, arrayOf(vibrantColor, dominantColor).toIntArray()))

    val accountViewLayoutParameters: ConstraintLayout.LayoutParams = accountInformationLayoutBinding.profileImageView.layoutParams as ConstraintLayout.LayoutParams
    accountViewLayoutParameters.setMargins(accountViewLayoutParameters.topMargin, accountViewLayoutParameters.topMargin + statusBarHeight(applicationContext), accountViewLayoutParameters.topMargin, accountViewLayoutParameters.topMargin)
    accountInformationLayoutBinding.profileImageView.layoutParams = accountViewLayoutParameters

    MoreOptions(this@accountManagerSetupUserInterface)
        .setup()

    Firebase.auth.currentUser?.let { firebaseUser ->

        Glide.with(this@accountManagerSetupUserInterface)
            .asDrawable()
            .load(firebaseUser.photoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(glideException: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {

                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                    runOnUiThread {

                        accountInformationLayoutBinding.profileImageView.setImageDrawable(resource)

                        resource?.let {

                            dominantColor = extractDominantColor(applicationContext, it)
                            vibrantColor = extractVibrantColor(applicationContext, it)

                            window.setBackgroundDrawable(GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, arrayOf(vibrantColor, dominantColor).toIntArray()))

                            if (isColorDark(dominantColor) && isColorDark(vibrantColor)) {
                                Log.d(this@accountManagerSetupUserInterface.javaClass.simpleName, "Dark Extracted Colors")

                            } else {
                                Log.d(this@accountManagerSetupUserInterface.javaClass.simpleName, "Light Extracted Colors")

                            }

                        }

                    }

                    return false
                }

            })
            .submit()

        clickSetup()

    }

}

fun AccountInformation.clickSetup() {

    accountInformationLayoutBinding.submitProfileView.setOnClickListener {

        if (profileUpdating) {

            profileUpdating = false

            this@clickSetup.finish()

        } else {

            profileUpdating = true

            createUserProfile(true)

        }

    }

}

fun AccountInformation.createUserProfile(profileUpdatingProcess: Boolean = false) = CoroutineScope(SupervisorJob() + Dispatchers.Main).async {

    Firebase.auth.currentUser?.let { firebaseUser ->

        accountInformationLayoutBinding.updatingLoadingView.visibility = View.VISIBLE
        accountInformationLayoutBinding.updatingLoadingView.playAnimation()

        accountInformationLayoutBinding.welcomeTextView.text = getString(R.string.welcomeText, Firebase.auth.currentUser?.displayName)

        val userInformationProfileData = UserInformationProfileData(
                privacyAgreement = userInformationIO.readPrivacyAgreement().first(),
                userIdentification = firebaseUser.uid,
                userEmailAddress = firebaseUser.email.toString(),
                userDisplayName = firebaseUser.displayName.toString(),
                userProfileImage = firebaseUser.photoUrl.toString(),
                instagramAccount = accountInformationLayoutBinding.instagramAddressView.text.toString().lowercase(),
                twitterAccount = accountInformationLayoutBinding.twitterAddressView.text.toString(),
                phoneNumber = accountInformationLayoutBinding.phoneNumberAddressView.text.toString(),
                isBetaUser = BuildConfig.VERSION_NAME.uppercase(Locale.getDefault()).contains("Beta".uppercase())
            )

        (application as PremiumStorefrontApplication).firestoreDatabase
            .document(accountDataStructure.userProfileDatabasePath(firebaseUser.uid))
            .set(userInformationProfileData)
            .addOnSuccessListener {

                if (!accountInformationLayoutBinding.phoneNumberAddressView.text.isNullOrEmpty()) {

                    val phoneAuthOptions = PhoneAuthOptions.Builder(Firebase.auth).apply {
                        setPhoneNumber(accountInformationLayoutBinding.phoneNumberAddressView.text.toString())
                        setTimeout(120, TimeUnit.SECONDS)
                        setActivity(this@createUserProfile)
                        setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                                Log.d(this@createUserProfile.javaClass.simpleName, "Phone Number Verified")

                                (application as PremiumStorefrontApplication).firestoreDatabase
                                    .document(accountDataStructure.userProfileDatabasePath(firebaseUser.uid))
                                    .update(
                                        "phoneNumberVerified", true,
                                    )

                                firebaseUser.linkWithCredential(phoneAuthCredential).addOnSuccessListener {
                                    Log.d(this@createUserProfile.javaClass.simpleName, "User Profile Linked To Phone Number Authentication")

                                    accountInformationLayoutBinding.updatingLoadingView.pauseAnimation()

                                    accountInformationLayoutBinding.updatingLoadingView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
                                    accountInformationLayoutBinding.updatingLoadingView.visibility = View.INVISIBLE

                                    Handler(Looper.getMainLooper()).postDelayed({

                                        accountInformationLayoutBinding.submitProfileView.playAnimation()

                                        profileUpdating = true

                                    }, 531)

                                }.addOnFailureListener {
                                    it.printStackTrace()

                                    accountInformationLayoutBinding.updatingLoadingView.pauseAnimation()

                                    accountInformationLayoutBinding.updatingLoadingView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
                                    accountInformationLayoutBinding.updatingLoadingView.visibility = View.INVISIBLE

                                    Handler(Looper.getMainLooper()).postDelayed({

                                        accountInformationLayoutBinding.submitProfileView.playAnimation()

                                        profileUpdating = true

                                    }, 531)

                                }

                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                e.printStackTrace()

                                if (e is FirebaseAuthInvalidCredentialsException) {



                                } else if (e is FirebaseTooManyRequestsException) {



                                }

                            }

                            override fun onCodeSent(verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                                Log.d(this@createUserProfile.javaClass.simpleName, "Verification Code Sent: ${verificationId}")

                            }

                        })
                    }

                    PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions.build())

                } else {

                    accountInformationLayoutBinding.updatingLoadingView.pauseAnimation()

                    accountInformationLayoutBinding.updatingLoadingView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
                    accountInformationLayoutBinding.updatingLoadingView.visibility = View.INVISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({

                        accountInformationLayoutBinding.submitProfileView.playAnimation()

                        profileUpdating = profileUpdatingProcess

                    }, 531)

                }

            }

        (application as PremiumStorefrontApplication).firestoreDatabase
            .document(accountDataStructure.userProfileDatabasePath(firebaseUser.uid))
            .get()
            .addOnSuccessListener { documentSnapshot ->

                documentSnapshot?.let { documentData ->

                    accountInformationLayoutBinding.socialMediaScrollView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
                    accountInformationLayoutBinding.socialMediaScrollView.visibility = View.VISIBLE

                    accountInformationLayoutBinding.instagramAddressView.setText(documentData.data?.get(
                        AccountDataStructure.Attributes.instagramAccount).toString().lowercase(Locale.getDefault()))

                    accountInformationLayoutBinding.twitterAddressView.setText(documentData.data?.get(
                        AccountDataStructure.Attributes.twitterAccount).toString())

                    accountInformationLayoutBinding.phoneNumberAddressView.setText(documentData.data?.get(
                        AccountDataStructure.Attributes.phoneNumber).toString())

                    accountInformationLayoutBinding.inviteFriendsView.visibility = View.VISIBLE
                    accountInformationLayoutBinding.inviteFriendsView.setOnClickListener {

                        SendInvitation(this@createUserProfile, accountInformationLayoutBinding.root)
                            .invite(Firebase.auth.currentUser!!)

                    }

                }

            }

        Glide.with(this@createUserProfile)
            .asDrawable()
            .load(firebaseUser.photoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(glideException: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {

                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                    runOnUiThread {

                        accountInformationLayoutBinding.profileImageView.setImageDrawable(resource)

                        resource?.let {

                            val dominantColor = extractDominantColor(applicationContext, it)
                            val vibrantColor = extractVibrantColor(applicationContext, it)

                            window.setBackgroundDrawable(GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, arrayOf(vibrantColor, dominantColor).toIntArray()))

                            accountInformationLayoutBinding.signupLoadingView.pauseAnimation()
                            accountInformationLayoutBinding.signupLoadingView.visibility = View.INVISIBLE

                            if (isColorDark(dominantColor) && isColorDark(vibrantColor)) {
                                Log.d(this@createUserProfile.javaClass.simpleName, "Dark Extracted Colors")

                            } else {
                                Log.d(this@createUserProfile.javaClass.simpleName, "Light Extracted Colors")

                            }

                        }

                    }

                    return false
                }

            })
            .submit()

    }

}