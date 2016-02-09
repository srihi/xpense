package com.morcinek.xpense.home.history.period

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import com.morcinek.xpense.Application
import com.morcinek.xpense.R
import com.morcinek.xpense.common.BaseFragment
import com.morcinek.xpense.common.adapter.AbstractRecyclerViewAdapter
import com.morcinek.xpense.common.recyclerview.DividerItemDecoration
import com.morcinek.xpense.common.utils.*
import com.morcinek.xpense.data.expense.Expense
import com.morcinek.xpense.data.expense.ExpenseManager
import com.morcinek.xpense.data.note.ExpenseAction
import com.morcinek.xpense.expense.ExpenseActivity
import com.morcinek.xpense.home.history.period.model.Period
import com.morcinek.xpense.home.history.period.model.PeriodObject
import kotlinx.android.synthetic.main.default_list.*
import javax.inject.Inject

/**
 * Copyright 2016 Tomasz Morcinek. All rights reserved.
 */
class PeriodFragment : BaseFragment, AbstractRecyclerViewAdapter.OnItemClickListener<Expense> {

    override fun getLayoutResourceId() = R.layout.default_list

    private val periodFilterFactory = PeriodFilterFactory()

    private val periodObject: PeriodObject by lazy {
        periodFilterFactory.getPeriodFilter(arguments.getSerializable<Period>()!!)
    }

    @Inject
    lateinit var expenseManager: ExpenseManager

    private lateinit var periodAdapter: PeriodAdapter

    fun getTitle() = periodObject.titleResource

    constructor() {
    }

    constructor(period: Period) {
        arguments = Bundle()
        arguments.putSerializable(period)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity.application as Application).component.inject(this)

        setupAdapter()
        setupRecyclerView()
    }

    private fun setupAdapter() {
        periodAdapter = PeriodAdapter(activity)
        periodAdapter.setList(expenseManager.getExpenses().filter(periodObject.filter))
        periodAdapter.itemClickListener = this
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = periodAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutAnimation = LayoutAnimationController(createLayoutAnimation())
        recyclerView.addItemDecoration(DividerItemDecoration(activity, R.drawable.item_divider, true, true))
    }

    private fun createLayoutAnimation() = AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left)

    override fun onItemClicked(item: Expense) {
        activity.startActivityFromFragment<ExpenseActivity>(this, item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val expenses = expenseManager.getExpenses()
            val action = data!!.getSerializableExtra<ExpenseAction>()!!
            val expense = data.getParcelableExtra<Expense>()
            periodAdapter.updateList(expenses, expense, action)
        }
    }
}