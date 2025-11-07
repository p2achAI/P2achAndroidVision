package ai.p2ach.p2achandroidvision.viewmodels

import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.repos.MDMRepo
import ai.p2ach.p2achandroidvision.repos.MDMSettingDAO
import androidx.lifecycle.ViewModel

class TestViewModel(private val repo : MDMRepo) : ViewModel() {


    suspend fun test(){
        repo.test()
    }

}