package ai.p2ach.p2achandroidvision.base.activites

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/*
* 선언된 VB를 자동으로 inflate하여 자식에서의 Boilerplate Code 방지
* */

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(){

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vbClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>
        val inflateMethod = vbClass.getMethod("inflate", LayoutInflater::class.java)
        binding = inflateMethod.invoke(null, layoutInflater) as VB
        setContentView(binding.root)
        viewInit(savedInstanceState)

    }

    open fun viewInit(savedInstanceState: Bundle?){}
}