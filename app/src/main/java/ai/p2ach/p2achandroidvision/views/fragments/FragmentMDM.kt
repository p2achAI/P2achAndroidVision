package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.utils.toUiItems
import ai.p2ach.p2achandroidvision.viewmodels.MdmViewModel
import ai.p2ach.p2achandroidvision.views.fragments.adapters.MDMAdapter

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentMDM : BaseFragment<FragmentCameraBinding>(){


    private val mdmViewModel : MdmViewModel by viewModel()
    private val adapter : MDMAdapter by lazy { MDMAdapter() }


    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        autoBinding {

            rvMdm.adapter = adapter
            rvMdm.layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }

            viewLifecycleOwner.lifecycleScope.launch {
                mdmViewModel.data.collect {
                        mdmEntity ->
//                    Log.d(mdmEntity)
                    adapter.submitList(mdmEntity?.toUiItems())

                }
            }

        }



    }




}