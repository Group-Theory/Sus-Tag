package com.grouptheory.sus_tag

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.grouptheory.sus_tag.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var binding: ActivityMainBinding
	private lateinit var alertBuilder: NotificationCompat.Builder
	private var alertCounter: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Required to serve notifications
		createNotificationChannel()

		// Builder used to alert user of tracker (w/ notification)
		createBuilder()
		
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)

		val navController = findNavController(R.id.nav_host_fragment_content_main)
		appBarConfiguration = AppBarConfiguration(navController.graph)
		setupActionBarWithNavController(navController, appBarConfiguration)

		binding.fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		}

		val motionDetector = MotionDetector(this)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return when (item.itemId) {
			R.id.action_settings -> true
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment_content_main)
		return navController.navigateUp(appBarConfiguration)
				|| super.onSupportNavigateUp()
	}

	private fun createNotificationChannel() {
		// Builds a notification channel used for tracker detection alerts
		// Only on API 26+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationId = getString(R.string.channel_id)
			val name = getString(R.string.channel_name)
			val desc = getString(R.string.channel_desc)
			val priority = NotificationManager.IMPORTANCE_HIGH
			val channel = NotificationChannel(notificationId, name, priority)
					.apply { description = desc }

			// After building notification, register it with the OS
			val notificationManager: NotificationManager =
				getSystemService(Context.NOTIFICATION_SERVICE)
					as NotificationManager

			notificationManager.createNotificationChannel(channel)
		}
	}

	private fun createBuilder() {
		alertBuilder = NotificationCompat.Builder(
				this, getString(R.string.channel_id)
		)
				.setSmallIcon(R.drawable.ic_notification_alert_24)
				.setContentTitle(getString(R.string.channel_name))
				.setContentText(getString(R.string.channel_desc))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
	}

	private fun notifyUser() {
		with(NotificationManagerCompat.from(this)) {
			notify(alertCounter, alertBuilder.build())
		}

		alertCounter++
	}
}