package com.memerexample.calculator

import android.content.Context
import android.widget.Toast
import java.util.Stack // We'll need Stacks

class Operations {

    private fun precedence(operator: Char): Int {
        return when (operator) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> 0
        }
    }

    private fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0) throw ArithmeticException("Division by zero")
                a / b
            }
            else -> throw IllegalArgumentException("Invalid operator")
        }
    }

    fun calculateExpression(initialExpression: String, context: Context): String {
        var expression = initialExpression.replace(" ", "") // Remove spaces

        if (expression.isEmpty()) {
            Toast.makeText(context, "Input is empty", Toast.LENGTH_SHORT).show()
            return "Error: Empty"
        }

        while (expression.contains('%')) {
            val percentIndex = expression.lastIndexOf('%')
            if (percentIndex == 0) {
                Toast.makeText(context, "Error: Misplaced %", Toast.LENGTH_SHORT).show()
                return "Error: Misplaced %"
            }

            var startIndex = -1
            for (i in percentIndex - 1 downTo 0) {
                val char = expression[i]
                if (char.isDigit() || char == '.' || (char == '-' && (i == 0 || !expression[i-1].isDigit() && expression[i-1] != '.' && expression[i-1] != ')'))) { // Allow leading minus for numbers
                    startIndex = i
                } else {
                    if (startIndex != -1) break
                }
            }

            if (startIndex == -1 && percentIndex > 0 && (expression[percentIndex-1].isDigit() || expression[percentIndex-1] == '.' || (expression[percentIndex-1] == '-' && (percentIndex-1 == 0 || !expression[percentIndex-2].isDigit() && expression[percentIndex-2] != '.' && expression[percentIndex-2] != ')')))) {
                startIndex = percentIndex -1
            }

            if (startIndex == -1) {
                Toast.makeText(context, "Error: % needs a valid number before it", Toast.LENGTH_SHORT).show()
                return "Error: % format"
            }

            val numberString = expression.substring(startIndex, percentIndex)

            try {
                val numberValue = numberString.toDouble()
                val percentResult = numberValue / 100.0
                expression = expression.substring(0, startIndex) +
                        (if (percentResult < 0) "(0${percentResult})" else percentResult.toString()) +
                        expression.substring(percentIndex + 1)
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Error: Invalid number for % ('$numberString')", Toast.LENGTH_SHORT).show()
                return "Error: % num"
            }
        }
        try {
            val values = Stack<Double>()
            val ops = Stack<Char>()

            var i = 0
            while (i < expression.length) {
                val char = expression[i]

                if (char.isDigit() || char == '.') {

                    val sb = StringBuilder()

                    if (char == '.' || char.isDigit()) {
                        if (i > 0 && expression[i-1] == '-' && (i-1 == 0 || "+-*/(".contains(expression[i-2]))) {
                        }
                        sb.append(char)
                        i++
                        while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                            sb.append(expression[i])
                            i++
                        }
                        values.push(sb.toString().toDouble())
                        i--
                    }
                } else if (char == '(') {
                    ops.push(char)
                } else if (char == ')') {
                    while (ops.isNotEmpty() && ops.peek() != '(') {
                        if (values.size < 2) {
                            Toast.makeText(context, "Error: Invalid expression (operand missing)", Toast.LENGTH_SHORT).show()
                            return "Error: Operand missing"
                        }
                        values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                    }
                    if (ops.isEmpty()) {
                        Toast.makeText(context, "Error: Mismatched parentheses", Toast.LENGTH_SHORT).show()
                        return "Error: Mismatched )"
                    }
                    ops.pop()
                } else if ("+-*/".contains(char)) {
                    if (char == '-' && (i == 0 || "+-*/(".contains(expression[i-1]))) {

                        val sb = StringBuilder()

                        sb.append('-')
                        i++
                        if (i >= expression.length || (!expression[i].isDigit() && expression[i] != '.' && expression[i] != '(') ) {
                            Toast.makeText(context, "Error: Invalid use of unary minus", Toast.LENGTH_SHORT).show()
                            return "Error: Unary -"
                        }
                        if (expression[i] == '(') {
                            values.push(-1.0)
                            ops.push('*')
                            ops.push('(')
                        } else {
                            while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                                sb.append(expression[i])
                                i++
                            }
                            values.push(sb.toString().toDouble())
                            i--
                        }
                    } else {
                        while (ops.isNotEmpty() && ops.peek() != '(' && precedence(ops.peek()) >= precedence(char)) {
                            if (values.size < 2) {
                                Toast.makeText(context, "Error: Invalid expression (operand missing for op)", Toast.LENGTH_SHORT).show()
                                return "Error: Operand missing for op"
                            }
                            values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                        }
                        ops.push(char)
                    }
                } else {
                    Toast.makeText(context, "Error: Invalid character '$char'", Toast.LENGTH_SHORT).show()
                    return "Error: Invalid char"
                }
                i++
            }
            while (ops.isNotEmpty()) {
                if (ops.peek() == '(') {
                    Toast.makeText(context, "Error: Mismatched parentheses", Toast.LENGTH_SHORT).show()
                    return "Error: Mismatched ("
                }
                if (values.size < 2) {
                    Toast.makeText(context, "Error: Invalid expression (final operand missing)", Toast.LENGTH_SHORT).show()
                    return "Error: Final operand missing"
                }
                values.push(applyOp(ops.pop(), values.pop(), values.pop()))
            }

            if (values.size != 1) {
                Toast.makeText(context, "Error: Invalid expression (malformed)", Toast.LENGTH_SHORT).show()
                return "Error: Malformed expr"
            }

            val resultValue = values.pop()

            return if (resultValue.isInfinite() || resultValue.isNaN()) {
                Toast.makeText(context, "Error: Result is infinity or NaN", Toast.LENGTH_SHORT).show()
                "Error: Math error"
            } else if (resultValue % 1.0 == 0.0) {
                resultValue.toLong().toString()
            } else {
                String.format("%.4f", resultValue).trimEnd('0').trimEnd('.')
            }

        } catch (e: ArithmeticException) {
            Toast.makeText(context, "Error: Division by zero!", Toast.LENGTH_SHORT).show()
            return "Error: Div by 0"
        } catch (e: Exception) {
            Toast.makeText(context, "Error: Calculation error (${e.message})", Toast.LENGTH_SHORT).show()
            return "Error: Calc"
        }
    }
}