package com.ssafy.howdomodo.ui.theater

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.Marker
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ssafy.howdomodo.R
import com.ssafy.howdomodo.`object`.ObjectMovie
import com.ssafy.howdomodo.data.datasource.model.Theater
import com.ssafy.howdomodo.ui.gwanSelect.GwanSelectActivity
import kotlinx.android.synthetic.main.activity_theater.*
import kotlinx.coroutines.selects.select
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TheaterActivity : AppCompatActivity() {
    private val getTheatersViewModel: GetTheatersViewModel by viewModel()

    companion object {
        var theater_select = false
        var selectSiName = "서울특별시"
        var selectGuName = "마포구"
        lateinit var selectedTheater : Theater
        lateinit var mapView: MapView
    }

    var theaterList = arrayListOf<Theater>(
        Theater(1246, 175, "브로드웨이(신사)", "서울특별시 강남구 논현동 도산대로 8길 8", "롯데시네마", 37.5164, 127.022),
        Theater(1247, 175, "도곡", "서울특별시 강남구 도곡동 174-3", "롯데시네마", 37.4875, 127.047),
        Theater(1248, 175, "청남씨네시티", "서울특별시 강남구 도산대로 323, 씨네시티빌딩 14층", "CGV", 37.5229, 127.037),
        Theater(1249, 175, "코엑스", "서울특별시 강남구 삼성1동 봉은사로 524", "메가박스", 37.5129, 127.057),
        Theater(1250, 175, "압구정", "서울특별시 강남구 신사동 압구정로30길 45", "CGV", 37.5243, 127.029),
        Theater(1251, 175, "de CHEF 압구정", "서울특별시 강남구 신사동 압구정로30길 45", "CGV", 37.5243, 127.029),
        Theater(1252, 175, "씨티(강남대로)", "서울특별시 강남구 역삼1동 강남대로 422", "메가박스", 37.5004, 127.027),
        Theater(1253, 175, "강남", "서울특별시 강남구 역삼동 강남대로 438", "CGV", 37.5016, 127.026),
    )
    lateinit var theaterAdapter: TheaterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theater)

        //해당 지역이 뭔지 받아오기.
        if (intent.hasExtra("siName")) {
            selectSiName = intent.getStringExtra("siName")!!
            selectGuName = intent.getStringExtra("guName")!!
            Toast.makeText(
                this@TheaterActivity,
                intent.getStringExtra("siName") + intent.getStringExtra("guName"),
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            Log.e("유사에러","이전 페이지에서 넘어온 시도데이터가 없음 - > 강남으로 디폴트 검색!" + selectSiName + selectGuName)
        }

        act_theater_tv_search.text = selectSiName +" " + selectGuName
        getTheatersViewModel.getTheaters(selectSiName, selectGuName)

        observeData()



        act_theater_cl_theater_selected.setOnClickListener {
            val intent = Intent(this, GwanSelectActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeData() {
        getTheatersViewModel.getTheatersError.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        getTheatersViewModel.getTheatersResponse.observe(this, Observer {
            //영화 리스트 가져왔을떄.
            if (it.status == 200) {
                Toast.makeText(this, "영화관 리스트 출력!", Toast.LENGTH_SHORT).show()

                theaterAdapter = TheaterAdapter(
                    object :
                        TheaterAdapter.TheaterViewHolder.TheaterClickListener {
                        override fun onclick(position: Int, textView: TextView) {
                            if (!theaterAdapter.getClicked(position)) {
                                if (theaterAdapter.getClickedTheater() != -1) {
                                    theaterAdapter.setClicked(theaterAdapter.getClickedTheater(), false)
                                }
                                theaterAdapter.setClicked(position, true)
                                theater_select = true
                                selectedTheater = theaterList[position]
                                changeCenter()

                            } else if (theaterAdapter.getClicked(position)) {
                                ObjectMovie.movieTheater =
                                    theaterAdapter.theaterData[position].theaterBrand + " " + theaterAdapter.theaterData[position].theaterName
                                theaterAdapter.setClicked(theaterAdapter.getClickedTheater(), false)
                                theater_select = false
                            }
                            setButtonActive()
                        }

                        override fun starClick(position: Int, starImageView: ImageView) {
                            Toast.makeText(this@TheaterActivity, position.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                theaterList = it.data!!
                theaterAdapter.setTheaterData(theaterList)
                act_theater_rv_theaters.adapter = theaterAdapter
                var theaterlm = LinearLayoutManager(this)
                act_theater_rv_theaters.layoutManager = theaterlm
                act_theater_rv_theaters.setHasFixedSize(true)
                //Maps

                mapView = MapView(this)
                var mapViewController = act_theater_rl_map_view as ViewGroup
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.4874592, 127.0471432), true)
                mapView.setZoomLevel(7, true)


                for (i in theaterList.indices) {
                    var t = theaterList[i]
                    var marker_img = R.drawable.ic_launcher
                    var selected_marker_img = R.drawable.ic_launcher

                    if (t.theaterBrand == "CGV") {
                        marker_img = R.drawable.cgv_marker_unselected
                        selected_marker_img = R.drawable.cgv_marker
                    } else if (t.theaterBrand == "메가박스") {
                        marker_img = R.drawable.megabox_marker_unselected
                        selected_marker_img = R.drawable.megabox_marker
                    } else if (t.theaterBrand == "롯데시네마") {
                        marker_img = R.drawable.lotte_marker_unselected
                        selected_marker_img = R.drawable.lotte_marker
                    } else {
                        marker_img = R.drawable.ic_launcher
                    }

                    var marker = MapPOIItem()
                    marker.itemName = t.theaterBrand + " " + t.theaterName
                    marker.tag = t.theaterId
                    marker.mapPoint = MapPoint.mapPointWithGeoCoord(t.theaterLat, t.theaterLon)
                    marker.markerType = MapPOIItem.MarkerType.CustomImage
                    marker.setCustomImageResourceId(marker_img)
                    marker.userObject = t


                    marker.selectedMarkerType = MapPOIItem.MarkerType.CustomImage
                    marker.customSelectedImageResourceId = selected_marker_img
                    marker.isCustomImageAutoscale = false
                    marker.setCustomImageAnchor(0.5f, 1.0f)

                    mapView.setCalloutBalloonAdapter(CustomInfoWindow(context = this, theater = t,m = marker))

                    mapView.addPOIItem(marker)
                }
                mapView.fitMapViewAreaToShowAllPOIItems()
                mapViewController.addView(mapView)


            } else {
                Toast.makeText(this, "TheaterActivity observeData 에러!", Toast.LENGTH_SHORT).show()

            }
        })
    }

    fun changeCenter(){
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(selectedTheater.theaterLat, selectedTheater.theaterLon), true)
    }
    fun setButtonActive() {
        if (theater_select) {
            act_theater_cl_theater_selected.setBackgroundColor(Color.parseColor("#f73859"))
            act_theater_cl_theater_selected.isClickable = true
        } else {
            act_theater_cl_theater_selected.setBackgroundColor(Color.parseColor("#aaaaaa"))
            act_theater_cl_theater_selected.isClickable = false
        }
    }

    class CustomInfoWindow(private val context: Context, val theater: Theater,var m : MapPOIItem) :
        CalloutBalloonAdapter {
        lateinit var mCalloutBalloon: View

        init {
            mCalloutBalloon =
                LayoutInflater.from(context).inflate(R.layout.item_custom_infowindow, null)
        }


        override fun getCalloutBalloon(poiItem: MapPOIItem): View {
            var theater_img = R.drawable.ic_launcher

//            Log.e("name",poiItem.itemName)
//            Log.e("address",((poiItem.userObject) as Theater).theaterAddress)
            if (poiItem.itemName.contains("CGV")) {
                theater_img = R.drawable.cgv
            } else if (poiItem.itemName.contains("메가박스")) {
                theater_img = R.drawable.megabox
            } else if (poiItem.itemName.contains("롯데시네마")) {
                theater_img = R.drawable.lottecinema
            } else {
                theater_img = R.drawable.ic_launcher
            }


            (mCalloutBalloon.findViewById<View>(R.id.item_infowindow_iv_theater_image) as ImageView).setImageResource(
                theater_img
            )
            (mCalloutBalloon.findViewById<View>(R.id.item_infowindow_tv_theater_title) as TextView).text =
//                ((poiItem.userObject) as Theater).theaterBrand+ " " +((poiItem.userObject) as Theater).theaterName
                poiItem.itemName

            (mCalloutBalloon.findViewById<View>(R.id.item_infowindow_tv_theater_desc) as TextView).text =
                ((poiItem.userObject) as Theater).theaterAddress
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem): View {
            return mCalloutBalloon
        }

    }
}


