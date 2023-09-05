package com.example.studybuddy.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAXFaNXOA:APA91bGE84MCzg8ILxJ7dN2kehtmwEfxzkvz9F2ns6saKmjoF1RcRUbq84BhZfK2qT4jusj_F3vnGBr09Dju7cx9xB1x8VdN5ByzNEyyYOLT-UliuULSwc7lYVbonTXkHPhZKQQr_m8r"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender sender);
}
