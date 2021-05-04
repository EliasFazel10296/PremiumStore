/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 5/4/21 2:42 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.CategoryContent.Adapter

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.geeksempire.premium.storefront.R
import co.geeksempire.premium.storefront.StorefrontConfigurations.DataStructure.StorefrontCategoriesData
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.CategoryContent.ViewHolder.CategoriesViewHolder
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.Storefront
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class CategoriesAdapter(private val context: Storefront) : RecyclerView.Adapter<CategoriesViewHolder>() {

    val storefrontCategories: ArrayList<StorefrontCategoriesData> = ArrayList<StorefrontCategoriesData>()

    override fun getItemCount() : Int {

        return storefrontCategories.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : CategoriesViewHolder {

        return CategoriesViewHolder(LayoutInflater.from(context).inflate(R.layout.storefront_category_item, viewGroup, false))
    }

    override fun onBindViewHolder(categoriesViewHolder: CategoriesViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(categoriesViewHolder, position, payloads)

        val categoryBackgroundItem = context.getDrawable(R.drawable.category_background_item) as LayerDrawable
        categoryBackgroundItem.findDrawableByLayerId(R.id.temporaryBackground).setTint(context.getColor(R.color.dark))

        categoriesViewHolder.productIconImageView.background = categoryBackgroundItem

    }

    override fun onBindViewHolder(categoriesViewHolder: CategoriesViewHolder, position: Int) {

        Glide.with(context)
                .asDrawable()
                .load(storefrontCategories[position].categoryIconLink)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(CircleCrop())
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(glideException: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                        resource?.let {

                            context.runOnUiThread {

                                val categoryBackgroundItem = context.getDrawable(R.drawable.category_background_item) as LayerDrawable
                                categoryBackgroundItem.findDrawableByLayerId(R.id.temporaryBackground).setTint(context.getColor(R.color.dark))

                                categoriesViewHolder.productIconImageView.background = categoryBackgroundItem

                                resource.setTint(context.getColor(R.color.light))

                                categoriesViewHolder.productIconImageView.setImageDrawable(resource)

                            }

                        }

                        return false
                    }

                })
                .submit()

        categoriesViewHolder.rootView.setOnClickListener {

            val categoryBackgroundSelectedItem = context.getDrawable(R.drawable.category_background_selected_item) as LayerDrawable

            categoriesViewHolder.productIconImageView.background = categoryBackgroundSelectedItem

            val currentPosition = position

            storefrontCategories.forEachIndexed { index, storefrontCategoriesData ->

                if (index != currentPosition) {

                    notifyItemChanged(index, null)

                }

            }

        }

        categoriesViewHolder.rootView.setOnLongClickListener { view ->

//            BalloonOptionsMenu(context = context,
//                rootView = context.storefrontLayoutBinding.rootView,
//                balloonItemsAction = object : BalloonItemsAction {
//
//                    override fun onBalloonItemClickListener(balloonOptionsMenu: BalloonOptionsMenu, balloonOptionsRootView: View, itemView: View) {
//                        Log.d(this@CategoriesAdapter.javaClass.simpleName, itemView.tag.toString())
//
//                        balloonOptionsMenu.removeBalloonOption()
//
//                    }
//
//                }).initializeBalloonPosition(anchorView = view)
//                .setupOptionsItems(arrayListOf("<b>${storefrontCategories[position].categoryName}</b>", context.getString(R.string.categoryShowAllApplications)))

            false
        }

    }

}