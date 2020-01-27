package fr.epita.apping_x2_p21_ghibliquiz

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import fr.epita.apping_x2_p21_ghibliquiz.Adapters.AnswerListA
import fr.epita.apping_x2_p21_ghibliquiz.Interfaces.GhibliInterface
import fr.epita.apping_x2_p21_ghibliquiz.Models.FilmObject
import fr.epita.apping_x2_p21_ghibliquiz.Models.PeopleObject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val baseUrl = "https://ghibliapi.herokuapp.com/"
    private var goodP = PeopleObject("", "", "", "", "", "", ArrayList(), "", "")
    private var chosenF = FilmObject("", "", "", "", "", "", "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), "")
    private fun getFilmIdFromUrl(url: String): String {
        val ret_url = url.substring("https://ghibliapi.herokuapp.com/films/".length);
        return ret_url
    }
    private fun checkIfCorrectP(ppleName: String): Boolean { return goodP.name == ppleName }
    private fun goToDetails(ppleName: String) {
        val explicitIntent = Intent(this, PeopleDetail::class.java)
        explicitIntent.putExtra("FILM_ID", chosenF.id)
        explicitIntent.putExtra("CHARACTER_NAME", goodP.name)
        explicitIntent.putExtra("CORRECT", checkIfCorrectP(ppleName))
        explicitIntent.putExtra("FILM_BASE_URL", this.baseUrl)
        startActivity(explicitIntent)
    }
    @SuppressLint("WrongConstant")
    fun initListWithAnswers(answers: ArrayList<PeopleObject>) {
        val itemClickListener = View.OnClickListener {
            val peopleName = it.tag as String
            Log.d("TEST", "clicked on row $peopleName")
            goToDetails(peopleName)
        }
        answersView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        answersView.setHasFixedSize(true)
        answersView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        answersView.adapter = AnswerListA(this, answers, itemClickListener)
    }
    fun getAnswers(peopleList: ArrayList<PeopleObject>, chosenFilmId: String): ArrayList<PeopleObject> {
        val answers: ArrayList<PeopleObject> = arrayListOf()
        var foundGoodCharacter = false
        for (p in peopleList) {
            if (foundGoodCharacter) {
                if (! p.films.contains("https://ghibliapi.herokuapp.com/films/$chosenFilmId") && answers.size < 6) { answers.add(p) }
            } else {
                if (p.films.contains("https://ghibliapi.herokuapp.com/films/$chosenFilmId") && answers.size < 6) {
                    answers.add(p)
                    foundGoodCharacter = true
                }
            }
        }
        return answers
    }
    fun getChosenFilmId(ppleList: ArrayList<PeopleObject>): String {
        val r = Random().nextInt(ppleList.size)
        val goodAnswer = ppleList[r].films[0]
        goodP = ppleList[r]
        return getFilmIdFromUrl(goodAnswer)
    }
    override fun onCreate(savedIS: Bundle?) {
        super.onCreate(savedIS)
        setContentView(R.layout.activity_main)
        val jsonConverter = GsonConverterFactory.create(GsonBuilder().create())
        val retrofit = Retrofit.Builder()
            .baseUrl(this.baseUrl)
            .addConverterFactory(jsonConverter)
            .build()
        val service: GhibliInterface = retrofit.create(GhibliInterface::class.java)
        val callbackF = object : Callback<FilmObject> {
            override fun onFailure(call: Call<FilmObject>, t: Throwable) {
                Log.d("APP", "failed to get the film")
                Log.d("APP", t.message)
            }
            override fun onResponse(call: Call<FilmObject>, response: Response<FilmObject>) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        chosenF = response.body()!!
                        question.text = "Which one of these characters can be found in the movie ${chosenF.title} ?"
                    }
                }
            }
        }
        val callbackP = object : Callback<ArrayList<PeopleObject>> {
            override fun onFailure(call: Call<ArrayList<PeopleObject>>, t: Throwable) {
                Log.d("APP", "failed to list the people")
                Log.d("APP", t.message)
            }
            override fun onResponse(
                call: Call<ArrayList<PeopleObject>>,
                response: Response<ArrayList<PeopleObject>>
            ) {
                val rCode = response.code()
                if (rCode == 200) {
                    if (response.body() != null) {
                        val peopleList = response.body()!!
                        Log.d("APP", "retrieved pple_list")
                        val chosenFilmId: String = getChosenFilmId(peopleList)
                        val answer: ArrayList<PeopleObject> = getAnswers(peopleList, chosenFilmId)
                        initListWithAnswers(answer)
                        service.getFilmDetail(chosenFilmId).enqueue(callbackF)
                    }
                }
            }
        }
        service.listP().enqueue(callbackP)
    }
}
