/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 5/19/21, 3:41 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.CategoryContent.Adapter

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.geeksempire.premium.storefront.R
import co.geeksempire.premium.storefront.StorefrontConfigurations.DataStructure.StorefrontCategoriesData
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.AllContent.Filter.FilterAllContent
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.CategoryContent.ViewHolder.CategoriesViewHolder
import co.geeksempire.premium.storefront.StorefrontConfigurations.UserInterface.Storefront
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import net.geeksempire.balloon.optionsmenu.library.BalloonItemsAction
import net.geeksempire.balloon.optionsmenu.library.BalloonOptionsMenu

class CategoriesAdapter(private val context: Storefront, private val filterAllContent: FilterAllContent) : RecyclerView.Adapter<CategoriesViewHolder>() {

    val storefrontCategories: ArrayList<StorefrontCategoriesData> = ArrayList<StorefrontCategoriesData>()

    var lastPosition: Int = 0

    override fun getItemCount(): Int {

        return storefrontCategories.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CategoriesViewHolder {

        return CategoriesViewHolder(LayoutInflater.from(context).inflate(R.layout.storefront_category_item, viewGroup, false))
    }

    override fun onBindViewHolder(categoriesViewHolder: CategoriesViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(categoriesViewHolder, position, payloads)

        categoriesViewHolder.productIconImageView.background = context.getDrawable(R.drawable.category_background_item)
        categoriesViewHolder.productIconImageView.imageTintList = ColorStateList.valueOf(context.getColor(R.color.dark))

    }

    override fun onBindViewHolder(categoriesViewHolder: CategoriesViewHolder, position: Int) {

        categoriesViewHolder.productIconImageView.background = if (storefrontCategories[position].selectedCategory || position == 0) {

            context.getDrawable(R.drawable.category_background_selected_item)

        } else {

            context.getDrawable(R.drawable.category_background_item)

        }

        categoriesViewHolder.productIconImageView.imageTintList = if (storefrontCategories[position].selectedCategory || position == 0) {

            ColorStateList.valueOf(context.getColor(R.color.light))

        } else {

            ColorStateList.valueOf(context.getColor(R.color.dark))

        }

        categoriesViewHolder.productIconImageView.tag = storefrontCategories[position].categoryId
        categoriesViewHolder.productIconImageView.contentDescription = storefrontCategories[position].categoryName

        Glide.with(context)
                .asDrawable()
                .load(storefrontCategories[position].categoryIconLink)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(categoriesViewHolder.productIconImageView)

        categoriesViewHolder.rootView.setOnClickListener {

            if (context.storefrontAllUnfilteredContents.isNotEmpty()) {

                notifyItemChanged(lastPosition, null)

                if (position == 0) {

                    context.storefrontLiveData.allFilteredContentItemData.postValue(context.storefrontAllUntouchedContents)

                } else {

                    filterAllContent.filterAllContentByCategory(context.storefrontAllUnfilteredContents, storefrontCategories[position].categoryName)

                }

                storefrontCategories[lastPosition].selectedCategory = false
                storefrontCategories[position].selectedCategory = true

                lastPosition = position

                categoriesViewHolder.productIconImageView.background = context.getDrawable(R.drawable.category_background_selected_item)
                categoriesViewHolder.productIconImageView.imageTintList = ColorStateList.valueOf(context.getColor(R.color.light))

            } else {



            }

        }

        categoriesViewHolder.rootView.setOnLongClickListener { view ->

            context.balloonOptionsMenu.also {

                it.initializeBalloonPosition(anchorView = view)
                it.setupOptionsItems(arrayListOf("<b>${storefrontCategories[position].categoryName.replace(" Applications", "")}</b>",
                    context.getString(R.string.categoryShowAllApplications)))

                it.setupActionListener(balloonItemsAction = object : BalloonItemsAction {

                    override fun onBalloonItemClickListener(balloonOptionsMenu: BalloonOptionsMenu, balloonOptionsRootView: View, itemView: View) {

                        balloonOptionsMenu.removeBalloonOption()

                        Log.d(this@CategoriesAdapter.javaClass.simpleName, itemView.tag.toString())
                    }

                })

            }

            true
        }

    }

}