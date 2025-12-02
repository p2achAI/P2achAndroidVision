package ai.p2ach.p2achandroidvision.base.fragments

import ai.p2ach.p2achandroidvision.utils.Log
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB by autoCleared()
    protected val binding: VB
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vbClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0] as Class<VB>
        val inflate = vbClass.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        _binding = inflate.invoke(null, inflater, container, false) as VB
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewInit(savedInstanceState)
    }

    open fun viewInit(savedInstanceState: Bundle?) {}

    protected inline fun autoBinding(block: VB.() -> Unit) {
        binding.block()
    }

    fun navigate(
        @IdRes destinationId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null
    ) {
        findNavController().navigate(
            resId = destinationId,
            args = args,
            navOptions = navOptions
        )
    }

    fun navigate(deepLink: Uri) {
        findNavController().navigate(deepLink)
    }

    fun popBack() {
        findNavController().popBackStack()
    }

    fun popBackTo(
        @IdRes destinationId: Int,
        inclusive: Boolean = false
    ) {
        findNavController().popBackStack(destinationId, inclusive)
    }
}

fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T>(this)

class AutoClearedValue<T : Any>(fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { owner ->
            owner?.lifecycle?.addObserver(
                object : androidx.lifecycle.DefaultLifecycleObserver {
                    override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) {
                        _value = null
                    }
                }
            )
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: error("Binding accessed outside of view lifecycle.")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}