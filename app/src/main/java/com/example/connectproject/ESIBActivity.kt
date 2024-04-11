package com.example.connectproject;

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.location.GpsStatus
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.connectproject.databinding.ActivityEsibBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException

class ESIBActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController;
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay;
    private lateinit var binding: ActivityEsibBinding
    private lateinit var navigationView: BottomNavigationView
    private val BASE_URL = "https://router.project-osrm.org"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEsibBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())

        controller = mMap.controller

        val marker = Marker(mMap)
        val startPoint = GeoPoint(33.865534218437595, 35.564146501618225)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "ESIB"
        mMap.overlays.add(marker)

        val polygonPoints = ArrayList<GeoPoint>()
        polygonPoints.add(GeoPoint(33.86466148180942, 35.56529227530186))
        polygonPoints.add(GeoPoint(33.86457907703166, 35.561841421490925))
        polygonPoints.add(GeoPoint(33.86646313002603, 35.56177826861072))
        polygonPoints.add(GeoPoint(33.866485603568265, 35.56509379482123))
        val polygon = Polygon()
        polygon.points = polygonPoints
        polygon.fillPaint.color = Color.argb(75, 255, 0, 0) // Set the fill color
        polygon.strokeColor = Color.RED // Set the border color
        mMap.overlays.add(polygon)

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        mMap.overlays.add(mMyLocationOverlay)

        controller.setZoom(15.0)
        controller.setCenter(startPoint)

        // Enable follow location to automatically center the map on the user's location
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        mMap.addMapListener(this)

        val start = "33.87761721017601,35.57818154544155"
        val end = "33.865534218437595,35.564146501618225"
        requestRoute(start, end)

        navigationView = findViewById(R.id.navigation)
        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }


    }


    private fun requestRoute(start: String, end: String) {
        val client = OkHttpClient()

        val url = "https://router.project-osrm.org/route/v1/driving/33.87761721017601,35.57818154544155;33.865534218437595,35.564146501618225?steps=true"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ESIBActivity", "Error fetching route: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonData = response.body?.string()
                    response.close()

                    if (jsonData != null) {
                        val points = parseRoute(jsonData)
                        if (points.isNotEmpty()) {
                            drawRoute(points)
                        } else {
                            Log.e("ESIBActivity", "No route points found.")
                            // Handle the case where no route points are found
                        }
                    } else {
                        Log.e("ESIBActivity", "Empty response body.")
                        // Handle the case where the response body is empty
                    }
                } catch (e: Exception) {
                    Log.e("ESIBActivity", "Error parsing route response: ${e.message}")
                    // Handle the parsing error
                }
            }
        })
    }


    private fun parseRoute(jsonData: String): List<GeoPoint> {
        val routePoints = mutableListOf<GeoPoint>()

        try {
            val jsonObject = JSONObject(jsonData)
            val routesArray = jsonObject.getJSONArray("routes")

            if (routesArray.length() > 0) {
                val routeObject = routesArray.getJSONObject(0)
                val geometryObject = routeObject.getJSONObject("geometry")
                val coordinatesArray = geometryObject.getJSONArray("coordinates")

                for (i in 0 until coordinatesArray.length()) {
                    val coordinateArray = coordinatesArray.getJSONArray(i)
                    val lat = coordinateArray.getDouble(1)
                    val lon = coordinateArray.getDouble(0)
                    routePoints.add(GeoPoint(lat, lon))
                }
            }
        } catch (e: JSONException) {
            Log.e("ESIBActivity", "Error parsing route points: ${e.message}")
            // Handle parsing error (e.g., return empty list)
        }

        return routePoints
    }

    private fun drawRoute(points: List<GeoPoint>) {
        runOnUiThread {
            val polyline = Polyline()
            polyline.setPoints(points)
            mMap.overlays.add(polyline)
            mMap.invalidate()
        }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        event?.zoomLevel?.let { controller.setZoom(it) }
        return false;
    }

    override fun onGpsStatusChanged(event: Int) {
        TODO("Not yet implemented")
    }


}
