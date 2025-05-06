package com.memerexample.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.memerexample.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var operationsLogic: Operations
    private lateinit var binding: ActivityMainBinding

    private var openParenthesesCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        operationsLogic = Operations()

        fun appendToSummary(charToAppend: String) {
            binding.summary.append(charToAppend)
        }

        //--- Numbers
        binding.button0.setOnClickListener { appendToSummary("0") }
        binding.button1.setOnClickListener { appendToSummary("1") }
        binding.button2.setOnClickListener { appendToSummary("2") }
        binding.button3.setOnClickListener { appendToSummary("3") }
        binding.button4.setOnClickListener { appendToSummary("4") }
        binding.button5.setOnClickListener { appendToSummary("5") }
        binding.button6.setOnClickListener { appendToSummary("6") }
        binding.button7.setOnClickListener { appendToSummary("7") }
        binding.button8.setOnClickListener { appendToSummary("8") }
        binding.button9.setOnClickListener { appendToSummary("9") }
        binding.buttonDot.setOnClickListener { appendToSummary(".") }

        //--- Operations
        binding.buttonPercent.setOnClickListener { appendToSummary("%") }
        binding.buttonPlus.setOnClickListener { appendToSummary("+") }
        binding.buttonMinus.setOnClickListener { appendToSummary("-") }
        binding.buttonMult.setOnClickListener { appendToSummary("*") }
        binding.buttonDiv.setOnClickListener { appendToSummary("/") }
        binding.buttonParentheses.setOnClickListener {
            val currentText = binding.summary.text.toString()
            val lastChar: Char? = if (currentText.isNotEmpty()) currentText.last() else null

            if (openParenthesesCount > 0 && (lastChar?.isDigit() == true || lastChar == ')')) {
                appendToSummary(")")
                openParenthesesCount--
            } else {
                appendToSummary("(")
                openParenthesesCount++
            }
        }

        //--- Clear and back
        binding.buttonDel.setOnClickListener {
            binding.summary.setText("")
            openParenthesesCount = 0
        }
        binding.buttonBack.setOnClickListener {
            val currentText = binding.summary.text.toString()
            if (currentText.isNotEmpty()) {
                val charRemoved = currentText.last()
                binding.summary.setText(currentText.substring(0, currentText.length - 1))
                if (charRemoved == '(') {
                    if (openParenthesesCount > 0) {
                        openParenthesesCount--
                    }
                } else if (charRemoved == ')') {
                    openParenthesesCount++
                }
            }
        }

        //--- Equals
        binding.buttonEquals.setOnClickListener {
            val expressionFromInput = binding.summary.text.toString()
            if (expressionFromInput.isNotEmpty()) {
                val resultString = operationsLogic.calculateExpression(expressionFromInput, this)
                binding.summary.setText(resultString)
                // Consider resetting openParenthesesCount here if a calculation clears state
                // openParenthesesCount = 0
            } else {
                binding.summary.setText("")
                Toast.makeText(this, "Input is empty", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}