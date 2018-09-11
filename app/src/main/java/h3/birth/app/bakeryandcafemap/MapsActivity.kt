package h3.birth.app.bakeryandcafemap

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import h3.birth.app.bakeryandcafemap.data.Shop
import java.lang.Double.doubleToRawLongBits



class MapsActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    // GPS用
    private var mLocationManager: LocationManager? = null

    lateinit var shopTag: Marker
    lateinit var mapIcon: BitmapDescriptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /** Called when the user clicks a marker.  */
    override fun onMarkerClick(marker:Marker):Boolean {

        // Retrieve the data from the marker.
        var clickCount = marker.tag as Int?

         // Check if a click count was set, then display the click count.
        if (clickCount != null)
        {
            clickCount = clickCount!! + 1
                marker.tag = clickCount
            Toast.makeText(this,
                    marker.title +
                    " has been clicked " + clickCount + " times.",
            Toast.LENGTH_SHORT).show()
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).

        return false
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mapIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_store_black_24dp)
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(getString(R.string.default_lat).toDouble(), getString(R.string.default_lng).toDouble())
        shopTag = mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        shopTag.setTag(0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13f))
        checkParminssionGeolocation()

        mockMaker()
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
    }

    fun mockMaker(){
        var shop_1 = Shop(1,"MAISON Kayser Cafe", 35.680975, 139.767950, "〒100-0005 東京都千代田区丸の内1-4-1 iiyo!!1F")
        var shop_2 = Shop(2,"ＤＥＡＮ＆ＤＥＬＵＣＡ", 35.680192, 139.768028, "〒100-0005 東京都千代田区丸の内1-4-5 三菱UFJ信託銀行本店ビル1F")
        var shops: MutableList<Shop> = mutableListOf()
        shops.add(shop_1)
        shops.add(shop_2)

        shops.forEach{
            val sydney = LatLng(it.latitude, it.longitude)
            shopTag = mMap.addMarker(MarkerOptions().position(sydney).title(it.name))
            shopTag.setTag(it.id)
        }
    }

    fun checkParminssionGeolocation(){
        // GPS
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1000)
        } else {
            try {
                mMap.setMyLocationEnabled(true)
                var gMapSettings: UiSettings = mMap.getUiSettings()
                gMapSettings.setMyLocationButtonEnabled(true)
                // Request location updates
                mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 1f, this)
                mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300, 1f, this)
            } catch(ex: SecurityException) {
                Log.e("GeolocationError", ex.message)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.i("Geolocation", "lat = "+location.latitude + " : lng = "+location.longitude)
        var preferences : SharedPreferences = getSharedPreferences(getString(R.string.PREF_FILE_NAME), Context.MODE_PRIVATE)
        preferences.edit().putLong(getString(R.string.PREF_LAST_LAT), doubleToRawLongBits(location.latitude)).commit()
        preferences.edit().putLong(getString(R.string.PREF_LAST_LNG), doubleToRawLongBits(location.longitude)).commit()

        val sydney = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}

