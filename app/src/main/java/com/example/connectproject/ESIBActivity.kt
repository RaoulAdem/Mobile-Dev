package com.example.connectproject;

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.location.GpsStatus
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.connectproject.databinding.ActivityEsibBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class ESIBActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {
    private lateinit var mMap: MapView
    private lateinit var controller: IMapController;
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay;
    private lateinit var binding: ActivityEsibBinding
    private lateinit var navigationView: BottomNavigationView
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

        controller.setZoom(19.0)
        controller.setCenter(startPoint)

        // Enable follow location to automatically center the map on the user's location
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        mMap.addMapListener(this)

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
