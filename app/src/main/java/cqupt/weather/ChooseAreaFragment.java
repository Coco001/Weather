package cqupt.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cqupt.weather.bean.City;
import cqupt.weather.bean.County;
import cqupt.weather.bean.Province;
import cqupt.weather.db.Myopenhelper;
import cqupt.weather.util.HttpUtil;
import cqupt.weather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int currentLevel;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private Myopenhelper oh;
    private static final String TAG = "ChooseAreaFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        oh = new Myopenhelper(getActivity().getApplicationContext());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        SQLiteDatabase database = oh.getReadableDatabase();
        provinceList = new ArrayList<>();
        Cursor cursor = database.query("Province", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Province province = new Province();
            province.setProvinceCode(cursor.getInt(cursor.getColumnIndex("code")));
            province.setProvinceName(cursor.getString(cursor.getColumnIndex("name")));
            provinceList.add(province);
        }
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province p : provinceList) {
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromService(address, "province");
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        int id = selectedProvince.getProvinceCode();
        SQLiteDatabase database = oh.getReadableDatabase();
        cityList = new ArrayList<>();
        Cursor cursor = database.query("City", null, "provinceId=?", new String[]{id + ""}, null, null, null);
        while (cursor.moveToNext()) {
            City city = new City();
            city.setCityCode(cursor.getInt(cursor.getColumnIndex("code")));
            city.setCityName(cursor.getString(cursor.getColumnIndex("name")));
            city.setProvinceId(cursor.getInt(cursor.getColumnIndex("provinceId")));
            cityList.add(city);
        }
        if (cityList.size() > 0) {
            dataList.clear();
            for (City c : cityList) {
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromService(address, "city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        int id = selectedCity.getCityCode();
        SQLiteDatabase database = oh.getReadableDatabase();
        countyList = new ArrayList<>();
        Cursor cursor = database.query("County", null, "cityId=?", new String[]{id + ""}, null, null, null);
        while (cursor.moveToNext()) {
            County county = new County();
            county.setCityId(cursor.getInt(cursor.getColumnIndex("cityId")));
            county.setCountyName(cursor.getString(cursor.getColumnIndex("name")));
            county.setWeatherId(cursor.getString(cursor.getColumnIndex("weatherId")));
            countyList.add(county);
        }
        if (countyList.size() > 0) {
            dataList.clear();
            for (County c : countyList) {
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromService(address, "county");
        }
    }

    private void queryFromService(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Message message = new Message();
                message.what = 2;
                mHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText, getActivity().getApplicationContext());
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getProvinceCode(), getActivity().getApplicationContext());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getCityCode(), getActivity().getApplicationContext());
                }
                if (result) {
                    Message message = new Message();
                    message.what = 1;
                    message.obj = type;
                    mHandler.sendMessage(message);
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    closeProgressDialog();
                    String type = (String) msg.obj;
                    if ("province".equals(type)) {
                        queryProvinces();
                    } else if ("city".equals(type)) {
                        queryCities();
                    } else if ("county".equals(type)) {
                        queryCounties();
                    }
                    break;
                case 2:
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
