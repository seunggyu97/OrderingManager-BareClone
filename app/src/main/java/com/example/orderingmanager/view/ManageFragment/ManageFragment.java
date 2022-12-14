package com.example.orderingmanager.view.ManageFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.orderingmanager.Dto.ResultDto;
import com.example.orderingmanager.Dto.RetrofitService;
import com.example.orderingmanager.Dto.request.FoodCategory;
import com.example.orderingmanager.Dto.request.RestaurantType;
import com.example.orderingmanager.Dto.request.SignInDto;
import com.example.orderingmanager.Dto.response.OwnerSignInResultDto;
import com.example.orderingmanager.Dto.response.RestaurantInfoDto;
import com.example.orderingmanager.R;
import com.example.orderingmanager.UserInfo;
import com.example.orderingmanager.databinding.ActivityStoreNoticeBinding;
import com.example.orderingmanager.databinding.FragmentManageBinding;
import com.example.orderingmanager.view.MainActivity;
import com.example.orderingmanager.view.QRFragment.QrList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageFragment extends Fragment {

    private View view;
    private FragmentManageBinding binding;
    public static final String EMPTY_NOTICE = "SECRETCODEFOREMPTYNOTICE";
    Boolean storeInitInfo;
    String notice;

    TextView waitingTimeTextView, takeoutTimeTextView, tvNoticePreview;
    ProgressBar progressBarStoreInfo;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        // ????????????
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.detach(this).attach(this).commit();

        initButtonClickListener();

        storeInfoCheck();

        if (UserInfo.getRestaurantId() != null) {
            initView();


            getDataFromServer();
        }
        return view;
    }

    private void initView() {
        // ?????????
        binding.tvStoreName.setText(UserInfo.getRestaurantName());

        // ????????????
        binding.tvName.setText(UserInfo.getOwnerName());

        // ????????????
        binding.tvAddress.setText(UserInfo.getAddress());

        // ????????????
        RestaurantType restaurantType = UserInfo.getRestaurantType();
        switch (restaurantType) {
            case ONLY_TO_GO:
                binding.tvMealMethod.setText("??????");
                break;
            case FOR_HERE_TO_GO:
                binding.tvMealMethod.setText("????????????, ??????");
                break;
        }
        // ????????????
        FoodCategory foodCategory = UserInfo.getFoodCategory();
        switch (foodCategory) {
            case PIZZA:
                binding.tvCategory.setText("??????");
                break;
            case BUNSIK:
                binding.tvCategory.setText("??????");
                break;
            case CHICKEN:
                binding.tvCategory.setText("??????");
                break;
            case KOREAN_FOOD:
                binding.tvCategory.setText("??????");
                break;
            case CAFE_DESSERT:
                binding.tvCategory.setText("??????/?????????");
                break;
            case PORK_CUTLET_ROW_FISH_SUSHI:
                binding.tvCategory.setText("?????????/???/??????");
                break;
            case FAST_FOOD:
                binding.tvCategory.setText("???????????????");
                break;
            case JJIM_TANG:
                binding.tvCategory.setText("???/???");
                break;
            case CHINESE_FOOD:
                binding.tvCategory.setText("?????????");
                break;
            case JOKBAL_BOSSAM:
                binding.tvCategory.setText("??????/??????");
                break;
            case ASIAN_FOOD_WESTERN_FOOD:
                binding.tvCategory.setText("?????????/??????");
                break;
        }

        waitingTimeTextView = view.findViewById(R.id.tv_waiting_time);
        takeoutTimeTextView = view.findViewById(R.id.tv_take_out_waiting_time);
        progressBarStoreInfo = view.findViewById(R.id.progressBar_store_info);
        tvNoticePreview = view.findViewById(R.id.tv_notice_preview);

        setStoreNoticeFromServerData();
    }

    private void initButtonClickListener() {
        binding.btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), StoreManageActivity.class));
                //getActivity().finish();
            }
        });

        binding.btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditPersonalInfoActivity.class));
            }
        });

        binding.btnStoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditStoreInfoActivity.class);
                startActivity(intent);
                // ?????? ??? ????????? ???????????? ?????? startActivityForResult ??? ??????
                // ??? ??????????????? ????????? ???????????? MainActivity ??? onActivityResult ?????? ?????? ?????????.
                //startActivityForResult(intent,MainActivity.MANAGEFRAGMENT);
            }
        });//

        binding.viewErrorLoadStore.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });


        binding.btnSettingWaitingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Bundle??? ????????? WaitingBottomDialog??? ?????????.
                WaitingTimeSetDialog waitingTimeSetDialog = new WaitingTimeSetDialog();
                Bundle waitingData = new Bundle();
                waitingData.putString("waitingTime", String.valueOf(binding.tvWaitingTime.getText()));
                waitingTimeSetDialog.setArguments(waitingData);
                waitingTimeSetDialog.show((getActivity()).getSupportFragmentManager(), "WaitingTimeSetDialog");
            }
        });

        binding.btnSettingTakeOutWaitingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Bundle??? ????????? WaitingBottomDialog??? ?????????.
                OrderWaitingTimeSetDialog takeOutwaitingTimeSetDialog = new OrderWaitingTimeSetDialog();
                Bundle waitingData = new Bundle();
                waitingData.putString("takeoutWaitingTime", String.valueOf(binding.tvTakeOutWaitingTime.getText()));
                takeOutwaitingTimeSetDialog.setArguments(waitingData);
                takeOutwaitingTimeSetDialog.show((getActivity()).getSupportFragmentManager(), "TakeOutWaitingTimeSetDialog");
            }
        });

        binding.btnStoreNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noticeSavedInstance = binding.tvNoticePreview.getText().toString();

                Intent intent = new Intent(getActivity(), StoreNoticeActivity.class);
                if(notice.equals(EMPTY_NOTICE)){
                    intent.putExtra("notice", EMPTY_NOTICE);
                }else{
                    intent.putExtra("notice", noticeSavedInstance);
                }
                startActivity(intent);
            }
        });
    }

    public void storeInfoCheck() {
        storeInitInfo = UserInfo.getRestaurantId() != null;
        if (!storeInitInfo) {
            Log.e("ManageFragment", storeInitInfo.toString());
            binding.viewErrorLoadStore.errorNotFound.setVisibility(View.VISIBLE);
            binding.manageFragment.setVisibility(View.GONE);
        } else {
            Log.e("ManageFragment", storeInitInfo.toString());
            binding.viewErrorLoadStore.errorNotFound.setVisibility(View.GONE);
            binding.manageFragment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // ?????? ????????? ??????????????? fragment ??? ?????? ?????????????????? onResume ??? ????????????
        // ?????? ?????? ?????? ???????????????.

        if (UserInfo.getRestaurantId() != null) {
            initView();
            initQrList();
            getDataFromServer();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void initQrList() {
        // MainActivity??? ???????????? QrList??? ???????????? ??? UserInfo??? ????????? tableCount??? ????????????
        // QrList??? ??????Qr,?????????Qr,?????????Qr Bitmap??? ????????????.
        // static ???????????? ???????????? ????????? ?????? ???????????? ??????????????? ???????????? ?????????
        // ???????????? QrList??? ????????? ??? ??????

        int tableCount = UserInfo.getTableCount();
        ArrayList<Bitmap> qrArrayList = new ArrayList<>();
        qrArrayList.add(CreateTakeoutQR());
        qrArrayList.add(CreateWaitingQR());

        if (tableCount != 0) {
            for (int i = 1; i < tableCount + 1; i++) {
                qrArrayList.add(CreateTableQR(i));
            }
        }

        QrList qrList = new QrList(qrArrayList);
    }

    private Bitmap CreateTakeoutQR() {
        String url;
        url = "http://www.ordering.ml/" + UserInfo.getRestaurantId() + "/takeout";
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            Log.e("takeout qr ", "??????");
            return bitmap;
        } catch (Exception e) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ordering_bitmap);
            Log.e("takeout qr ", e.toString());
            Log.e("url = ", url);
            return bitmap;
        }
    }

    private Bitmap CreateWaitingQR() {
        String url;
        url = "http://www.ordering.ml/" + Long.toString(UserInfo.getRestaurantId()) + "/waiting";
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            return bitmap;
        } catch (Exception e) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ordering_bitmap);
            return bitmap;
        }
    }

    private Bitmap CreateTableQR(int i) {
        String url;
        url = "http://www.ordering.ml/" + Long.toString(UserInfo.getRestaurantId()) + "/table" + i;
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            return bitmap;
        } catch (Exception e) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ordering_bitmap);
            return bitmap;
        }
    }

    //getRestaurantInfo
    private void getDataFromServer() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("?????? ??????", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        String token = task.getResult();

                        SignInDto signInDto = new SignInDto(UserInfo.getUserId(), UserInfo.getUserPW(), token);

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://www.ordering.ml/api/owner/signin/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        RetrofitService service = retrofit.create(RetrofitService.class);
                        Call<ResultDto<OwnerSignInResultDto>> call = service.ownerSignIn(signInDto);

                        call.enqueue(new Callback<ResultDto<OwnerSignInResultDto>>() {
                            @Override
                            public void onResponse(Call<ResultDto<OwnerSignInResultDto>> call, Response<ResultDto<OwnerSignInResultDto>> response) {
                                ResultDto<OwnerSignInResultDto> result = response.body();

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // ????????? ???????????? ?????????Url??? ????????? ??????
                                        if(getActivity() != null) {
                                            if (result.getData().getProfileImageUrl() == null) {
                                                Glide.with(getActivity()).load(R.drawable.icon).into(binding.ivStoreIcon);
                                            } else {
                                                Glide.with(getActivity()).load(result.getData().getProfileImageUrl()).into(binding.ivStoreIcon);
                                            }
                                        }
                                        // ????????? ???????????? ????????? ?????? ??????
                                        Integer waitingTime = result.getData().getAdmissionWaitingTime();
                                        UserInfo.setAdmissionWaitingTime(waitingTime);
                                        waitingTimeTextView.setText(String.valueOf(waitingTime));
                                        Integer takeoutWaitingTime = result.getData().getOrderingWaitingTime();
                                        UserInfo.setOrderingWaitingTime(takeoutWaitingTime);
                                        takeoutTimeTextView.setText(String.valueOf(takeoutWaitingTime));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<ResultDto<OwnerSignInResultDto>> call, Throwable t) {
                                Log.e("e = ", t.getMessage());
                            }
                        });

                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.e("token Log", msg);
                    }
                });
    }

    private void setStoreNoticeFromServerData(){
        try {
            new Thread() {
                @SneakyThrows
                public void run() {

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://www.ordering.ml/api/restaurant/"+UserInfo.getRestaurantId()+"/info/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    RetrofitService service = retrofit.create(RetrofitService.class);
                    Call<ResultDto<RestaurantInfoDto>> call = service.getStoreNoticeAndCoordinate(UserInfo.getRestaurantId());

                    call.enqueue(new Callback<ResultDto<RestaurantInfoDto>>() {
                        @Override
                        public void onResponse(Call<ResultDto<RestaurantInfoDto>> call, Response<ResultDto<RestaurantInfoDto>> response) {

                            if (response.isSuccessful()) {
                                ResultDto<RestaurantInfoDto> result;
                                result = response.body();
                                if (result.getData() != null) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBarStoreInfo.setVisibility(View.GONE);
                                            notice = result.getData().getNotice();
                                            if(notice != null && !notice.equals("")){
                                                tvNoticePreview.setText(notice);
                                            }else{
                                                tvNoticePreview.setText("??????????????? ??????????????????.");
                                                notice = EMPTY_NOTICE;
                                            }
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResultDto<RestaurantInfoDto>> call, Throwable t) {
                            Toast.makeText(getActivity(), "??????????????? ???????????? ??? ????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                            Log.e("e = ", t.getMessage());
                            progressBarStoreInfo.setVisibility(View.GONE);

                        }
                    });
                }
            }.start();

        } catch (Exception e) {
            Toast.makeText(getActivity(), "??????????????? ???????????? ??? ????????? ?????????????????????.", Toast.LENGTH_LONG).show();
            Log.e("e = ", e.getMessage());
            progressBarStoreInfo.setVisibility(View.GONE);

        }
    }
}
