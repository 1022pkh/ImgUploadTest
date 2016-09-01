package sopt.com.imgtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import sopt.com.imgtest.model.UploadResult;

public class MainActivity extends AppCompatActivity {

    private TextView getIP;
    private Button getImgBtn;
    private Button uploadImg;
    String getServerURL = "";
    String getImgURL="";
    String getImgName="";

    final int REQ_CODE_SELECT_IMAGE=100;
    ProgressDialog asyncDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIP = (TextView)findViewById(R.id.getIP);

        Intent intent = getIntent();
        getServerURL = intent.getExtras().get("url").toString();
        getIP.setText(intent.getExtras().get("url").toString());


        getImgBtn = (Button)findViewById(R.id.getImg);
        getImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 사진 갤러리 호출
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });

        uploadImg = (Button)findViewById(R.id.uploadImg);
        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(),getImgURL,Toast.LENGTH_SHORT).show();
                /**
                 * getImgBtn 버튼 클릭을 통해, 업로드할 사진의 절대경로를 가져옴
                 * 서버로 보내는 시간을 고려하여 진행바를 넣어줌
                 */

                asyncDialog = new ProgressDialog(MainActivity.this);
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                asyncDialog.setMessage("로딩중입니다..");

                // show dialog
                asyncDialog.show();

                uploadFile(getImgURL , getImgName);


            }
        });


    }

    // 선택된 이미지 가져오기
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();

        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                try {
                    //Uri에서 이미지 이름을 얻어온다.
                    String name_Str = getImageNameToUri(data.getData());

                    Log.i("myTag",name_Str);

                    //이미지 데이터를 비트맵으로 받아온다.
                    Bitmap image_bitmap 	= MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    ImageView image = (ImageView)findViewById(R.id.imageView1);

                    //배치해놓은 ImageView에 set
                    image.setImageBitmap(image_bitmap);

                    //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();

                }
                catch (FileNotFoundException e) { 		e.printStackTrace(); 			}
                catch (IOException e)                 {		e.printStackTrace(); 			}
                catch (Exception e)		         {             e.printStackTrace();			}
            }
        }
    }


    // 선택된 이미지 파일명 가져오기
    public String getImageNameToUri(Uri data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        getImgURL = imgPath;
        getImgName = imgName;

        return "success";
    }


    /**
     * Upload Image Client Code
     */

    private void uploadFile(String ImgURL, String ImgName) {

        /**
         * 현재 연결된 서버의 URL을 받아옴
         */
        String url = getServerURL;

        /**
         * 다시 연결 시도
         */
        // create upload service client
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .build();

        NetworkService service = retrofit.create(NetworkService.class);


        /**
         * 서버로 보낼 파일의 전체 url을 이용해 작업
         */

        File photo = new File(ImgURL);
        RequestBody photoBody = RequestBody.create(MediaType.parse("image/jpg"), photo);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("picture", photo.getName(), photoBody);

//        Log.i("myTag","this file'name is "+ photo.getName());

        /**
         * 서버에 사진이외의 텍스트를 보낼 경우를 생각해서 일단 넣어둠
         */
        // add another part within the multipart request
        String descriptionString = "android";

        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionString);


        /**
         * 사진 업로드하는 부분 // POST방식 이용
         */
        Call<ResponseBody> call = service.upload(body, description);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(response.isSuccessful()){

                    Gson gson = new Gson();
                    try {
                        String getResult = response.body().string();

                        JsonParser parser = new JsonParser();
                        JsonElement rootObejct = parser.parse(getResult);

//                        Log.i("mytag",rootObejct.toString());

                        UploadResult example = gson.fromJson(rootObejct, UploadResult.class);

                        Log.i("mytag",example.url);

                        String result = example.result;

                        if(result.equals("success")){
                            Toast.makeText(getApplicationContext(),"사진 업로드 성공!!!!",Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i("MyTag", "error : "+e.getMessage());
                    }


                }else{
                    Toast.makeText(getApplicationContext(),"사진 업로드 실패!!!!",Toast.LENGTH_SHORT).show();
                }


                // dismiss dialog
                asyncDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());

                // dismiss dialog
                asyncDialog.dismiss();
            }



        });
    }


}
