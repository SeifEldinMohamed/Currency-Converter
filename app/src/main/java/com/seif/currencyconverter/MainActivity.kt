package com.seif.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {
    var BaseCurrency = "EUR"
    var ConvertedToCurrency = "USD"
    var conversionRate = 0f
    var currencyName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SpinnerSetup()
        TextChange()
    }

    private fun TextChange() {
        edit_firstConversion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                try {
                    getApiResult()
                } catch (e: Exception) {
                    Log.d("main", "${e.message}")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("main", "on Text changed")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("main", "Before Text changed")
            }

        })

    }

    // make a function that setup the two spinners
    private fun SpinnerSetup() {
        val spinner: Spinner = findViewById(R.id.spinner_firstConversion)
        val spinner2: Spinner = findViewById(R.id.spinner_secondConversion)

        ArrayAdapter.createFromResource(
                this,
                R.array.currencies,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        ArrayAdapter.createFromResource(
                this,
                R.array.currencies2,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }
        // for the base currency
        spinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            // when the user pick a currency it's going to update the Ui and change the currency
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                BaseCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })
        // for the converted currency
        spinner2.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            // when the user pick a currency it's going to update the Ui and change the currency
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ConvertedToCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })
    }

    // make a function that gets the api result
    private fun getApiResult() {
        if (edit_firstConversion != null && edit_firstConversion.text.isNotEmpty()
                && edit_firstConversion.text.isNotBlank()) {
            // add api url
           // val APi = "https://api.ratesapi.io/api/latest?base=$BaseCurrency&symbols=$ConvertedToCurrency"
            val Api = "https://v6.exchangerate-api.com/v6/6e9cb6f457854c2f4b20c09b/latest/$BaseCurrency"
            val CurrencyNameApi = "https://api.vatcomply.com/currencies"
            if (BaseCurrency == ConvertedToCurrency) {
                Toast.makeText(this, "cannot convert the same currency!", Toast.LENGTH_LONG).show()
            } else { // get api data using coroutines
                // I gonna to add Dispatchers.IO bec: it gonna to be retrieving data
                // and this is the one we use for retrieving data.
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        // takes url from internet and it reads it then gives us the information each time i call it.
                        val apiResult = URL(Api).readText()
                        val jsonObject = JSONObject(apiResult)

                        val apiResult2 = URL(CurrencyNameApi).readText()
                        val jsonObject2 = JSONObject(apiResult2)

                        conversionRate = jsonObject.getJSONObject("conversion_rates").getString(ConvertedToCurrency).toFloat()
                        Log.d("main", "conversion rate : $conversionRate")
                        Log.d("main", "api result = $apiResult")
                        Log.d("main", "conversion rate : $ConvertedToCurrency")

                        if (ConvertedToCurrency == "EGP"){
                            currencyName = ""

                        }
                        else{
                            currencyName = jsonObject2.getJSONObject(ConvertedToCurrency).getString("name").toString()

                        }


                        // to update the Ui
                        withContext(Dispatchers.Main) {
                            val text = ((edit_firstConversion.text.toString().toFloat()) * conversionRate).toString()
                            edit_secondConversion.setText(text)
                                txt_currencyName.text = "$text $currencyName"


                        }

                    } catch (e: Exception) {
                        Log.d("main", "${e.message}")
                    }
                }
            }
        }
    }
}