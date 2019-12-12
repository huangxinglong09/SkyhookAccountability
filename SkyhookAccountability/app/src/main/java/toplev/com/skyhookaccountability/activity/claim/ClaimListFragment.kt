package toplev.com.skyhookaccountability.activity.claim

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.os.HandlerCompat.postDelayed
import kotlinx.android.synthetic.main.activity_begin_auth.*
import kotlinx.android.synthetic.main.activity_begin_auth.progressBar
import kotlinx.android.synthetic.main.activity_claim_detail.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_claim_list.*
import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.adapter.ClaimAdapter
import toplev.com.skyhookaccountability.model.Claim
import toplev.com.skyhookaccountability.support.App
import java.io.Serializable
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ClaimListFragment.OnListFragmentInteractionListener] interface.
 */
class ClaimListFragment : Fragment() {

    // TODO: Customize parameters

    private var listener: OnListFragmentInteractionListener? = null

    var claimsList = ArrayList<Claim>()
    lateinit var runnable: Runnable
    lateinit var claimAdapter : ClaimAdapter
    lateinit var parentContext : MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(toplev.com.skyhookaccountability.R.layout.fragment_claim_list, container, false)


        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentContext = context as MainActivity

        //weird android bug handling..
        parentContext.toolbarTitleTextView.text = "My Claims"

        claimListView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(activity!!.applicationContext, ClaimDetailActivity::class.java)
//            intent.putExtra("CLAIM", claimsList.get(position) as Serializable)
            App.shared!!.selectedClaim = claimsList.get(position)
            startActivity(intent)
        }

    }


    fun loadClaims(){
        parentContext.showLoading(true)
        Claim().fetchClaims {
            if(it != null && it.size > 0){
                parentContext.runOnUiThread {
                    claimsList.clear()
                    parentContext.showLoading(false)
                    for(claim in it){
                       if(!claim.status.equals("CLOSED"))  {
                           claimsList.add(claim)
                       }
                    }

                    //sort most recent default
                    Collections.sort(claimsList, object : Comparator<Claim> {
                        override fun compare(o1: Claim, o2: Claim): Int {
                            return o2.claimDate.compareTo(o1.claimDate)
                        }
                    })

                    if(claimListView != null){
                        claimAdapter = ClaimAdapter(activity!!.applicationContext,claimsList)
                        claimListView.adapter = claimAdapter
                    }

                }

            }
        }
    }

    override fun onStart() {
        super.onStart()

        //load claims for this user
        loadClaims()
    }





    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: Claim?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ClaimListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
