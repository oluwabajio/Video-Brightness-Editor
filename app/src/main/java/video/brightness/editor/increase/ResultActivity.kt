package video.brightness.editor.increase

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import video.brightness.editor.increase.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    lateinit var binding:ActivityResultBinding
    lateinit var vidUrl: String
    lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initAds()
        initListeners()

        val videoUrl = intent.getStringExtra("vid_url")
        vidUrl = videoUrl.toString()
        playVideo(videoUrl)

    }


    private fun initAds() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-9562015878942760/4393140610"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun initListeners() {
        binding.btnGoBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("Video Brightness App")
            //set message for alert dialog
            builder.setMessage("Video Saved to \n  ${vidUrl}")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder.setPositiveButton("Yes"){dialogInterface, which ->
               dialogInterface.dismiss()
                showInterstitial()
            }

            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()


}
    }

    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            Log.e("TAG", "The interstitial wasn't loaded yet.")
        }
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
}