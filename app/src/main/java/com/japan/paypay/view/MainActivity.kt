package com.japan.paypay.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.japan.paypay.App
import com.japan.paypay.R
import com.japan.paypay.model.data.Currency
import com.japan.paypay.model.data.CurrencyAmount
import com.japan.paypay.utils.EspressoIdlingResource
import com.japan.paypay.utils.ExchangeRatesWorker
import com.japan.paypay.utils.Status
import com.japan.paypay.vm.MainActivityViewModel
import kotlinx.android.synthetic.main.content_controls.*
import kotlinx.android.synthetic.main.content_list.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var workerManager: WorkManager
    private lateinit var workRequest: PeriodicWorkRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //initialize dagger for view model
        val daggerComponent = (application as App).daggerComponent()
        daggerComponent.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)
        workerManager = WorkManager.getInstance(this)
        initUI()
        initObservers()
        initRefreshWorker()
    }

    private fun initUI() {
        btn_submit.setOnClickListener {
            if (edtxt_amt.text.toString() != "." && edtxt_amt.text.toString().isNotEmpty()) {
                val amt = edtxt_amt.text.toString().toDouble()
                val item = spn_currencies.selectedItem as Currency
                viewModel.calculate(amt, item)
            } else {
                Toast.makeText(this, "Please enter value", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initObservers() {
        observeData()
        observeCurrencies()
        observeExchangeRates()
    }

    private fun observeData() {
        //observe for Data
        viewModel.resource.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    enableControls(true)
                    stopProgress()
                    if (it.data == null || it.data.isEmpty()) {
                        showFeedBack("Empty Data")
                        return@Observer
                    }
                    showData(it.data)
                    EspressoIdlingResource.decrement()
                }

                Status.LOADING -> {
                    enableControls(false)
                    showProgress()
                    EspressoIdlingResource.increment()
                }
                Status.ERROR -> {
                    enableControls(true)
                    stopProgress()
                    showFeedBack(it.message ?: "an error occurred")
                    EspressoIdlingResource.decrement()
                }
            }
        })
    }

    private fun showProgress() {
        btn_submit.text = getString(R.string.progress)
    }

    private fun stopProgress() {
        btn_submit.text = getString(R.string.calculate)
    }

    private fun observeCurrencies() {
        //observe for Currencies
        viewModel.currencies.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data == null || it.data.isEmpty()) return@Observer
                    spn_currencies.adapter = CurrencySpinAdapter(this, ArrayList(it.data))
                }

                Status.LOADING -> {
                    Toast.makeText(this, "loading currencies", Toast.LENGTH_SHORT).show()
                }
                Status.ERROR -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun observeExchangeRates() {
        //observe for ExchangeRates
        viewModel.exchangeRates.observe(this, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    Toast.makeText(this, "exchange rates refreshed", Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {
                    Toast.makeText(this, "refreshing exchange rates", Toast.LENGTH_SHORT).show()
                }
                Status.ERROR -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun enableControls(enable: Boolean) {
        edtxt_amt.isEnabled = enable
        spn_currencies.isEnabled = enable
        btn_submit.isEnabled = enable
    }

    private fun showFeedBack(msg: String = "") {
        tv_feedback.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        if (msg.isNotEmpty())
            tv_feedback.text = msg
    }

    private fun showData(data: List<CurrencyAmount>) {
        recyclerView.visibility = View.VISIBLE
        tv_feedback.visibility = View.GONE
        tv_feedback.text = null
        if (recyclerView.adapter == null)
            recyclerView.adapter = RatesAdapter(this, ArrayList(data))
        else
            (recyclerView.adapter as RatesAdapter).update(data)
    }

    private fun initRefreshWorker() {
        workRequest =
            PeriodicWorkRequest.Builder(ExchangeRatesWorker::class.java, 30, TimeUnit.MINUTES)
                .addTag(ExchangeRatesWorker::class.java.simpleName)
                .build()
        workerManager.enqueueUniquePeriodicWork(
            ExchangeRatesWorker::class.java.simpleName,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun onStop() {
        if (::workerManager.isInitialized && ::workRequest.isInitialized)
            workerManager.cancelAllWorkByTag(ExchangeRatesWorker::class.java.simpleName)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (::workerManager.isInitialized && ::workRequest.isInitialized)
            workerManager.enqueue(workRequest)
    }

    @VisibleForTesting
    fun viewModel() = viewModel
}
