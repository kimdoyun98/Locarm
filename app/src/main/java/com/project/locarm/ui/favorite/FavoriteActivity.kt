package com.project.locarm.ui.favorite

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.locarm.R
import com.project.locarm.databinding.ActivityFavoriteBinding
import com.project.locarm.ui.favorite.adapter.FavoritesAdapter
import com.project.locarm.ui.main.MainActivity.Companion.SELECT

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private val viewModel: FavoriteViewModel by viewModels { FavoriteViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowInset()

        binding.lifecycleOwner = this@FavoriteActivity
        binding.viewModel = viewModel

        initToolbarNavigationButton()
        initFavorites()
        selectFavoriteItem()
    }

    private fun initFavorites() {
        val adapter = FavoritesAdapter(
            lifecycleOwner = this,
            favoriteViewModel = viewModel,
        )
        
        binding.favorites.adapter = adapter
        viewModel.favoriteList.observe(this) {
            adapter.setData(it)
        }
    }

    private fun selectFavoriteItem() {
        viewModel.selectDestinationFavorite.observe(this) {
            if (it != null) {
                intent.apply {
                    putExtra(SELECT, it)
                    setResult(RESULT_OK, intent)
                }

                finish()
            }
        }
    }

    private fun initToolbarNavigationButton() {
        binding.favoriteToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun windowInset() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
