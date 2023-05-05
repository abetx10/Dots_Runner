package com.example.pointsrunner.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.pointsrunner.databinding.FragmentMainBinding

class MainFragment : Fragment(), OnGameOverListener {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var circleView: CircleView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        val lineView = createLineView()
        binding.gameContainer.addView(lineView)

        circleView = createCircleView(lineView)
        binding.gameContainer.addView(circleView)
        circleView.setScoreTextView(binding.scoreValueText)

        circleView.setOnGameOverListener(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                circleView.toggleRotationDirection()
                circleView.performClick()
            }
            true
        }
    }

    private fun createLineView(): LineView {
        return LineView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private fun createCircleView(lineView: LineView): CircleView {
        return CircleView(requireContext(), lineView).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private fun showRestartButton() {
        binding.restartButton.apply {
            visibility = View.VISIBLE
            setOnClickListener { restartGame() }
        }
    }

    private fun restartGame() {
        restartApp()
    }

    private fun restartApp() {
        activity?.apply {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onGameOver() {
        showRestartButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}