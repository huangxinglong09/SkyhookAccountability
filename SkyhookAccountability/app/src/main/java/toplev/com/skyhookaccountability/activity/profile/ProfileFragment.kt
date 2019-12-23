package toplev.com.skyhookaccountability.activity.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_claim_list.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.view_profile_header.view.*

import toplev.com.skyhookaccountability.R
import toplev.com.skyhookaccountability.adapter.ClaimAdapter
import toplev.com.skyhookaccountability.activity.claim.ClaimDetailActivity
import toplev.com.skyhookaccountability.activity.main.MainActivity
import toplev.com.skyhookaccountability.model.Claim
import toplev.com.skyhookaccountability.support.App

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ProfileFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private var claimsList = ArrayList<Claim>()
    private lateinit var claimAdapter : ClaimAdapter
    lateinit var parentContext : MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentContext = context as MainActivity

    }


    override fun onStart() {
        super.onStart()

        //load users closed claims on profile
        loadClaims()

        //set header and data
        val headerView = layoutInflater.inflate(toplev.com.skyhookaccountability.R.layout.view_profile_header, null) as View
        headerView.fullNameTextView.text = App.shared!!.user.fullName
        headerView.emailTextView.text = App.shared!!.user.email

        closedClaimListView.addHeaderView(headerView)
        closedClaimListView.setOnItemClickListener { _, _, position, _ ->
            if(position>0){
                val intent = Intent(activity!!.applicationContext, ClaimDetailActivity::class.java)
                App.shared!!.selectedClaim = claimsList.get(position-1)
                startActivity(intent)
            }


        }

    }

    override fun onStop() {
        super.onStop()
    }

    fun loadClaims(){
        parentContext.showLoading(true)
        Claim().fetchClaims {
            if(it != null && it.size > 0){
                parentContext.runOnUiThread {
                    claimsList.clear()
                    parentContext.showLoading(false)
                    for(claim in it){
                        if(claim.status.equals("CLOSED"))  {
                            claimsList.add(claim)
                        }
                    }
                    claimAdapter = ClaimAdapter(activity!!.applicationContext,claimsList)
                    if (closedClaimListView != null) {
                        closedClaimListView.adapter = claimAdapter
                    }
                }

            } else {
                parentContext.runOnUiThread {
                    parentContext.showLoading(false)
                }
            }
        }
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
