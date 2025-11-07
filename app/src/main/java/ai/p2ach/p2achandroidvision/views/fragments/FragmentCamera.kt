package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.viewmodels.TestViewModel
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCamera : BaseFragment<FragmentCameraBinding>(){

    private val testViewModel : TestViewModel by viewModel()


    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        CoroutineScope(Dispatchers.IO).async {
            testViewModel.test()
        }



    }


}