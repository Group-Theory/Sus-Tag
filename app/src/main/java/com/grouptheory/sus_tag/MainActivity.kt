package com.grouptheory.sus_tag

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grouptheory.sus_tag.databinding.ActivityMainBinding

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 1
private const val BLUETOOTH_SCAN_PERMISSION_CODE = 1
private const val BLUETOOTH_CONNECT_PERMISSION_CODE = 1

class MainActivity : AppCompatActivity() {

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var binding: ActivityMainBinding

	@RequiresApi(Build.VERSION_CODES.M)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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

		binding.button.setOnClickListener {
			startBleScan()
		}
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


	private val bluetoothAdapter: BluetoothAdapter by lazy {
		val bluetoothManager =
			getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
		bluetoothManager.adapter
	}

	private val bleScanner by lazy {
		bluetoothAdapter.bluetoothLeScanner
	}

	override fun onResume() {
		super.onResume()
		if (!bluetoothAdapter.isEnabled) {
			promptEnableBluetooth()
		}
	}

	private fun promptEnableBluetooth() {
		if (!bluetoothAdapter.isEnabled) {
			val enableBtIntent =
				Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
			startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			ENABLE_BLUETOOTH_REQUEST_CODE -> {
				if (resultCode != Activity.RESULT_OK) {
					promptEnableBluetooth()
				}
			}
		}
	}

	private val isLocationPermissionGranted
	get() =
		hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

	private val isScanPermissionGranted
	get() =
		hasPermission(Manifest.permission.BLUETOOTH_SCAN)

	private val isConnectPermissionGranted
	get() =
		hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

	private fun Context.hasPermission(permissionType: String): Boolean {
		return ContextCompat.checkSelfPermission(this,
			permissionType) == PackageManager.PERMISSION_GRANTED
	}

	@RequiresApi(Build.VERSION_CODES.M)
	private fun startBleScan() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
				!isLocationPermissionGranted) {
			requestLocationPermission()
		}
		else {
			Log.i("OUTPUT", "In else!")
			if (!isScanPermissionGranted) {
				requestPermissions(
					Array<String>(2){Manifest.permission.BLUETOOTH_SCAN},
					BLUETOOTH_SCAN_PERMISSION_CODE
				)
			}
			else {
				if (!isConnectPermissionGranted) {
					requestPermissions(
						Array<String>(2){Manifest.permission.BLUETOOTH_CONNECT},
						BLUETOOTH_CONNECT_PERMISSION_CODE
					)
				}
				bleScanner.startScan(List<ScanFilter>(1){filter}, scanSettings, scanCallback)
			}
		}

	}

	@RequiresApi(Build.VERSION_CODES.M)
	private fun requestLocationPermission() {
		if (isLocationPermissionGranted) {
			return
		}
		runOnUiThread {
			/*requestPermission(
				Manifest.permission.ACCESS_FINE_LOCATION,
				LOCATION_PERMISSION_REQUEST_CODE
			)*/
			requestPermissions(
				Array<String>(1){Manifest.permission.ACCESS_FINE_LOCATION},
				LOCATION_PERMISSION_REQUEST_CODE
			)
			/*requestPermission(
				Manifest.permission.ACCESS_COARSE_LOCATION,
				LOCATION_PERMISSION_REQUEST_CODE
			)
			requestPermission(
				Manifest.permission.BLUETOOTH_SCAN,
				ENABLE_BLUETOOTH_REQUEST_CODE)*/
		}
	}

	private fun Activity.requestPermission(permission: String,
	requestCode: Int) {
		ActivityCompat.requestPermissions(this,
		arrayOf(permission), requestCode)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(
			requestCode,
			permissions,
			grantResults
		)
		when (requestCode) {
			LOCATION_PERMISSION_REQUEST_CODE -> {
				if (grantResults.firstOrNull() ==
					PackageManager.PERMISSION_DENIED) {
						Log.println(Log.WARN, "Hi", "Anti cash money")
				} else {
					startBleScan()
				}
			}
		}
	}

	private val manData = ubyteArrayOf( 0x12U,0x19U,0x10U,0x74U,
		0xB8U,0xB8U,0xF7U,0x4FU,0xF3U,0x38U,0x99U,
		0xEDU,0x82U,0xEBU,0x6BU,0x61U,0xD3U,0x57U,
		0x2EU,0x4AU,0x92U,0xD0U,0xFAU,0xB8U,
		0x91U,0x01U,0x8AU).toByteArray()

	private val manData2 = ubyteArrayOf( 0x10U, 0x05U, 0x03U, 0x1CU,
		0xE9U, 0x00U, 0x04U).toByteArray()

	private val manData3 = ubyteArrayOf( 0x12U,0x19U,0x10U).toByteArray()

	val filter: ScanFilter = ScanFilter.Builder().setManufacturerData( 0x4C,
		manData3
	).build()

	val filter2: ScanFilter = ScanFilter.Builder().setDeviceAddress("EE:19:D2:91:D3:DB").build()


	private val scanSettings = ScanSettings.Builder()
		.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
		.build()

	private val scanCallback = object : ScanCallback() {
		override fun onScanResult(callbackType: Int, result:
		ScanResult
		) {
			with(result.device) {
				Log.i("ScanCallback", "Found BLE device! Name: " +
						"${name ?: "Unamed"}, address: $address")
			}
		}
	}

	private val gattCallback = object : BluetoothGattCallback() {
		override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
			super.onConnectionStateChange(gatt, status, newState)
		}
	}
}