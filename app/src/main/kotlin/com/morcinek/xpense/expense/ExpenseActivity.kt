package com.morcinek.xpense.expense

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder
import com.morcinek.xpense.Application
import com.morcinek.xpense.R
import com.morcinek.xpense.common.adapter.AbstractRecyclerViewAdapter
import com.morcinek.xpense.common.utils.betterpickers.setCurrentNumberAsInteger
import com.morcinek.xpense.expense.note.NotePickerDialogFragment
import com.morcinek.xpense.common.pickers.TextPickerDialogFragment
import com.morcinek.xpense.common.recyclerview.DividerItemDecoration
import com.morcinek.xpense.expense.common.model.Expense
import com.morcinek.xpense.expense.note.NoteAdapter
import com.morcinek.xpense.hint.HintProvider
import kotlinx.android.synthetic.main.expense.*
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Tomasz Morcinek. All rights reserved.
 */
class ExpenseActivity : AppCompatActivity(), AbstractRecyclerViewAdapter.OnItemClickListener<Int> {

    @Inject
    lateinit var hintProvider: HintProvider

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expense)
        (application as Application).component.inject(this)

        setupToolbar()
        setupAdapter()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_cancel)
    }

    private fun setupAdapter() {
        expenseAdapter = ExpenseAdapter(this)
        expenseAdapter.itemClickListener = this
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = expenseAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(context = this, showLast = true))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.expense, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_done -> {
                setResult(RESULT_OK)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(item: Int) {
        when (item) {
            R.string.title_amount -> startAmountPicker(expenseAdapter.expense)
            R.string.title_category -> startCategoryPicker(expenseAdapter.expense)
            R.string.title_note -> startTextPicker(expenseAdapter.expense)
            R.string.title_date -> startDatePicker(expenseAdapter.expense)
        }
    }

    private fun startAmountPicker(expense: Expense) {
        val numberPickerBuilder = NumberPickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment)
                .setPlusMinusVisibility(View.GONE)
                .setLabelText("$")
                .addNumberPickerDialogHandler { reference, number, decimal, isNegative, fullNumber ->
                    expense.value = fullNumber
                    expenseAdapter.notifyDataSetChanged()
                }
        numberPickerBuilder.setCurrentNumberAsInteger(expense.value)
        numberPickerBuilder.show()
    }

    private fun startCategoryPicker(expense: Expense) {
//        val textPickerFragment = TextPickerDialogFragment()
//        textPickerFragment.adapter = NoteAdapter(this)
//        textPickerFragment.items = hintProvider.provideNoteList()
//        textPickerFragment.text = expense.note
//        textPickerFragment.onTextSetListener = { textPickerFragment, text ->
//            expense.note = text
//            expenseAdapter.notifyDataSetChanged()
//        }
//        textPickerFragment.show(supportFragmentManager, TextPickerDialogFragment::class.java.name)
    }

    private fun startTextPicker(expense: Expense) {
        val textPickerFragment = NotePickerDialogFragment()
        textPickerFragment.adapter = NoteAdapter(this)
        textPickerFragment.items = hintProvider.provideNoteList()
        textPickerFragment.selectedItem = expense.note
        textPickerFragment.onTextSetListener = { textPickerFragment, text ->
            expense.note = text
            expenseAdapter.notifyDataSetChanged()
        }
        textPickerFragment.show(supportFragmentManager, TextPickerDialogFragment::class.java.name)
    }

    private fun startDatePicker(expense: Expense) {
        val calendar = expense.date
        val calendarDatePickerDialogFragment = CalendarDatePickerDialogFragment.newInstance(
                { dialogFragment, year, month, day ->
                    calendar.set(year, month, day)
                    expenseAdapter.notifyDataSetChanged()
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        calendarDatePickerDialogFragment.setThemeCustom(R.style.BetterPickersTheme)
        calendarDatePickerDialogFragment.show(supportFragmentManager, CalendarDatePickerDialogFragment::class.java.name)
    }
}
