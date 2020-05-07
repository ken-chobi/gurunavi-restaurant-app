package kenchovi.androiddev.gnavirestaurants

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

var offsetPage = 1
const val hitPerPage = 15

fun getImageByUrl(url: String, timeout: Int = 500): Bitmap? {
    try {
        val con = URL(url).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.connectTimeout = timeout
        con.connect()
        val image = BitmapFactory.decodeStream(con.inputStream)
        con.disconnect()
        return image
    } catch (e: Exception) {}
    return null
}

fun JSONObject.getAccessStr(): String {
    var accessStr = ""
    listOf("line", "station", "station_exit", "walk").forEach {
        if (this.getString(it).isNotBlank()) accessStr += this.getString(it) + " "
    }

    accessStr = if (accessStr.isBlank()) "アクセス情報なし"
    else "アクセス:" + accessStr.removeSuffix(" ") + "分"

    return accessStr
}

fun JSONObject.accessNote(): String {
    val note = this.getString("note")
    return if (note.isBlank()) "" else "\r\n備考:$note"
}

fun String.plusTitleNotBlank(title: String) = if (this.isNotBlank()) title + this else this

fun String.plusSuffixNotBlank(str: String) = if (this.isNotBlank()) this + str else this

private fun urlElementsMap(): Map<String, String> {
    val lat = if (latitude < .0) 360.0 + latitude else latitude
    val lon = if (longitude < .0) 360.0 + longitude else longitude

    return mapOf("range" to searchRange.toString(),
        "lat" to lat.toString(), "lon" to lon.toString(),
        "name" to storeName, "offset_page" to offsetPage.toString(),
        "no_smoking" to noSmoking.toString(), "bottomless" to bottomlessCup.toString(),
        "with_pet" to withPet.toString(), "e_money" to eMoney.toString(),
        "buffet" to buffet.toString(), "private" to privateRoom.toString(),
        "takeout" to takeout.toString(), "midnight" to midnight.toString())
}

fun getJSONData(timeout: Int = 5000): String {
    var urlStr = "$GURUNAVI_API_URL?keyid=$GURUNAVI_KEY&hit_per_page=$hitPerPage"
    urlElementsMap().forEach {
        urlStr += "&" + when (it.key) {
            "name" -> "name"
            "lat", "latitude" -> "latitude"
            "lon", "longitude" -> "longitude"
            "range" -> "range"
            "offset" -> "offset"
            "offset_page" -> "offset_page"
            "no_smoking" -> "no_smoking"
            "bottomless", "bottomless_cup" -> "bottomless_cup"
            "with_pet", "pet" -> "with_pet"
            "e_money" -> "e_money"
            "buffet" -> "buffet"
            "private", "private_room" -> "private_room"
            "takeout" -> "takeout"
            "midnight" -> "midnight"
            else -> ""
        } + "=${it.value}"
    }

    val con = URL(urlStr).openConnection() as HttpURLConnection
    con.requestMethod = "GET"
    con.connectTimeout = timeout
    con.connect()

    val result = is2String(con.inputStream)
    con.disconnect()

    return result
}

fun is2String(stream: InputStream): String {
    val sb = StringBuilder()
    val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
    var line = reader.readLine()
    while (line != null) {
        sb.append(line)
        line = reader.readLine()
    }
    reader.close()
    return sb.toString()
}