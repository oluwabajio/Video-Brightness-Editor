package video.brightness.editor.increase

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kbeanie.multipicker.api.Picker
import com.kbeanie.multipicker.api.VideoPicker
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback
import com.kbeanie.multipicker.api.entity.ChosenVideo
import video.brightness.editor.increase.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var videoPicker: VideoPicker
    var brightnessLevel: Int = 0
    lateinit var originalPath: String
    lateinit var outputPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initViews()
        initListeners()
        binding.btnSelect.setOnClickListener { selectVideo() }

    }

    private fun initListeners() {
        videoPicker.setVideoPickerCallback(object : VideoPickerCallback {
            override fun onError(p0: String?) {

            }

            override fun onVideosChosen(pathList: MutableList<ChosenVideo>?) {
                playVideo(pathList?.get(0)?.originalPath)
                originalPath = pathList?.get(0)?.originalPath.toString()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    binding.brightnessSeekbar.setProgress(100, true)
                } else {
                    binding.brightnessSeekbar.setProgress(100)
                }
            }
        })

        binding.btnRender.setOnClickListener {

            val seekbarValue = binding.brightnessSeekbar.progress
            val ss = (seekbarValue - 100) / 100
            brightnessLevel = ss

            runFFMpeg()
        }
        binding.brightnessSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                getPercentage();
                Log.e(TAG, "onProgressChanged: " + getPercentage())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })



    }

    private fun runFFMpeg() {
        val outputDirectory =
            File(Environment.getExternalStoragePublicDirectory("").toString() + "/VideoBrightness/")
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        val outputDir: String = outputDirectory.getAbsolutePath()
        val ffmpegString: String = getBrightnessString(originalPath, outputDir)
        Log.e(Companion.TAG, "convertToMono: $originalPath")

        val executionId = FFmpeg.executeAsync(ffmpegString) { executionId, returnCode ->

            if (returnCode == RETURN_CODE_SUCCESS) {
                Log.e(Config.TAG, "Async command execution completed successfully.")
                playVideo2(outputPath)

            } else if (returnCode == RETURN_CODE_CANCEL) {
                Log.e(Config.TAG, "Async command execution cancelled by user.")
            } else {
                Log.e(
                    Config.TAG, String.format(
                        "Async command execution failed with rc=%d.", returnCode))
                Config.printLastCommandOutput(Log.INFO)
            }
        }
    }

    private fun playVideo2(outputPath: String) {
        val videoView = findViewById<VideoView>(R.id.videoView2)
        //Creating MediaController
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        //specify the location of media file
        val uri: Uri = Uri.parse(outputPath)
        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    }

    private fun getBrightnessString(originalPath: String, outputDir: String): String {
        outputPath =
            outputDir + "/Stef_" + SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) + ".mp4"
        return String.format(
            "-i  '%s' -vf eq=brightness=%s -c:a copy '%s'",
            originalPath,
            getPercentage(),
            outputPath)

    }

    private fun getPercentage(): Float {
        var value: Float
        value = (binding.brightnessSeekbar.progress - 100).toFloat() * .010f
        return value
    }

    private fun playVideo(originalPath: String?) {
        val videoView = findViewById<VideoView>(R.id.videoView)
        //Creating MediaController
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        //specify the location of media file
        val uri: Uri = Uri.parse(originalPath)
        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.requestFocus()
        videoView.start()
    }

    private fun initViews() {
        videoPicker = VideoPicker(this)
    }

    private fun selectVideo() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        videoPicker.pickVideo();
                    } else {
                        Log.e("TAG", "onPermissionsChecked: false")
                        Toast.makeText(
                            this@MainActivity,
                            "Kindly Accept all Permissions",
                            Toast.LENGTH_LONG).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?, token: PermissionToken?
                ) {
                    token?.continuePermissionRequest();
                }

            }).check()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Picker.PICK_VIDEO_DEVICE) {
            videoPicker.submit(data)

        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }


}