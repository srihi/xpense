package com.morcinek.xpense.home.statistics.charts.week

import com.morcinek.xpense.data.category.Category
import com.morcinek.xpense.data.expense.Expense
import com.morcinek.xpense.home.statistics.charts.ChartDataGenerator
import com.morcinek.xpense.home.statistics.charts.iterateWeek
import lecho.lib.hellocharts.model.*
import java.util.*

/**
 * Copyright 2016 Tomasz Morcinek. All rights reserved.
 */
internal class LineDataGenerator(val range: Iterable<Int>) : ChartDataGenerator {

    override fun generateData(expenses: List<Expense>, selectedCategories: List<Category>): AbstractChartData {
        val data = LineChartData(createLines(expenses, selectedCategories))
        data.axisXBottom = Axis(lineXValues()).setHasLines(true)
        data.axisYLeft = Axis().setHasLines(true)
        return data

    }

    private fun createLines(expenses: List<Expense>, selectedCategories: List<Category>): ArrayList<Line> {
        val lines = arrayListOf<Line>()
        val categoriesExpenses = expenses.groupBy { it.category!! }
        for (category in selectedCategories) {
            lines.add(createLine(category.color!!, categoriesExpenses[category]!!))
        }
        return lines
    }

    private fun createLine(color: Int, expenses: List<Expense>): Line {
        val line = Line(pointValues(expenses))
        line.setColor(color)
        return line
    }

    private fun pointValues(expenses: List<Expense>): ArrayList<PointValue> {
        val values = arrayListOf<PointValue>()
        iterateWeek(expenses, range) { index, day, value ->
            values.add(PointValue(day.toFloat(), value))
        }
        return values
    }

    private fun lineXValues(): ArrayList<AxisValue> {
        val axisValues = arrayListOf<AxisValue>()
        for (week in range) {
            axisValues.add(AxisValue(week.toFloat()).setLabel(week.toString()))
        }
        return axisValues
    }
}
