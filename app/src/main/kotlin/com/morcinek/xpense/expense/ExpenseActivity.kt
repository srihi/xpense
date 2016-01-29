package com.morcinek.xpense.expense

import android.os.Bundle
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
import com.morcinek.xpense.common.recyclerview.DividerItemDecoration
import com.morcinek.xpense.common.utils.*
import com.morcinek.xpense.common.utils.betterpickers.setCurrentNumberAsInteger
import com.morcinek.xpense.create.CreateActivity
import com.morcinek.xpense.create.Validator
import com.morcinek.xpense.data.expense.Expense
import com.morcinek.xpense.data.expense.ExpenseManager
import com.morcinek.xpense.data.note.ExpenseAction
import com.morcinek.xpense.expense.category.CategoryPickerDialogFragment
import com.morcinek.xpense.expense.note.NotePickerDialogFragment
import kotlinx.android.synthetic.main.expense.*
import org.jetbrains.anko.alert
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Tomasz Morcinek. All rights reserved.
 */
class ExpenseActivity : CreateActivity<Expense>(), AbstractRecyclerViewAdapter.OnItemClickListener<Int> {

    override var item: Expense
        get() = expenseAdapter.expense
        set(value) {
            expenseAdapter.expense = value
            expenseAdapter.notifyDataSetChanged()
        }

    override val validator: Validator<Expense> by lazy { ExpenseValidator() }

    private val isEditMode by lazy { intent.extras != null }

    @Inject
    lateinit var expenseManager: ExpenseManager

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expense)
        (application as Application).component.inject(this)

        setupAdapter()
        setupItem()
        setupRecyclerView()
        setupExpense()
    }

    override fun restoreItem(bundle: Bundle) = bundle.getParcelable<Expense>()

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.findItem(R.id.action_delete).setVisible(isEditMode)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                showConfirmationDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showConfirmationDialog() {
        alert(R.string.expense_delete_message) {
            positiveButton(R.string.yes) { deleteExpense() }
            negativeButton(R.string.no)
        }.show()
    }

    private fun deleteExpense() {
        expenseManager.deleteExpense(item)
        intent.putSerializableExtra(ExpenseAction.DELETED)
        intent.putParcelableExtra(item)
        finishOk()
    }

    private fun setupAdapter() {
        expenseAdapter = ExpenseAdapter(this)
        expenseAdapter.currencySymbol = expenseManager.currentProject.currency
        expenseAdapter.itemClickListener = this
    }

    private fun setupItem() {
        item = Expense()
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = expenseAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(context = this, showLast = true))
    }

    private fun setupExpense() {
        if (isEditMode) {
            setTitle(R.string.expense_edit_label)
        }
    }

    override fun onDoneItemSelected() {
        if (isEditMode) {
            expenseManager.updateExpense(item)
            intent.putSerializableExtra(ExpenseAction.UPDATED)
        } else {
            expenseManager.addExpense(item)
            intent.putSerializableExtra(ExpenseAction.CREATED)
        }
        intent.putParcelableExtra(item)
    }

    override fun onItemClicked(item: Int) {
        when (item) {
            AMOUNT_ITEM -> startAmountPicker()
            CATEGORY_ITEM -> startCategoryPicker()
            NOTE_ITEM -> startTextPicker()
            DATE_ITEM -> startDatePicker()
        }
    }

    private fun startAmountPicker() {
        val numberPickerBuilder = NumberPickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment)
                .setPlusMinusVisibility(View.GONE)
                .setLabelText(expenseManager.currentProject.currency)
                .setCurrentNumberAsInteger(item.value)
                .addNumberPickerDialogHandler { reference, number, decimal, isNegative, fullNumber ->
                    item.value = fullNumber
                    expenseAdapter.notifyDataItemChanged(AMOUNT_ITEM)
                    invalidateItem()
                }
        numberPickerBuilder.show()
    }

    private fun startCategoryPicker() {
        val textPickerFragment = CategoryPickerDialogFragment()
        textPickerFragment.selectedItem = item.category
        textPickerFragment.onItemSetListener = {
            item.category = it
            expenseAdapter.notifyDataItemChanged(CATEGORY_ITEM)
            invalidateItem()
        }
        textPickerFragment.show(supportFragmentManager)
    }

    private fun startTextPicker() {
        val textPickerFragment = NotePickerDialogFragment()
        textPickerFragment.selectedItem = item.note
        textPickerFragment.onItemSetListener = {
            item.note = it
            expenseAdapter.notifyDataItemChanged(NOTE_ITEM)
        }
        textPickerFragment.show(supportFragmentManager)
    }

    private fun startDatePicker() {
        val calendar = item.date
        val calendarDatePickerDialogFragment = CalendarDatePickerDialogFragment.newInstance(
                { dialogFragment, year, month, day ->
                    calendar.set(year, month, day)
                    expenseAdapter.notifyDataItemChanged(DATE_ITEM)
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        calendarDatePickerDialogFragment.setThemeCustom(R.style.BetterPickersTheme)
        calendarDatePickerDialogFragment.show(supportFragmentManager)
    }
}
