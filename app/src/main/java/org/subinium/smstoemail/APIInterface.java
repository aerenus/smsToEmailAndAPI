package org.subinium.smstoemail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("sms.php")
    //Call<List<APIModel>> getData();

    Call<List<APIModel>> getData(@Query("smsText") String smsText);
}
