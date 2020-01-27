package fr.epita.apping_x2_p21_ghibliquiz.Interfaces

import fr.epita.apping_x2_p21_ghibliquiz.Models.FilmObject
import fr.epita.apping_x2_p21_ghibliquiz.Models.PeopleObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GhibliInterface {

    @GET("films")
    fun listFilms() : Call<ArrayList<FilmObject>>

    @GET("films/{id}")
    fun getFilmDetail(@Path("id") id: String) : Call<FilmObject>

    @GET("people")
    fun listP() : Call<ArrayList<PeopleObject>>
}