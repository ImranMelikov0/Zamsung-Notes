package com.imranmelikov.zamsungnotes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class PasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val sharedPreferencesMain = getSharedPreferences("PreferencesMain", Context.MODE_PRIVATE)
        val editText = findViewById<EditText>(R.id.password_edittext)
        val confirmEditText = findViewById<EditText>(R.id.password_edittext2)
        val confirmButton = findViewById<Button>(R.id.password_button)
        val getIntent=intent
        if (getIntent!=null && getIntent.hasExtra("passwordReset")){
            val getString= getIntent.getStringExtra("passwordReset") as String
            if (getString == "reset"){
                editText.requestFocus()
                editText.postDelayed({
                    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }, 250)
                confirmButton.setOnClickListener {
                    val firstPasswordText=editText.text.toString().trim()
                    val secondPasswordText=confirmEditText.text.toString().trim()
                    if (firstPasswordText.isNotEmpty()&&secondPasswordText.isNotEmpty()){
                        if (firstPasswordText.length >= 4&&secondPasswordText.length>=4) {
                            if(firstPasswordText==secondPasswordText){
                                val editor = sharedPreferences.edit()
                                editor.putString("password",firstPasswordText)
                                editor.putBoolean("passwordEntered", true)
                                editor.apply()
                                val intent= Intent(this,MainActivity::class.java)
                                if (sharedPreferencesMain.getInt("Int",-100)==-1){
                                    intent.putExtra("All notes","All notes")
                                }else if (sharedPreferencesMain.getInt("Int",-100)==-3){
                                    intent.putExtra("lock","Locked notes")
                                }
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this,"Passwords do not match.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this,"Enter a password with at least 4 characters in it.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this,"Enter and confirm password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
            val passwordEntered = sharedPreferences.getBoolean("passwordEntered", false)
            if (passwordEntered){
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            editText.requestFocus()
            editText.postDelayed({
                val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 350)
            confirmButton.setOnClickListener {
                val firstPasswordText=editText.text.toString().trim()
                val secondPasswordText=confirmEditText.text.toString().trim()
                if (firstPasswordText.isNotEmpty()&&secondPasswordText.isNotEmpty()){
                    if (firstPasswordText.length >= 4&&secondPasswordText.length>=4) {
                        if(firstPasswordText==secondPasswordText){
                            val editor = sharedPreferences.edit()
                            editor.putString("password",firstPasswordText)
                            editor.putBoolean("passwordEntered", true)
                            editor.apply()
                            sharedPreferencesMain.edit().putInt("Int",-1).apply()
                            val intent= Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this,"Passwords do not match.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this,"Enter a password with at least 4 characters in it.",
                            Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this,"Enter and confirm password", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }


    override fun onBackPressed() {
        val getIntent=intent.getStringExtra("passwordReset") as String
        if (getIntent=="reset"){
            super.onBackPressed()
        }else{
            finish()
        }
    }
}