package kenchovi.androiddev.gnavirestaurants

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

data class ViewHolder(val ivThumb: ImageView, val tvName: TextView,
                      val tvPRShort: TextView, val tvAccess: TextView)

class MyAdapter(context: Context?, layoutId: Int, menuList: List<Map<String, Any>>) : BaseAdapter() {

    private val _inflater = LayoutInflater.from(context)
    private val _id = layoutId
    private val _mapList = menuList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        var view = convertView

        // Set or get of ViewHolder
        if (view == null) {
            view = _inflater.inflate(_id, null) // _id = R.layout.row; row.xml
            val ivThumb = view.findViewById<ImageView>(R.id.ivThumbNail)
            val tvName = view.findViewById<TextView>(R.id.tvName)
            val tvAccess = view.findViewById<TextView>(R.id.tvAccess)
            val tvPRShort = view.findViewById<TextView>(R.id.tvPRShort)
            viewHolder = ViewHolder(ivThumb, tvName, tvPRShort, tvAccess)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        //region Set data for each View
        viewHolder.ivThumb.setImageBitmap(_mapList[position]["image"] as Bitmap)
        viewHolder.tvName.text = _mapList[position]["name"] as String
        val prShort = _mapList[position]["pr_short"] as String
        viewHolder.tvPRShort.text = prShort
        viewHolder.tvAccess.text = _mapList[position]["access"] as String
        viewHolder.tvPRShort.visibility = if (prShort.isBlank()) View.GONE else View.VISIBLE
        //endregion

        return view!!
    }

    override fun getCount(): Int {
        return _mapList.size
    }

    override fun getItem(position: Int): Any {
        return  position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}