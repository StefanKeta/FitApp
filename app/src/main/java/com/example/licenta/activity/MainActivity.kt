package com.example.licenta.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.licenta.R
import com.example.licenta.activity.auth.LoginActivity
import com.example.licenta.data.LoggedUserData
import com.example.licenta.firebase.db.UsersDB
import com.example.licenta.fragment.main.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener,
    View.OnClickListener {
    private lateinit var navigationBar: BottomNavigationView
    private lateinit var fragmentLayout: FrameLayout
    private lateinit var addFab: FloatingActionButton
    private lateinit var fabLayout: LinearLayout
    private lateinit var addExerciseFab: FloatingActionButton
    private lateinit var addFoodFab: FloatingActionButton
    private lateinit var drawer: DrawerLayout
    private val rotateOpenAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.fab_rotate_open)
    }
    private val rotateCloseAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.fab_rotate_close)
    }
    private val fromBottomAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.fab_from_bottom)
    }
    private val toBottomAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.fab_to_bottom)
    }

    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
    }

    private fun initComponents() {
        createToolbar()
        navigationBar = findViewById(R.id.activity_main_navigation_view_bottom)
        fragmentLayout = findViewById(R.id.activity_main_fragment_layout)
        navigationBar.selectedItemId = R.id.menu_main_bottom_home
        navigationBar.setOnItemSelectedListener(this)
        fabLayout = findViewById(R.id.activity_main_fab_layout)
        addFab = findViewById(R.id.activity_main_fab_add)
        addFab.setOnClickListener(this)
        addFoodFab = findViewById(R.id.activity_main_fab_food)
        addFoodFab.setOnClickListener(this)
        addExerciseFab = findViewById(R.id.activity_main_fab_exercise)
        addExerciseFab.setOnClickListener(this)
        switchFragments(ActivityFragment())
    }

    private fun createToolbar() {
        val toolbar: Toolbar = findViewById(R.id.activity_main_nav_bar)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.activity_main_drawer_layout)
        val actionDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawer,
            toolbar,
            R.string.nav_drawer_open,
            R.string.nav_drawer_close
        )
        drawer.addDrawerListener(actionDrawerToggle)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        actionDrawerToggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_bottom_home -> switchFragments(ActivityFragment())
            R.id.menu_main_bottom_location -> switchFragments(MapsFragment())
            R.id.menu_main_bottom_diary -> switchFragments(DiaryFragment())
            R.id.menu_main_bottom_profile -> switchFragments(ProfileFragment())
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.activity_main_fab_add -> onAddFabClicked()
            R.id.activity_main_fab_food -> {
                onAddFabClicked()
                switchFragments(DiaryFragment())
            }
            R.id.activity_main_fab_exercise -> {
                onAddFabClicked()
                switchFragments(DiaryFragment(DiaryFragment.EXERCISE_FRAGMENT_CODE))
            }
        }
    }

    private fun switchFragments(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.activity_main_fragment_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun onAddFabClicked() {
        setButtonsVisibility(clicked)
        setButtonsAnimation(clicked)
        clicked = !clicked
    }

    private fun setButtonsVisibility(clicked: Boolean) {
        if (!clicked) {
            fabLayout.visibility = View.VISIBLE
        } else {
            fabLayout.visibility = View.GONE
        }
    }

    private fun setButtonsAnimation(clicked: Boolean) {
        if (!clicked) {
            addExerciseFab.startAnimation(fromBottomAnim)
            addFoodFab.startAnimation(fromBottomAnim)
            addFab.startAnimation(rotateOpenAnim)
        } else {
            addExerciseFab.startAnimation(toBottomAnim)
            addFoodFab.startAnimation(toBottomAnim)
            addFab.startAnimation(rotateCloseAnim)
        }

    }

    override fun onStart() {
        super.onStart()
        val auth = FirebaseAuth.getInstance()
        when {
            auth.currentUser == null -> {
                Toast.makeText(
                    this@MainActivity,
                    "You are not logged in, signing out",
                    Toast.LENGTH_SHORT
                )
                    .show()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            auth.currentUser != null -> {
                val id = auth.currentUser!!.uid
                UsersDB
                    .getUser(id) {
                        if (it != null)
                            LoggedUserData.setLoggedUser(it)
                    }
            }
            else -> {
                Toast.makeText(this@MainActivity, "Invalid user, signing out", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}