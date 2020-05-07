package kenchovi.androiddev.gnavirestaurants

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception
import java.net.SocketTimeoutException

// API key & URL
const val GURUNAVI_KEY = "5f15212d3cff5ac8b1af3aab58623cbf"
const val GURUNAVI_API_URL = "https://api.gnavi.co.jp/RestSearchAPI/v3/"

//region Search condition variables
var latitude = .0
    private set
var longitude = .0
    private set
var storeName = ""
    private set
var searchRange = 2
    private set
var noSmoking = 0
    private set
var bottomlessCup = 0
    private set
var withPet = 0
    private set
var eMoney = 0
    private set
var buffet = 0
    private set
var takeout = 0
    private set
var privateRoom = 0
    private set
var midnight = 0
    private set
//endregion

class MainActivity : AppCompatActivity() {

    private val _permissionGPS = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onStop() {
        super.onStop()
        btSearch.isEnabled = true
    }

    override fun onStart() {
        super.onStart()
        // Check permission & change btSearch availability
        if (ActivityCompat.checkSelfPermission(applicationContext, _permissionGPS)
            != PackageManager.PERMISSION_GRANTED) {
            btSearch.isEnabled = false
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity, _permissionGPS))
                Toast.makeText(applicationContext, R.string.please_prms_gps, Toast.LENGTH_SHORT).show()
        } else btSearch.isEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // The initial value of the Range; 500[m]
        spRange.setSelection(1)

        //region Setting for using GPS
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = GPSLocationListener()

        // If no permission of using GPS
        if (ActivityCompat.checkSelfPermission(applicationContext,
                _permissionGPS) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(_permissionGPS)
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1000)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        //endregion
    }

    // Processing for Permission Dialog
    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = GPSLocationListener()

            if (ActivityCompat.checkSelfPermission(
                    applicationContext, _permissionGPS)
                != PackageManager.PERMISSION_GRANTED) return

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        } else Toast.makeText(applicationContext, R.string.please_prms_gps, Toast.LENGTH_SHORT).show()
    }

    private inner class GPSLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Setting coordinates
            latitude = location.latitude
            longitude = location.longitude
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }

    // from onSearchClick; the listener set on btSearch
    @SuppressLint("StaticFieldLeak")
    private inner class DataReceiver : AsyncTask<String, Any?, List<Any>>() {
        override fun doInBackground(vararg params: String): List<Any> {
            var result: String
            var error = false

            try {
                result = getJSONData() // Functions.kt
            } catch (e: SocketTimeoutException) {
                result = "接続がタイムアウトしました。"
                error = true
            } catch (e: Exception) {
                result = "該当する店舗はありません。"
                error = true
            }

            return listOf(result, error)
        }

        override fun onPostExecute(result: List<Any>) {
            if (result[1] as Boolean) {
                // Processing on error; Toast display
                Toast.makeText(applicationContext, result[0] as String, Toast.LENGTH_SHORT).show()
                btSearch.isEnabled = true
            } else {
                // Set values & intent
                val total = JSONObject(result[0] as String).getInt("total_hit_count")
                val intentMenu = Intent(applicationContext, MenuResultActivity::class.java)
                intentMenu.putExtra("json", result[0] as String)
                intentMenu.putExtra("total", total)
                intentMenu.putExtra("limit", limitString())
                // Start of MenuResultActivity
                startActivity(intentMenu)
            }
        }
    }

    fun onSearchClick(view: View) {
        // Prevention of repeated hits -> Reset by onStop(), Processing on error in DataReceiver()
        view.isEnabled = false

        //region Setting search conditions
        storeName = etSearchName.text.toString()
        searchRange = spRange.selectedItemPosition + 1
        offsetPage = 1
        noSmoking = cbNoSmoking.convertInt()
        bottomlessCup = cbBottomless.convertInt()
        withPet = cbWithPet.convertInt()
        eMoney = cbEMoney.convertInt()
        buffet = cbBuffet.convertInt()
        takeout = cbTakeout.convertInt()
        privateRoom = cbPrivateRoom.convertInt()
        midnight = cbMidnight.convertInt()
        //endregion

        DataReceiver().execute()
    }

    // Narrowing conditions string
    private fun limitString(): String {
        var str = ""
        if (cbNoSmoking.isChecked) str += getString(R.string.cb_no_smoking) + ", "
        if (cbPrivateRoom.isChecked) str += getString(R.string.cb_private_room) + ", "
        if (cbBuffet.isChecked) str += getString(R.string.cb_buffet) + ", "
        if (cbBottomless.isChecked) str += getString(R.string.cb_bottomless_cup) + ", "
        if (cbTakeout.isChecked) str += getString(R.string.cb_takeout)
        if (cbMidnight.isChecked) str += getString(R.string.cb_midnight) + ", "
        if (cbEMoney.isChecked) str += getString(R.string.cb_e_money) + ", "
        if (cbWithPet.isChecked) str += getString(R.string.cb_with_pet) + ", "

        return if (str.isBlank()) "なし" else str.removeSuffix(", ")
    }

    private fun CheckBox.convertInt() = if (this.isChecked) 1 else 0
}
