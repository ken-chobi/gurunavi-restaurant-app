package kenchovi.androiddev.gnavirestaurants

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.row.*
import org.json.JSONObject

class DetailsActivity : AppCompatActivity() {

    private val _imageSize = 540 // Size of images

    override fun onStop() {
        super.onStop()
        btToMap.isEnabled = true
        btMobilePage.isEnabled = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val position = intent.getIntExtra("position", -1)
        val json = JSONObject(intent.getStringExtra("json")!!)
            .getJSONArray("rest").getJSONObject(position)
        val telNum = json.getString("tel")
        val urlMobile = json.getString("url_mobile")
        val storeAdd = json.getString("address")

        setData(json, position)

        //region Setting of Buttons
        // Setting of btCall; If there is no data to use, hide the view
        if (telNum.isBlank()) btCall.visibility = View.GONE
        else {
            btCall.visibility = View.VISIBLE
            btCall.setOnClickListener(OnButtonClickListener(telNum.replace("-", "")))
        }
        // Setting of btMobilePage; If there is no data to use, hide the view
        if (urlMobile.isBlank()) btMobilePage.visibility = View.GONE
        else {
            btMobilePage.visibility = View.VISIBLE
            btMobilePage.setOnClickListener(OnButtonClickListener(urlMobile))
        }
        // Setting of btToMap; If there is no data to use, hide the view
        if (storeAdd.isBlank()) btToMap.visibility = View.GONE
        else {
            btToMap.visibility = View.VISIBLE
            btToMap.setOnClickListener(OnButtonClickListener("0,0?q=$storeAdd"))
        }
        //endregion
    }

    private fun setData(jsonData: JSONObject, position: Int) {
        if (position < 0) {
            tvName.text = "データがありません"
        } else {
            //region Get data
            /**
             * plusTitleNotBlack(), getAccessStr(), accessNote(), plusSuffixNotBlack()
             *  -> Functions.kt [ctrl + left-click]
             */
            val storeName = jsonData.getString("name")
            val img1Url = jsonData.getJSONObject("image_url").getString("shop_image1")
            val img2Url = jsonData.getJSONObject("image_url").getString("shop_image2")
            val openTime = jsonData.getString("opentime").plusTitleNotBlank("営業時間:")
            val address = jsonData.getString("address")
            val access = jsonData.getJSONObject("access")
            val accessStr = access.getAccessStr() + access.accessNote()
            val tel = jsonData.getString("tel").plusTitleNotBlank("TEL:")
            val fax = jsonData.getString("fax").plusTitleNotBlank("FAX:")
            val category = jsonData.getString("category")
            val holiday = jsonData.getString("holiday").plusTitleNotBlank("休日:")
            val prLong = jsonData.getJSONObject("pr").getString("pr_long")
            val budget = jsonData.getString("budget").plusSuffixNotBlank("円")
                .takeIf { it.isNotBlank() } ?: getString(R.string.no_info)
            val card = jsonData.getString("credit_card")
                .takeIf { it.isNotBlank() } ?: getString(R.string.no_info)
            val eMoneyStr = jsonData.getString("e_money")
                .takeIf { it.isNotBlank() } ?: getString(R.string.no_info)
            //endregion

            //region Set data to View
            tvStoreName.putText(storeName)
            setImage(GetImageBitmap().execute(img1Url).get(), ivImage1)
            setImage(GetImageBitmap().execute(img2Url).get(), ivImage2)
            tvBusinessHours.putText(openTime)
            tvAddress.putText(address)
            tvAccessDetail.putText(accessStr)
            tvTEL.putText(tel)
            tvFAX.putText(fax)
            tvCategory.putText(category)
            tvHoliday.putText(holiday)
            tvPRLong.putText(prLong)
            tvPR.visibility = if (prLong.isBlank()) View.GONE else View.VISIBLE
            tvBudget.text = budget
            tvCreditCard.text = card
            tvEMoney.text = eMoneyStr
            //endregion
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetImageBitmap: AsyncTask<String, String, Bitmap?>() {
        override fun doInBackground(vararg params: String) = getImageByUrl(params[0]) // Functions.kt
    }

    private fun setImage(img: Bitmap?, view: ImageView) {
        if (img == null) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.setImageBitmap(Bitmap.createScaledBitmap(img, _imageSize, _imageSize, true))
        }
    }

    // Set string & change visibility
    private fun TextView.putText(txt: String) {
        if (txt.isBlank()) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
            this.text = txt
        }
    }

    private inner class OnButtonClickListener(private val data: String) : View.OnClickListener {
        override fun onClick(view: View) {
            // Prevention of repeated hits -> Reset by onStop() or CallConfirmDialogFragment()
            view.isEnabled = false
            when (view.id) {
                R.id.btCall -> goCall(view)
                R.id.btMobilePage -> goMobilePage()
                R.id.btToMap -> goToMap()
            }
        }

        private fun goCall(view: View) {
            val permissionCALL = Manifest.permission.CALL_PHONE
            if (ActivityCompat.checkSelfPermission(applicationContext,
                    permissionCALL) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(permissionCALL)
                ActivityCompat.requestPermissions(this@DetailsActivity, permissions, 2000)

                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this@DetailsActivity, permissionCALL)) {
                    Toast.makeText(
                        applicationContext, R.string.please_prms_phone, Toast.LENGTH_SHORT).show()
                }

                view.isEnabled = true
                return
            }

            CallConfirmDialogFragment(data, view)
                .show(supportFragmentManager, "call") // CallConfirmDialogFragment.kt
        }

        private fun goMobilePage() {
            val uri = Uri.parse(data)
            val intentPage = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intentPage)
        }

        private fun goToMap() {
            val uri = Uri.parse("geo:$data")
            val intentToMap = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intentToMap)
        }
    }

    // Processing for Permission Dialog
    override fun onRequestPermissionsResult(requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 2000 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(applicationContext,
                getString(R.string.toast_retry), Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}