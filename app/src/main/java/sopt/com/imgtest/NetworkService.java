package sopt.com.imgtest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import sopt.com.imgtest.model.Result;

/**
 * Created by kh on 2016. 8. 25..
 */
public interface NetworkService {

    @GET("/connect")
    Call<Result> connecting();

    @Multipart
    @POST("/upload")
    Call<ResponseBody> upload(@Part("description") RequestBody description,
                              @Part MultipartBody.Part file);

}
