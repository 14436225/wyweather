package com.weather.app;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.weather.utils.WebAccessTools;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static final String WALLPAPER_FILE="wallpaper_file";
	public static final String STORE_WEATHER="store_weather";
	private MenuInflater mi;
	private LinearLayout rootLayout;	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);        
        setContentView(R.layout.main);        
        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        mi = new MenuInflater(this);       
        String dirPath= "/data/data/com.weather.app/shared_prefs/";
        File file= new File(dirPath);
        boolean isFirstRun = false;
        if(!file.exists()) {
        	SharedPreferences.Editor editor = getSharedPreferences(WALLPAPER_FILE, MODE_PRIVATE).edit();
        	editor.putInt("wellpaper", R.drawable.app_bg02);
        	editor.commit();
        	
        	isFirstRun = true;
        	
        } else {
        	SharedPreferences sp= getSharedPreferences(WALLPAPER_FILE, MODE_PRIVATE);
        	rootLayout.setBackgroundResource(sp.getInt("wellpaper", R.drawable.app_bg02));	
        }
        
        SharedPreferences sp = getSharedPreferences(SetCityActivity.CITY_CODE_FILE ,MODE_PRIVATE);
    	String cityCode= sp.getString("code", "");
    	if( cityCode!= null && cityCode.trim().length()!=0) {
    		SharedPreferences shared = getSharedPreferences(STORE_WEATHER, MODE_PRIVATE);
    		long currentTime = System.currentTimeMillis();
    		long vaildTime = shared.getLong("validTime", currentTime);
    		if(vaildTime > currentTime)
        	   setWeatherSituation(shared);
    		else
    		   setWeatherSituation(cityCode);
        } else {
    		Intent intent = new Intent(MainActivity.this, SetCityActivity.class);
    		intent.putExtra("isFirstRun", isFirstRun);
    		startActivityForResult(intent, 0);
        }
    }
    
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	SharedPreferences sp = getSharedPreferences(SetCityActivity.CITY_CODE_FILE, MODE_PRIVATE);
		String cityCode = sp.getString("code", "");
    	if(cityCode!=null&&cityCode.trim().length()!=0) {
    		if(data!=null&&data.getBooleanExtra("updateWeather", false)) {
    			setWeatherSituation(cityCode);
    		} else {
    			SharedPreferences shared = getSharedPreferences(STORE_WEATHER, MODE_PRIVATE);
    			setWeatherSituation(shared);
    		}
    	} else {
    		MainActivity.this.finish();
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	mi.inflate(R.menu.configure_menu, menu);
    	SharedPreferences sp= getSharedPreferences(WALLPAPER_FILE, MODE_PRIVATE);
    	SubMenu subMenu = menu.getItem(2).getSubMenu();
    	MenuItem item = null;
    	switch(sp.getInt("wellpaper", R.drawable.app_bg02)) {
    	case R.drawable.app_bg01:
    		item = subMenu.getItem(0);
    		item.setChecked(true);
    		break;
    	case R.drawable.app_bg02:
    		item = subMenu.getItem(1);
    		item.setChecked(true);
    		break;
    	case R.drawable.app_bg03:
    		item = subMenu.getItem(2);
    		item.setChecked(true);
    		break;
    	case R.drawable.app_bg04:
    		item = subMenu.getItem(3);
    		item.setChecked(true);
    		break;
    	}
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	SharedPreferences.Editor editor = getSharedPreferences(WALLPAPER_FILE, MODE_PRIVATE).edit();
    	SharedPreferences sp = getSharedPreferences(SetCityActivity.CITY_CODE_FILE, MODE_PRIVATE);
    	switch(menuItem.getItemId()) {
    	case R.id.menu_changeCity:
    		Intent intent = new Intent(MainActivity.this, SetCityActivity.class);
    		startActivityForResult(intent, 0);
    		break;
    	case R.id.menu_update:
            String cityCode = sp.getString("code", "");
            if( cityCode!= null && cityCode.trim().length()!=0) {
            	setWeatherSituation(cityCode);
            }
    		break;
    	case R.id.wallpaper01:
    		rootLayout.setBackgroundResource(R.drawable.app_bg01);
        	editor.putInt("wellpaper", R.drawable.app_bg01);
        	editor.commit();
        	menuItem.setChecked(true);
    		break;
    	case R.id.wallpaper02:
    		rootLayout.setBackgroundResource(R.drawable.app_bg02);
        	editor.putInt("wellpaper", R.drawable.app_bg02);
        	editor.commit();
        	menuItem.setChecked(true);
    		break;
    	case R.id.wallpaper03:
    		rootLayout.setBackgroundResource(R.drawable.app_bg03);
        	editor.putInt("wellpaper", R.drawable.app_bg03);
        	editor.commit();
        	menuItem.setChecked(true);
    		break;
    	case R.id.wallpaper04:
    		rootLayout.setBackgroundResource(R.drawable.app_bg04);
        	editor.putInt("wellpaper", R.drawable.app_bg04);
        	editor.commit();
        	menuItem.setChecked(true);
    		break;
		default:
			break;
    	}
    	
    	return true;
    }
    
    public void setWeatherSituation(String cityCode) {
      String info = "http://m.weather.com.cn/data/"+cityCode+".html";
      info=new WebAccessTools(this).getWebContent(info);
      try {
    	    
			JSONObject json=new JSONObject(info).getJSONObject("weatherinfo");
			TextView tempText = null;
			ImageView imageView=null;
			int weather_icon = 0;
			SharedPreferences.Editor editor = getSharedPreferences(STORE_WEATHER, MODE_PRIVATE).edit();
			info=json.getString("city");
			tempText=(TextView)findViewById(R.id.cityField);
			tempText.setText(info);
			editor.putString("city", info);
			info= json.getString("date_y") ;
			info= info+"("+json.getString("week")+")";
			tempText=(TextView)findViewById(R.id.date_y);
			tempText.setText(info);
			editor.putString("date_y", info);
			info= json.getString("date");
			tempText=(TextView)findViewById(R.id.date);
			tempText.setText(info);
			editor.putString("date", info);
			info= json.getString("temp1");
			tempText=(TextView)findViewById(R.id.currentTemp);
			tempText.setText(info);
			editor.putString("temp1", info);
			info= json.getString("weather1");
			tempText=(TextView)findViewById(R.id.currentWeather);
			tempText.setText(info);
			editor.putString("weather1", info);
			info= json.getString("img_title1");
			imageView=(ImageView)findViewById(R.id.weather_icon01);
			weather_icon = getWeatherBitMapResource(info);
			imageView.setImageResource(weather_icon);
			editor.putInt("img_title1", weather_icon);
			info= json.getString("wind1");
			tempText=(TextView)findViewById(R.id.currentWind);
			tempText.setText(info);
			editor.putString("wind1", info);
			info= json.getString("index_d");
			tempText=(TextView)findViewById(R.id.index_d);
			tempText.setText(info);
			editor.putString("index_d", info);
			info= json.getString("weather2");
			tempText=(TextView)findViewById(R.id.weather02);
			tempText.setText(info);
			editor.putString("weather2", info);
			info= json.getString("img_title2");
			imageView=(ImageView)findViewById(R.id.weather_icon02);
			weather_icon = getWeatherBitMapResource(info);
			imageView.setImageResource(weather_icon);
			editor.putInt("img_title2", weather_icon);
			info= json.getString("temp2");
			tempText=(TextView)findViewById(R.id.temp02);
			tempText.setText(info);
			editor.putString("temp2", info);
			info= json.getString("wind2");
			tempText=(TextView)findViewById(R.id.wind02);
			tempText.setText(info);
			editor.putString("wind2", info);
			info= json.getString("weather3");
			tempText=(TextView)findViewById(R.id.weather03);
			tempText.setText(info);
			editor.putString("weather3", info);
			info= json.getString("img_title3");
			imageView=(ImageView)findViewById(R.id.weather_icon03);
			weather_icon = getWeatherBitMapResource(info);
			imageView.setImageResource(weather_icon);
			editor.putInt("img_title3", weather_icon);
			info= json.getString("temp3");
			tempText=(TextView)findViewById(R.id.temp03);
			tempText.setText(info);
			editor.putString("temp3", info);
			info= json.getString("wind3");
			tempText=(TextView)findViewById(R.id.wind03);
			tempText.setText(info);
			editor.putString("wind3", info);
			long validTime = System.currentTimeMillis();
			validTime = validTime + 5*60*60*1000;
			editor.putLong("validTime", validTime);
			editor.commit();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    public void setWeatherSituation(SharedPreferences shared) {
    	String info = null;
    	TextView tempText = null;
		ImageView imageView=null;
		info = shared.getString("city", "");
		tempText=(TextView)findViewById(R.id.cityField);
		tempText.setText(info);
		info= shared.getString("date_y", "");
		tempText=(TextView)findViewById(R.id.date_y);
		tempText.setText(info);
		info= shared.getString("date", "");
		tempText=(TextView)findViewById(R.id.date);
		tempText.setText(info);
		info= shared.getString("temp1", "");
		tempText=(TextView)findViewById(R.id.currentTemp);
		tempText.setText(info);
		info= shared.getString("weather1", "");
		tempText=(TextView)findViewById(R.id.currentWeather);
		tempText.setText(info);
		imageView=(ImageView)findViewById(R.id.weather_icon01);
		imageView.setImageResource(shared.getInt("img_title1", 0));
		info= shared.getString("wind1", "");
		tempText=(TextView)findViewById(R.id.currentWind);
		tempText.setText(info);
		info= shared.getString("index_d", "");
		tempText=(TextView)findViewById(R.id.index_d);
		tempText.setText(info);
		info= shared.getString("weather2", "");
		tempText=(TextView)findViewById(R.id.weather02);
		tempText.setText(info);
		imageView=(ImageView)findViewById(R.id.weather_icon02);
		imageView.setImageResource(shared.getInt("img_title2", 0));
		info= shared.getString("temp2", "");
		tempText=(TextView)findViewById(R.id.temp02);
		tempText.setText(info);
		info= shared.getString("wind2", "");
		tempText=(TextView)findViewById(R.id.wind02);
		info= shared.getString("weather3", "");
		tempText=(TextView)findViewById(R.id.weather03);
		tempText.setText(info);
		imageView=(ImageView)findViewById(R.id.weather_icon03);
		imageView.setImageResource(shared.getInt("img_title3", 0));
		info= shared.getString("temp3", "");
		tempText=(TextView)findViewById(R.id.temp03);
		tempText.setText(info);
		info= shared.getString("wind3", "");
		tempText=(TextView)findViewById(R.id.wind03);
		tempText.setText(info);
    }
    
    public static int getWeatherBitMapResource(String weather) {
    	Log.i("weather_info", "============="+weather+"===============");
    	if(weather.equals("晴")) {
    		return R.drawable.weathericon_condition_01;
    	} else if(weather.equals("多云")) {
    		return R.drawable.weathericon_condition_02;
    	} else if(weather.equals("阴")) {
    		return R.drawable.weathericon_condition_04;
    	} else if(weather.equals("雾")) {
    		return R.drawable.weathericon_condition_05;
    	} else if(weather.equals("沙尘暴")) {
    		return R.drawable.weathericon_condition_06;
    	} else if(weather.equals("阵雨")) {
    		return R.drawable.weathericon_condition_07;
    	} else if(weather.equals("小雨")||weather.equals("小到中雨")) {
    		return R.drawable.weathericon_condition_08;
    	} else if(weather.equals("大雨")) {
    		return R.drawable.weathericon_condition_09;
    	} else if(weather.equals("雷阵雨")) {
    		return R.drawable.weathericon_condition_10;
    	} else if(weather.equals("小雪")) {
    		return R.drawable.weathericon_condition_11;
    	} else if(weather.equals("大雪")) {
    		return R.drawable.weathericon_condition_12;
    	} else if(weather.equals("雨夹雪")) {
    		return R.drawable.weathericon_condition_13;
    	} else {
    		return R.drawable.weathericon_condition_17;
    	}
    }
}