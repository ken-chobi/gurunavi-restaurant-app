package kenchovi.androiddev.gnavirestaurants

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_menu_result.*
import kotlinx.android.synthetic.main.footer.*
import kotlinx.android.synthetic.main.header.*
import org.json.JSONObject
import java.lang.Exception
import java.net.SocketTimeoutException

class MenuResultActivity : AppCompatActivity() {

    private val _imageSize = 150 // size of thumbnail image

    override fun onStop() {
        super.onStop()
        lvMenu.isEnabled = true
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_result)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //region Set the header & footer of lvMenu: ListView
        val header = LayoutInflater.from(applicationContext).inflate(R.layout.header, null)
        header.setOnClickListener {}
        lvMenu.addHeaderView(header)
        val footer = LayoutInflater.from(applicationContext).inflate(R.layout.footer, null)
        footer.setOnClickListener {}
        lvMenu.addFooterView(footer)
        //endregion

        // setting of lvMenu: ListView
        setListViewElements(intent.getStringExtra("json")!!)
        val totalHits = intent.getIntExtra("total", 0)

        // Setting Views of header
        tvLimit.text = intent.getStringExtra("limit")
        tvNumber.text = totalHits.toString()

        //region Setting Views of footer
        btnEnabledManager(totalHits)
        btPrevious.setOnClickListener {
            if (offsetPage > 1) {
                offsetPage--
                DataReceiver().execute()
            }
            btnEnabledManager(totalHits)
        }
        btNext.setOnClickListener {
            if (hitPerPage * offsetPage < totalHits) {
                offsetPage++
                DataReceiver().execute()
            }
            btnEnabledManager(totalHits)
        }
        //endregion
    }

    private fun btnEnabledManager(totalHits: Int) {
        btPrevious.isEnabled = (offsetPage > 1)
        btNext.isEnabled = (hitPerPage * offsetPage < totalHits)
        tvPage.text = offsetPage.toString()
    }

    private fun setListViewElements(jsonData: String) {
        // Setting of Adapter & Listener
        lvMenu.adapter = MyAdapter(applicationContext, R.layout.row, createMenuList(jsonData)) // MyListAdapter.kt
        lvMenu.onItemClickListener = ListItemClickListener(jsonData)
    }

    // Organize data for each list element
    private fun createMenuList(jsonData: String): List<Map<String, Any>> {
        val menuList = mutableListOf<Map<String, Any>>()

        val restJSON = JSONObject(jsonData).getJSONArray("rest")

        (0 until restJSON.length()).forEach {
            val jsonObj = restJSON.getJSONObject(it)

            val name = jsonObj.getString("name")
            val prShort = jsonObj.getJSONObject("pr").getString("pr_short")
            val access = jsonObj.getJSONObject("access")
            val accessStr = access.getAccessStr()
            val imgUrlStr = jsonObj.getJSONObject("image_url").getString("shop_image1")
            val image = GetImage().execute(imgUrlStr).get()

            val map = mapOf("image" to image, "name" to name,
                "pr_short" to prShort, "access" to accessStr.removeSuffix(" "))
            menuList.add(map)
        }

        return menuList.toList()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetImage : AsyncTask<String, String, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap {
            val img = getImageByUrl(params[0]) // Functions.kt
            val image = img ?: BitmapFactory.decodeResource(resources, R.drawable.ic_menu_gallery)

            // return the same size
            return Bitmap.createScaledBitmap(image, _imageSize, _imageSize, true)
        }
    }

    private inner class ListItemClickListener(val jsonData: String) : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            if (position > 0) {
                // Prevention of repeated hits -> Reset by onStop()
                lvMenu.isEnabled = false

                val intentDetails = Intent(applicationContext, DetailsActivity::class.java)
                intentDetails.putExtra("json", jsonData)
                intentDetails.putExtra("position", position - 1)
                // Start of DetailsActivity
                startActivity(intentDetails)
            }
        }
    }

    // from Listener of btPrevious & btNext
    @SuppressLint("StaticFieldLeak")
    private inner class DataReceiver : AsyncTask<String, String, List<Any>>() {
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
            } else {
                // Recreate list
                setListViewElements(result[0] as String)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}