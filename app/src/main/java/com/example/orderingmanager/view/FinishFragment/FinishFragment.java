package com.example.orderingmanager.view.FinishFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.example.orderingmanager.Dto.ResultDto;
import com.example.orderingmanager.Dto.RetrofitService;
import com.example.orderingmanager.Dto.request.SalesRequestDto;
import com.example.orderingmanager.Dto.response.DailySalesDto;
import com.example.orderingmanager.Dto.response.SalesResponseDto;
import com.example.orderingmanager.R;
import com.example.orderingmanager.UserInfo;
import com.example.orderingmanager.databinding.FragmentFinishBinding;
import com.example.orderingmanager.view.MainActivity;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FinishFragment extends Fragment {
    private View view;
    private FragmentFinishBinding binding;
    private MaterialCalendarView calendarView;
    public TextView diaryTextView, selectedView, monthView;
    private Button btn_graph;

    String from, before, firstday;
    ArrayList<String> salesListFinal = new ArrayList<>();

    Bundle extra;

    Boolean storeInitInfo;

    int displayMonth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFinishBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        extra = this.getArguments();
        if (extra != null) {
            extra = getArguments();

            /* ????????? ????????? ???????????? ??????????????? */
        }

        //btn_graph = view.findViewById(R.id.btn_graph);

        initButtonClickListener();
        storeInfoCheck();

        displayMonth = LocalDate.now().getMonthValue();
        initData(LocalDate.now().getMonthValue());

        getDate();
        // try catch
        //???????????? ????????? ???????????? ?????? ???????????? arraylist ???????????? ????????????.



        Log.e("restaurantId", String.valueOf(UserInfo.getRestaurantId()));

        return view;
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initData(int currentMonth){
        int currentYear;
        int nextYear;
        int nextMonth;

        LocalDate now = LocalDate.now();
        currentYear = now.getYear();
        nextYear = currentYear;
        currentMonth = displayMonth;
        nextMonth = currentMonth + 1;
        if (currentMonth == 12) {
            nextMonth = 1;
            nextYear++;
        }

        String from2Server = String.format("%s-%s", currentYear, currentMonth < 10 ? "0" + currentMonth : currentMonth);
        getSalesRequestFromServer(from2Server);

        diaryTextView = view.findViewById(R.id.diaryTextView);
        selectedView = view.findViewById(R.id.selectedView);
        monthView = view.findViewById(R.id.monthView);
    }



    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getSalesRequestFromServer(String from2Server) {
        ArrayList<String> salesList = new ArrayList<>();
        // ?????? ?????? ?????? ????????????
        try {
            Log.e("sales1", from2Server);
            SalesRequestDto salesRequestDto = new SalesRequestDto(from2Server);

            new Thread() {
                @SneakyThrows
                public void run() {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://www.ordering.ml/api/restaurant/" + UserInfo.getRestaurantId() + "/daily_sales/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    RetrofitService service = retrofit.create(RetrofitService.class);
                    Call<ResultDto<List<SalesResponseDto>>> call = service.getSalesDaily(UserInfo.getRestaurantId(), salesRequestDto);

                    call.enqueue(new Callback<ResultDto<List<SalesResponseDto>>>() {
                        @Override
                        public void onResponse(Call<ResultDto<List<SalesResponseDto>>> call, Response<ResultDto<List<SalesResponseDto>>> response) {
                            ResultDto<List<SalesResponseDto>> result = response.body();

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    result.getData().forEach(SalesResponseDto -> {
                                        salesList.add(SalesResponseDto.getSales());
                                        Log.e("sales list", String.valueOf(salesList));
                                    });

//                                    for(int i = 1; i<32; i++){
//                                        salesList.add(Integer.toString(i*2000));
//                                    }
//

                                    int sum = 0;
                                    for(int i=0; i < salesList.size(); i++) {
                                        int salesOfInt = Integer.valueOf(salesList.get(i));
                                        sum += salesOfInt;
                                    }

                                    salesListFinal = salesList;

                                    LocalDate now = LocalDate.now();
                                    int currentMonth = now.getMonthValue();
                                    int currentDay = now.getDayOfMonth();
                                    Log.e("total date sales", String.valueOf(sum));
                                    Log.e("currentMonth", String.valueOf(currentMonth));
                                    Log.e("displayMonth", String.valueOf(displayMonth));
                                    String selectedSales = salesList.get(currentDay - 1);

                                    if (String.valueOf(currentMonth) == String.valueOf(displayMonth)) {
                                        Log.e("same", "same");
                                        diaryTextView.setText(from2Server + "-" + currentDay);
                                        selectedView.setText(displayMonth + "??? " + currentDay + "??? ?????? : " + selectedSales + "???");
                                        monthView.setText(displayMonth + "??? ??? ?????? : " + sum + "???");
                                    }

                                    else {
                                        Log.e("different", "different");
                                        diaryTextView.setText(from2Server + "-" + currentDay);
                                        monthView.setText(displayMonth + "??? ??? ?????? : " + sum + "???");
                                    }


                                    //binding.monthView.setText(from2Server + "??? ??? ?????? : " + sum + "???");

                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<ResultDto<List<SalesResponseDto>>> call, Throwable t) {
                            Toast.makeText(getActivity(), "???????????? ????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                            Log.e("e = ", t.getMessage());
                        }

                    });
                }
            }.start();

        } catch (Exception e) {
            Toast.makeText(getActivity(), "???????????? ????????? ?????????????????????.", Toast.LENGTH_LONG).show();
            Log.e("e = ", e.getMessage());
        }
    }

    public void getDate() {
        MaterialCalendarView materialCalendarView = view.findViewById(R.id.calendarView);
        materialCalendarView.setSelectedDate(CalendarDay.today());

        // ???, ????????? ????????? ????????? ?????? (MonthArrayTitleFormatter??? ????????? ??????????????? ?????? setTitleFormatter()??? ?????????)
        materialCalendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        materialCalendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));

        materialCalendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
                // ????????? ?????? ?????? ~ ????????? ??????
                String startDay = dates.get(0).getDate().toString();
                String endDay = dates.get(dates.size() - 1).getDate().toString();
                // ????????? ?????? ???, ????????? ???
                int startMonth = dates.get(0).getMonth();
                int endMonth = dates.get(dates.size() - 1).getMonth();
                // ????????? ?????? ???, ????????? ???
                int startDate = dates.get(0).getDay();
                int endDate = dates.get(dates.size() - 1).getDay();


                Log.e("DATE", "????????? : " + startDay + ", ????????? : " + endDay);
                from = startDay;
                before = endDay;

                int sum = 0;
                for(int i= Integer.valueOf(startDate) - 1; i < Integer.valueOf(endDate); i++) {
                    int salesOfInt = Integer.valueOf(salesListFinal.get(i));
                    sum += salesOfInt;
                }

                LocalDate now = LocalDate.now();
                int currentMonth = now.getMonthValue();

                Log.e("current", String.valueOf(currentMonth));
                Log.e("display", String.valueOf(displayMonth));

                if (String.valueOf(startMonth) != String.valueOf(endMonth)) {
                    Log.e("month changed", "month changed");
                    binding.selectedView.setText("?????? ????????? ?????? ??? ???????????? ?????? ???????????????.");
                }
                else {
                    binding.selectedView.setText(startMonth + "??? " + startDate + "??? - " + endMonth + "??? " + endDate + "??? ?????? : " + sum + "???");
                }

                binding.diaryTextView.setText(startDay + " ~ " + endDay);
                //binding.monthView.setText(startMonth + "??? ??? ?????? : ");

            }
        });

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                String selectedDate = date.getDate().toString();
                int selectedMonth = date.getMonth();
                int selectedDay = date.getDay();

                firstday = selectedDate;

                Log.e("DATE", selectedDate);
                binding.diaryTextView.setText(selectedDate);
                binding.selectedView.setText(selectedMonth + "??? " + selectedDay + "??? ?????? : ");
                //binding.monthView.setText(selectedMonth + "??? ??? ?????? : ");

                Log.e("selected date sales", String.valueOf(salesListFinal.get(selectedDay - 1)));

                String selectedSales = salesListFinal.get(selectedDay - 1);
                binding.selectedView.setText(selectedMonth + "??? " + selectedDay + "??? ?????? : " + selectedSales + "???");

            }
        });

        materialCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

                displayMonth = date.getMonth();
                Log.e("select month", String.valueOf(displayMonth));

                initData(displayMonth);

            }
        });

    }

    public void storeInfoCheck() {
        storeInitInfo = UserInfo.getRestaurantId() != null;
        if (!storeInitInfo) {
            Log.e("FinishFragment", storeInitInfo.toString());
            binding.viewErrorLoadStore.errorNotFound.setVisibility(View.VISIBLE);
            binding.finishFragment.setVisibility(View.GONE);
            //binding.refreshImageButton.setOnClickListener(onClickListener);
        } else {
            Log.e("FinishFragment", storeInitInfo.toString());
            binding.viewErrorLoadStore.errorNotFound.setVisibility(View.GONE);
            binding.finishFragment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void initButtonClickListener() {
        binding.viewErrorLoadStore.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });

        binding.btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GraphActivity.class);
                startActivity(intent);
            }
        });
    }


}
