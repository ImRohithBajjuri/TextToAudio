package com.rb.texttoaudio

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract.Colors
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.material.snackbar.Snackbar
import com.rb.texttoaudio.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        MobileAds.initialize(this)

        val adRequest = AdRequest.Builder().build()
        binding.bannerAd.loadAd(adRequest)




        binding.generateAudio.setOnClickListener {
            binding.textInput.clearFocus()
            it.startAnimation(pressAnim(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (!hasStoragePermission()) {
                        buildSnackbar(this@MainActivity, "Storage permission required for generating audio files", binding.root).show()
                        buildStoragePermission().show()
                    }
                    else {
                        if (binding.textInput.text.toString().trim().isEmpty()) {
                            buildSnackbar(this@MainActivity, "Please enter some text to generate...", binding.root).show()
                        }
                        else {
                            val name = uniqueContentNameGenerator("GeneratedAudio")
                            val path = "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOWNLOADS}/TextToAudio/${name}.mp3"
                            val folder = File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOWNLOADS}/TextToAudio/")
                            if (!folder.exists()) {
                                folder.mkdir()
                            }
                            var tts: TextToSpeech? = null
                            tts = TextToSpeech(this@MainActivity) {
                                val result = tts!!.synthesizeToFile(binding.textInput.text.toString(), null, File(path), "name")

                                if (result == TextToSpeech.SUCCESS) {
                                    val sb = buildSnackbar(this@MainActivity, "Text to Audio file generated at 'Downloads/TextToAudio/${name}'", binding.root)
                                    sb.setAction("open") {
                                        val fileUri = FileProvider.getUriForFile(
                                            this@MainActivity,
                                            "com.rb.texttoaudio" + ".provider", File(path)
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.setDataAndType(fileUri, "video/mp4")
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        startActivity(intent)
                                    }
                                    sb.duration = Snackbar.LENGTH_INDEFINITE
                                    sb.show()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(3000)
                                        val request = AdRequest.Builder().build()
                                        InterstitialAd.load(this@MainActivity, getString(R.string.interstital1), request, object: InterstitialAdLoadCallback(){
                                            override fun onAdLoaded(p0: InterstitialAd) {
                                                p0.show(this@MainActivity)
                                                p0.fullScreenContentCallback = object: FullScreenContentCallback(){
                                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                                        super.onAdFailedToShowFullScreenContent(p0)
                                                    }

                                                    override fun onAdShowedFullScreenContent() {

                                                    }

                                                    override fun onAdDismissedFullScreenContent() {
                                                        Toast.makeText(this@MainActivity, "Thank you for supporting the app!", Toast.LENGTH_SHORT).show()
                                                        super.onAdDismissedFullScreenContent()
                                                    }

                                                    override fun onAdImpression() {
                                                        super.onAdImpression()
                                                    }
                                                }
                                            }

                                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                                super.onAdFailedToLoad(p0)
                                            }
                                        })
                                    }
                                }
                                else {
                                    buildSnackbar(this@MainActivity, "Failed to generate audio, try again...", binding.root).show()

                                    val request = AdRequest.Builder().build()

                                    InterstitialAd.load(this@MainActivity, getString(R.string.interstital1), request, object: InterstitialAdLoadCallback(){
                                        override fun onAdLoaded(p0: InterstitialAd) {
                                            p0.show(this@MainActivity)
                                            p0.fullScreenContentCallback = object: FullScreenContentCallback(){
                                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                                    super.onAdFailedToShowFullScreenContent(p0)
                                                }

                                                override fun onAdShowedFullScreenContent() {

                                                }

                                                override fun onAdDismissedFullScreenContent() {
                                                    Toast.makeText(this@MainActivity, "Thank you for supporting the app!", Toast.LENGTH_SHORT).show()
                                                    super.onAdDismissedFullScreenContent()
                                                }

                                                override fun onAdImpression() {
                                                    super.onAdImpression()
                                                }
                                            }
                                        }

                                        override fun onAdFailedToLoad(p0: LoadAdError) {
                                            super.onAdFailedToLoad(p0)
                                        }
                                    })

                                }
                            }
                        }
                    }

                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

            }))
        }
    }

    fun pressAnim(listener: Animation.AnimationListener?): Animation? {
        val press: Animation = ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        press.duration = 150
        press.repeatCount = 1
        press.repeatMode = Animation.REVERSE
        press.interpolator = AccelerateInterpolator()
        if (listener != null) {
            press.setAnimationListener(listener)
        }
        return press
    }

    fun buildSnackbar(context: Context, text: String, view: View): Snackbar {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
        snackbar.view.background =
            ContextCompat.getDrawable(context, R.drawable.snackbar_background)
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.white))
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
        snackbar.view.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        return snackbar
    }
    fun uniqueContentNameGenerator(name: String): String {
        val timeStamp: String =
            SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(
                Date()
            )
        return name + "_" + timeStamp
    }

    fun buildStoragePermission(): AlertDialog.Builder {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission required!")
        builder.setMessage("Storage permission is required for generating audio files")
        builder.setPositiveButton(
            "GIVE",
            DialogInterface.OnClickListener { dialogInterface, i ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES
                        ), 67
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this
                        , arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 67
                    )
                }


            })
        return builder
    }

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }
}