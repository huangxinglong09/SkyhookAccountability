package toplev.com.skyhookaccountability.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.cell_new_activity.view.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.model.Activity
import toplev.com.skyhookaccountability.model.SectionItem


class NewActivityItemAdapter(context:Context, items:ArrayList<SectionItem>) :
    BaseAdapter() {

    val context:Context = context
    val items:ArrayList<SectionItem> = items

    //view holder is used to prevent findViewById calls
    private class ActivityViewHolder {
        internal var title: TextView? = null
    }

    override fun getItemId(position: Int): Long {
        return -1
    }
    override fun getItem(position: Int): Any {
        return(items[position])
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getViewTypeCount(): Int {
        return 2 // The number of distinct view types the getView() will return.
    }

    override fun getItemViewType(position: Int): Int {
        if(items.get(position).isHeader){
            return 1 //is header
        } else {
            return 0
        }

    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val viewHolder: ActivityViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(toplev.com.skyhookaccountability.R.layout.cell_new_activity, viewGroup, false)

            viewHolder = ActivityViewHolder()

            viewHolder.title = view.findViewById<TextView>(R.id.activityNameTextView)

            view.tag = viewHolder

        }  else {

            viewHolder = view.tag as ActivityViewHolder

        }

        viewHolder.title!!.typeface = Typeface.DEFAULT
        viewHolder.title!!.textSize = 19.0f
        if (getItemViewType(i) == 1 ){
            viewHolder.title!!.textSize = 17.0f
            viewHolder.title!!.typeface = Typeface.DEFAULT_BOLD
        }
        viewHolder.title!!.text = items.get(i).title

        return view!!

    }

}

