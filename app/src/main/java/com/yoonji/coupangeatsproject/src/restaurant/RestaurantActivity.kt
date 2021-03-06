package com.yoonji.coupangeatsproject.src.restaurant

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.MODE_SCROLLABLE
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.yoonji.coupangeatsproject.ApplicationClass
import com.yoonji.coupangeatsproject.R
import com.yoonji.coupangeatsproject.config.BaseActivity
import com.yoonji.coupangeatsproject.databinding.ActivityRestaurantBinding
import com.yoonji.coupangeatsproject.src.category.CategoryService
import com.yoonji.coupangeatsproject.src.main.home.models.storeMenuByCateResult
import com.yoonji.coupangeatsproject.src.order_cart.OrderCartActivity
import com.yoonji.coupangeatsproject.src.restaurant.adapter.RestaurantMenuAdapter
import com.yoonji.coupangeatsproject.src.restaurant.adapter.RestaurantReviewAdapter
import com.yoonji.coupangeatsproject.src.restaurant.data.RestaurantDetailData
import com.yoonji.coupangeatsproject.src.restaurant.data.RestaurantMenuData
import com.yoonji.coupangeatsproject.src.restaurant.data.RestaurantReviewData
import com.yoonji.coupangeatsproject.src.restaurant.model.RestaurantResponse


class RestaurantActivity : BaseActivity<ActivityRestaurantBinding>(ActivityRestaurantBinding::inflate), RestaurantActivityView{

    private val TAG = "*****RestaurantActivity------>"

    lateinit var reviewAdapter : RestaurantReviewAdapter
    val reviewDatas = mutableListOf<RestaurantReviewData>()

    lateinit var menuAdapter : RestaurantMenuAdapter
    var menuDatas = mutableListOf<RestaurantMenuData>()
    var menuDetailDatas = mutableListOf<RestaurantDetailData>()

    var totalCount = ApplicationClass.sSharedPreferences.getInt("RestaurantCount",0)
    var totalPrice = ApplicationClass.sSharedPreferences.getInt("RestaurantPrice",0)

    val editor: SharedPreferences.Editor = ApplicationClass.sSharedPreferences.edit()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<View>(R.id.toolbar_restaurant) as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)

        //????????? ??????
        window?.decorView?.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.TRANSPARENT



        // ????????? ?????? ??? ???????????? or ?????? ????????? ?????? ???????????? ???????????? ????????? ????????? ?????? ????????????
        val count = intent.getIntExtra("cartCount", 0)
        val price = intent.getIntExtra("cartPrice", 0)
        val check = intent.getStringExtra("cartCheck")
        totalCount += count
        totalPrice += price

        editor.putInt("RestaurantCount", totalCount)        //???????????? ????????? ????????? sharedPreference ???????????? ??????
        editor.putInt("RestaurantPrice", totalPrice)
        editor.apply()

        // ???????????? ???????????? ??????
        if(check == "letsOrder" || totalCount>0){
            binding.layoutRestaurantCart.visibility = VISIBLE
            binding.tvRestaurantTotalCount.text = totalCount.toString()
            binding.tvRestaurantTotalPrice.text = totalPrice.toString() + "???"

            binding.layoutRestaurantCart.setOnClickListener{
                val intent = Intent(this, OrderCartActivity::class.java)
                startActivity(intent)
            }
        }

        // ????????? ???????????? api
        RestaurantService(this).getRestaurantDetail(1)
    }

    fun initReviewRecycler(){
        reviewAdapter = RestaurantReviewAdapter(this)
        binding.rvRestaurantReview.adapter = reviewAdapter

        reviewAdapter.datas = reviewDatas
    }

    fun initMenuRecycler(){
        menuAdapter = RestaurantMenuAdapter(this)
        binding.rvRestaurantMenu.adapter = menuAdapter

        menuAdapter.datas = menuDatas
    }


    override fun onResume() {
        super.onResume()

        binding.imgvRestaurantBack.setOnClickListener { finish()}
    }

    override fun onGetRestaurantSuccess(response: RestaurantResponse) {
        Log.d(TAG, "onGetRestaurantSuccess: ?????? "+ response.result)

        if(response.isSuccess){

            // ?????? ???????????? ??? ????????? ?????? ????????? ??? ????????? ??????
            var isShow = true
            var scrollRange = -1
            binding.appBarRestaurant.addOnOffsetChangedListener( AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if (scrollRange == -1){
                    scrollRange = appBarLayout.totalScrollRange
                }

                if (scrollRange + verticalOffset == 0){     //appbar??? ????????? ???, ????????? ????????????
                    binding.tvRestaurantToolbarTitle.visibility = VISIBLE
                    binding.tvRestaurantToolbarTitle.text = response.result.storeInfoResult[0].name
                    binding.imgvRestaurantBack.imageTintList = ColorStateList.valueOf(this.getColor(R.color.black))
                    binding.imgvRestaurantHeart.imageTintList = ColorStateList.valueOf(this.getColor(R.color.pinkForLike))
                    binding.imgvRestaurantShare.imageTintList = ColorStateList.valueOf(this.getColor(R.color.black))

                    isShow = true

                } else if (isShow){     //appbar??? ???????????? ???
                    binding.tvRestaurantToolbarTitle.visibility = INVISIBLE
                    binding.imgvRestaurantBack.imageTintList = ColorStateList.valueOf(this.getColor(R.color.white))
                    binding.imgvRestaurantHeart.imageTintList = ColorStateList.valueOf(this.getColor(R.color.white))
                    binding.imgvRestaurantShare.imageTintList = ColorStateList.valueOf(this.getColor(R.color.white))

                    isShow = false
                }
            } )

            //?????? ??????
            var info = response.result.storeInfoResult[0]
            Glide.with(this).load(response.result.storeImgResult[0].storeImg).into(binding.imgvRestaurantMain)
            binding.tvRestaurantTitle.text = info.name
            binding.tvRestaurantToolbarTitle.text = info.name
            binding.tvRestaurantDeliveryTime.text = info.deliveryTime
            if(info.starRating == null) {
                binding.imgvRestaurantStar.visibility = GONE
                binding.tvRestaurantReviewScore.visibility = GONE
            }else{
                binding.imgvRestaurantStar.visibility = VISIBLE
                binding.tvRestaurantReviewScore.text = info.starRating.toString()
            }
            if(info.fastDelivery == "Y")
                binding.imgvRestaurantCheetah.visibility = VISIBLE
            else
                binding.imgvRestaurantCheetah.visibility = GONE
            binding.tvRestaurantDeliveryFee.text = info.deliveryFee
            binding.tvRestaurantMinimumOrder.text = info.minOrderPrice


            //??? ????????????
            for(i in response.result.storeMenuByCate){
                binding.tabRestaurant.addTab( binding.tabRestaurant.newTab().setText(i.menuCategory))
                Log.d(TAG, "tab ??????")
            }
            binding.tabRestaurant.tabMode = MODE_SCROLLABLE
            binding.tabRestaurant.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    TODO("?????????")
//                    if(binding.tabRestaurant.selectedTabPosition == )
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })


            //??????
            for((index,value) in response.result.storePtReviewResult.withIndex()){
                if(value.reviewImg == null){
                    reviewDatas.add( RestaurantReviewData(
                        reviewImg = "",
                        reviewTitle = value.content,
                        reviewStarScore = value.starRating) )
                }else{
                    reviewDatas.add( RestaurantReviewData(
                        reviewImg = value.reviewImg,
                        reviewTitle = value.content,
                        reviewStarScore = value.starRating) )
                }

            }

