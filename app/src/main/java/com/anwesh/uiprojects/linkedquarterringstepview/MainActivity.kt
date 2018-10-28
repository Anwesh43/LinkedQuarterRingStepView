package com.anwesh.uiprojects.linkedquarterringstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.quarterringstepview.QuarterRingStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QuarterRingStepView.create(this)
    }
}
