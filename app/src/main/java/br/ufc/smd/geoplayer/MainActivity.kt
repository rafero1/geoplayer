package br.ufc.smd.geoplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.provider.MediaStore.Audio
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    companion object {
        const val BROADCAST_PLAY_NEW_AUDIO = "br.ufc.smd.geoplayer.PLAY_NEW_AUDIO"
        const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        const val CHANNEL_ID = "br.ufc.smd.geoplayer.CHANNEL_ID"
    }

    private var player: MediaPlayerService? = null
    var serviceBound = false
    var songList: ArrayList<Song>? = null


    ///////////////////////////////////////////
    /// Lifecycle methods
    ///////////////////////////////////////////


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        if (checkAndRequestPermissions()) {
            loadSongList()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("serviceStatus")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            //service is active
            player?.stopSelf()
        }
    }


    ///////////////////////////////////////////
    /// Permission dialog
    ///////////////////////////////////////////


    private fun checkAndRequestPermissions(): Boolean {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            val permissionReadPhoneState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
            val permissionStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val listPermissionsNeeded = arrayListOf<String>()

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE)
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (listPermissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
                return false
            } else return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

        val TAG = "LOG_PERMISSION"
        Log.d(TAG, "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = hashMapOf<String, Int>()
                // Initialize the map with both permissions
                perms[android.Manifest.permission.READ_PHONE_STATE] = PackageManager.PERMISSION_GRANTED
                perms[android.Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions

                    if (perms[android.Manifest.permission.READ_PHONE_STATE] == PackageManager.PERMISSION_GRANTED && perms[android.Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Phone state and storage permissions granted")
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        loadSongList()
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                      //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_PHONE_STATE
                            )
                        ) {
                            showDialogOK("Phone state and storage permissions required for this app",
                                DialogInterface.OnClickListener { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }// proceed with logic by disabling the related features or quit the app.
                                })
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                .show()
                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }


    ///////////////////////////////////////////
    /// Service & Notification methods
    ///////////////////////////////////////////


    //Binding this Client to the AudioPlayer Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MediaPlayerService.LocalBinder
            player = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notif_channel)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    ///////////////////////////////////////////
    /// Playback
    ///////////////////////////////////////////


    private fun playAudio(audioIndex: Int) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable songList to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeSong(songList!!)
            storage.storeSongIndex(audioIndex)

            val playerIntent = Intent(this, MediaPlayerService::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(applicationContext)
            storage.storeSongIndex(audioIndex)

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(BROADCAST_PLAY_NEW_AUDIO)
            sendBroadcast(broadcastIntent)
        }
    }


    ///////////////////////////////////////////
    /// Activity functions
    ///////////////////////////////////////////


    private fun loadSongs() {
        val contentResolver = contentResolver

        val uri = Audio.Media.EXTERNAL_CONTENT_URI
        val selection = Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = Audio.Media.TITLE + " ASC"

        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.count > 0) {
            songList = ArrayList()
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(Audio.Media.DATA))
                val title = cursor.getString(cursor.getColumnIndex(Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(Audio.Media.ALBUM))
                val artist = cursor.getString(cursor.getColumnIndex(Audio.Media.ARTIST))

                // Save to songList
                songList?.add(Song(data, title, album, artist))
            }
        }
        cursor?.close()
    }

    private fun initRecyclerView() {
        if (songList != null && songList!!.size > 0) {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.apply {
                adapter = SongListAdapter(songList!!, application)
                layoutManager = LinearLayoutManager(this@MainActivity)
                addItemDecoration(
                    DividerItemDecoration(
                        recyclerView.context,
                        LinearLayoutManager.VERTICAL
                    )
                )
                addOnItemTouchListener(CustomTouchListener(this@MainActivity, object : ItemTouchListener {
                    override fun onClick(view: View, index: Int) {
                        playAudio(index)
                    }
                }))
            }
        }
    }

    private fun loadSongList() {
        loadSongs()
        initRecyclerView()
    }
}