//            reviewDatas.apply{
//                add(RestaurantReviewData(
//                    reviewImg = R.drawable.img_restaurant_review,
//                    reviewTitle = "???????????? ????????????~ ??????????????? ????????????",
//                    reviewStarScore = 3))
//                add(RestaurantReviewData(
//                    reviewImg = R.drawable.img_restaurant_review,
//                    reviewTitle = "???????????? ????????????~ ??????????????? ????????????dfdasfasfsfsfasfsdfsdfsf",
//                    reviewStarScore = 5))
//                add(RestaurantReviewData(
//                    reviewImg = R.drawable.img_restaurant_review,
//                    reviewTitle = "???????????? ????????????~ ??????????????? ????????????",
//                    reviewStarScore = 1))
//            }

            //??????
            var storeMenu = response.result.storeMenuByCate

            for((index,value) in storeMenu.withIndex()){
                var menu = storeMenu[index].menu

                for(i in menu){
                    if(i.menuImg == null && i.menuInfo == null){
                        menuDetailDatas.apply {
                            add( RestaurantDetailData(
                                restaurantDetailImg = "",
                                restaurantDetailDescrip = "",
                                restaurantDetailPrice = i.price,
                                restaurantDetailTitle = i.name) )
                        }
                    }else if(i.menuImg == null){
                        menuDetailDatas.apply {
                            add( RestaurantDetailData(
                                restaurantDetailImg = "",
                                restaurantDetailDescrip = i.menuInfo,
                                restaurantDetailPrice = i.price,
                                restaurantDetailTitle = i.name) )
                        }
                    }else if(i.menuInfo == null){
                        menuDetailDatas.apply {
                            add( RestaurantDetailData(
                                restaurantDetailImg = i.menuImg,
                                restaurantDetailDescrip = "",
                                restaurantDetailPrice = i.price,
                                restaurantDetailTitle = i.name) )
                        }
                    }else{
                        menuDetailDatas.apply {
                            add( RestaurantDetailData(
                                restaurantDetailImg = i.menuImg,
                                restaurantDetailDescrip = i.menuInfo,
                                restaurantDetailPrice = i.price,
                                restaurantDetailTitle = i.name) )
                        }
                    }
                }

                menuDatas.apply{
                    add(RestaurantMenuData(restaurantMenuTitle = value.menuCategory, menuDetailArrayList = menuDetailDatas))
                }
            }

            initReviewRecycler()
            initMenuRecycler()

        }else{
            Log.d(TAG, "response is false")
        }
    }

    override fun onGetRestaurantFailure(message: String) {
        Log.d(TAG, "onGetRestaurantFailure: $message")
    }
}

