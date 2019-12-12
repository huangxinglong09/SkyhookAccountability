package toplev.com.skyhookaccountability.adapter

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.model.Claim

class ClaimAdapter(private val context: Context,
                        private val dataSource: ArrayList<Claim>) : BaseAdapter() {


    private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        // Get view for row item
        val viewHolder: ViewHolder
        val rowView: View?

        if (view == null) {
            rowView = inflater.inflate(R.layout.cell_claim, viewGroup, false)
            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder

        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }

        val assignedDate = "Assigned: "+dataSource[position].claimDate
        viewHolder.date!!.text = assignedDate
        viewHolder.claimantName!!.text = dataSource[position].claimant.fullName
        viewHolder.insuredName!!.text = dataSource[position].insured.fullName
        viewHolder.claimNumber!!.text = dataSource[position].claimNumber
        viewHolder.status!!.text = dataSource[position].status
        if(dataSource[position].status.equals("CLOSED")){
            viewHolder.status.setBackgroundColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(context, R.color.green))))
        }

        return rowView!!
    }

    private class ViewHolder(view: View?) {
        val claimantName = view?.findViewById<TextView>(R.id.claimantNameTextView)
        val insuredName = view?.findViewById<TextView>(R.id.insuredNameTextView)
        val claimNumber = view?.findViewById<TextView>(R.id.claimNumberTextView)
        val date = view?.findViewById<TextView>(R.id.dateTextView)
        val status = view?.findViewById<TextView>(R.id.statusTextView)
    }
}


