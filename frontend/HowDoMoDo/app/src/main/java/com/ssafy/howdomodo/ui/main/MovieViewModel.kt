package com.ssafy.howdomodo.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.howdomodo.`object`.ObjectCollection
import com.ssafy.howdomodo.data.datasource.model.Movie
import com.ssafy.howdomodo.data.repository.MovieRepository

class MovieViewModel (private val movieRepository: MovieRepository) : ViewModel(){

    val isEmptyMovieData = MutableLiveData<Unit>()
    val movieData = MutableLiveData<List<Movie>>()
    val spinnerData = arrayListOf<String>()
    val spinnerCopyData =MutableLiveData<ArrayList<String>>()
    val errorToast = MutableLiveData<Throwable>()

    fun getMovieData(){
        val key = ObjectCollection.MOVIE_API_KEY
        val region = "ko"
        Log.d("TEST",key)
        movieRepository.getMovieData(key,region,success = {
            if(it.results.isEmpty()){
                isEmptyMovieData.value = Unit
            }else{
                movieData.value = it.results
                for(item in it.results){
                    Log.d("taek",item.title)
                    spinnerData.add(item.title)
                }
            }
            spinnerCopyData.value = spinnerData
        },fail = {
            Log.e("error is :", it.toString())
            errorToast.value = it
        })
    }
}