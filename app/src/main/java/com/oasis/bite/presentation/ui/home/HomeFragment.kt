package com.oasis.bite.presentation.ui.home
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.presentation.adapters.CategoryAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.oasis.bite.Category
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listaCategorias = listOf(
            Category("Ensaladas", R.drawable.ic_salad),
            Category("Pastas", R.drawable.ic_salad),
            Category("Postres", R.drawable.ic_salad)
        )

        // Configuración del RecyclerView horizontal
        binding.recyclerCategorias.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val adapter = CategoryAdapter(listaCategorias)
        binding.recyclerCategorias.adapter = adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
