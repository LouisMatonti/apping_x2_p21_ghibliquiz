package fr.epita.apping_x2_p21_ghibliquiz

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import fr.epita.apping_x2_p21_ghibliquiz.Interfaces.GhibliInterface
import fr.epita.apping_x2_p21_ghibliquiz.Models.FilmObject
import kotlinx.android.synthetic.main.activity_people_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PeopleDetail : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_details)
        button_people.setOnClickListener(this@PeopleDetail)
        val originIntent = intent
        val baseUrl = originIntent.getStringExtra("FILM_BASE_URL")
        val filmId = originIntent.getStringExtra("FILM_ID")
        val correctValue = originIntent.getBooleanExtra("CORRECT", false)
        val ppleName = originIntent.getStringExtra("CHARACTER_NAME")
        if (correctValue) {
            correct.text = "RIGHT!"
            correct.setTextColor(Color.GREEN)
        } else {
            correct.text = "WRONG!"
            correct.setTextColor(Color.RED)
        }
        charactersView.text = ppleName
        val jsonConverter = GsonConverterFactory.create(GsonBuilder().create())
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonConverter)
            .build()
        val service: GhibliInterface = retrofit.create(GhibliInterface::class.java)
        val callbackFilm = object : Callback<FilmObject> {
            override fun onFailure(call: Call<FilmObject>, t: Throwable) {
                Log.d("APP", "failed to get the film")
                Log.d("APP", t.message)
            }
            override fun onResponse(call: Call<FilmObject>, response: Response<FilmObject>) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        val chosenF = response.body()!!
                        titleView.text = chosenF.title
                        synopsisView.text = chosenF.description
                        directorView.text = chosenF.director
                        yearView.text = chosenF.release_date
                    }
                }
            }
        }
        service.getFilmDetail(filmId).enqueue(callbackFilm)
    }
    override fun onClick(clickedView: View?) {
        if (clickedView != null) {
            when (clickedView.id) {
                R.id.button_people -> {
                    val url = Intent(Intent.ACTION_VIEW)
                    url.data = Uri.parse("https://google.com/search?q="+titleView.text)
                    startActivity(url)
                }
            }
        }
    }
}
