package com.example.napomocinzynierom.data.remote

import android.util.Log
import com.example.napomocinzynierom.data.DataSource
import com.example.napomocinzynierom.Result
import com.example.napomocinzynierom.ui.home.HomeViewModel
import com.example.napomocinzynierom.ui.home.HomeViewModel.Companion.limit
import com.example.napomocinzynierom.ui.home.HomeViewModel.Companion.page
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

class RemoteDataSource : DataSource {
    companion object{
        val BASE_URL = "http://publisher.freesher.ct8.pl/"
        val TAG = "RemoteDataSource"
    }

    //login
    override suspend fun getMagazines(): List<Magazine> = getData(
        page, limit,"",100000,0)
    override suspend fun getMagazines(page: Int, limit: Int): List<Magazine> = getData(
        page, limit, "", 100000, 0)
    override suspend fun getMagazines(page: Int, limit: Int, title: String): List<Magazine> = getData(
        page, limit, title, 100000,0)
    override suspend fun getMagazines(page: Int, limit: Int, title: String, maxPoints: Int): List<Magazine> = getData(
        page, limit, title, maxPoints, 0)
    override suspend fun getMagazines(page: Int, limit: Int, title: String, maxPoints: Int, minPoints: Int): List<Magazine> = getData(
        page, limit, title, maxPoints, minPoints)
    override suspend fun getMagazines(limit: Int, title: String): List<Magazine> = getData(
        page, limit, title, 100000, 0)
    override suspend fun getMagazines(limit: Int, title: String, maxPoints: Int): List<Magazine> = getData(
        page, HomeViewModel.limit, title, maxPoints, 0)
    override suspend fun getMagazines(limit: Int,title: String, maxPoints: Int, minPoints: Int): List<Magazine> = getData(
        page, HomeViewModel.limit, title, maxPoints, minPoints)
    override suspend fun getMagazines(title: String): List<Magazine> = getData(
        page, limit, title, 0, 100000)
    override suspend fun getMagazines(title: String, maxPoints: Int): List<Magazine> = getData(
        page, limit, title, 0, maxPoints)
    override suspend fun getMagazines(title: String, maxPoints: Int, minPoints: Int): List<Magazine> = getMagazines(
        page, limit, title,maxPoints, minPoints)
    override suspend fun getMagazinesByMaxPoints(maxPoints: Int): List<Magazine> = getData(
        page, limit, "", maxPoints, 0)
    override suspend fun getMagazinesByminPoints(minPoints: Int): List<Magazine> = getData(
        page, limit, "", minPoints, 0)
    override suspend fun getMagazinesByPoints(maxPoints: Int, minPoints: Int): List<Magazine> = getDataBySearch(
        page, limit, "", maxPoints, minPoints)

    internal suspend fun getDataBySearch(page: Int, limit: Int, s: String, maxPoints: Int, minPoints: Int): List<Magazine> {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(MagazineService::class.java)
        val call = service.getCurrentMagazineData(page, 100, s, maxPoints, minPoints)
        var result = call.await()
        return result.magazines
    }

    //login
    override suspend fun authoriseCredentials(login: String, password: String): LoginResponde = authoriseUser(login, password)



    internal suspend fun getData(page: Int, limit: Int, title: String, maxPoint: Int, minPoint: Int): List<Magazine>{
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(MagazineService::class.java)
        val call = service.getCurrentMagazineData(page, limit, title, maxPoint, minPoint)
        var result = call.await()
        return result.magazines
    }

    internal suspend fun authoriseUser( login: String, password: String): LoginResponde {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(LoginService::class.java)
        val loginBody = LoginBody()
        loginBody.login = login
        loginBody.password = password
        val call = service.userAuthentication(loginBody)
        var result = call.await()
        return result
    }

    override suspend fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            Log.w(TAG, " error jest tu" + username + password)
            val user = authoriseCredentials(login = username, password = password)
            Log.w(TAG, user.message)
            if(user.message.equals("Auth success")){
                Log.w(TAG, "api działa")
                var loggedInUser = LoggedInUser(user.data!!.id, user.data!!.id.toString())
                return Result.Success(loggedInUser)
            }

            return Result.Error(IOException("Error"))

        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    override suspend fun getMagazineById(id: String): Magazine {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(MagazineService::class.java)
            val call = service.getMagazineById(id)
            return call.await()
        }

    override suspend fun getMagazinesByUserId(id: String): List<Magazine> {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(FavoriteService::class.java)
        val call = service.getFavorite(id)
        return call.await()
    }

    override suspend fun addMagazine(userID: String, magazineID: String): AddMagazineRespond {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(FavoriteService::class.java)
        var newFavoriteMagazine = AddMagazineBody()
        newFavoriteMagazine.userID = userID
        newFavoriteMagazine.magazineId = magazineID
        val call = service.addMagazines(newFavoriteMagazine)
        return call.await()
    }

    override suspend fun addUser(login: String, password: String, firstName: String,lastName: String
    ): SignUpResponde {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(SignUpService::class.java)
        var signUpBody = SignUpBody()
        signUpBody.newUserlogin = login
        signUpBody.newUserpassword = password
        signUpBody.newUserfirstName = firstName
        signUpBody.newUserLastName = lastName


        Log.w("loooooool", "Success create request")
        val response = service.signNewUser(signUpBody).awaitResponse()
        return response.body()!!
    }


    override suspend fun logout() {
        // TODO: revoke authentication
    }

}