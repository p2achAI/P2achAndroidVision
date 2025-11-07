package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.viewmodels.MdmViewModel

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCamera : BaseFragment<FragmentCameraBinding>(){


    private val mdmViewModel : MdmViewModel by viewModel()


    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            mdmViewModel.data.collect {
                mdmEntity ->
                Log.d(mdmEntity)
            }
        }



    }


}