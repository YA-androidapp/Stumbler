package jp.gr.java_conf.ya.stumbler; // Copyright (c) 2012-2015 YA <ya.androidapp@gmail.com> All rights reserved.

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StumblerActivity extends Activity implements LocationListener, NmeaListener, SensorEventListener {

	private boolean accEnabled = false;
	private float[] accValues = new float[3];
	private Button button_pref, button_map, button_exit, button_revgeocode;
	private ByteArrayOutputStream byteArrayOutputStream = null;
	private int counterRevGeocode = 0;
	private String currentNmeaGpgga = "";
	private String currentNmeaGpgsa = "";
	private String currentNmeaGpgsv = "";
	private String currentNmeaGprmc = "";
	private String currentNmeaGpvtg = "";
	private String currentProviders = "";
	private String currentRevGeocodeResult = "";
	private boolean enable_revgeocode;
	private boolean enable_show_gpssetting_if_disable;
	private boolean enable_show_gpssetting_on_destroy;
	private byte[] h2dBuffer = new byte[1024];
	private HttpURLConnection httpURLConnection = null;
	private float[] I = new float[16];
	private InputStream inputStream = null;
	private float[] inR = new float[16];
	private int isSize = 0;
	private LocationManager locationManager;
	private boolean magEnabled = false;
	private float[] magValues = new float[3];
	private StringBuilder mainwifiinfoStringBuilder = new StringBuilder();
	private StringBuilder nmeaStringBuilder = new StringBuilder();
	private float[] oriValues = new float[3];
	private float[] outR = new float[16];
	private double preLat = 0;
	private double preLng = 0;
	private SensorManager sensorManager;
	private StringBuilder sensorStringBuilder = new StringBuilder();
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN);
	private SimpleDateFormat simpleDateFormatShort = new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN);
	private String startupTime = "20000101000000";
	private String statusString = "";
	private String TAG = "Stumbler";

	private String timeStamp = "";
	private TextView tv_latlng, tv_address, tv_error, tv_output, tv_providers, tv_sensor;
	private double walkSpeed = 4.0;
	private StringBuilder wifiinfosStringBuilder = new StringBuilder();

	//	private String getAccuracy2String(int accuracy) {
	//		switch (accuracy) {
	//		case SensorManager.SENSOR_STATUS_UNRELIABLE:
	//			return "SENSOR_STATUS_UNRELIABLE";
	//		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
	//			return "SENSOR_STATUS_ACCURACY_HIGH";
	//		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
	//			return "SENSOR_STATUS_ACCURACY_MEDIUM";
	//		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
	//			return "SENSOR_STATUS_ACCURACY_LOW";
	//		default:
	//			return "NA";
	//		}
	//	}

	public String getMainWifiInfo(Location location, Date date) {
		WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();

		mainwifiinfoStringBuilder = new StringBuilder();

		// Date
		try {
			mainwifiinfoStringBuilder.append(simpleDateFormat.format(date));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// BSSID
		try {
			mainwifiinfoStringBuilder.append(info.getBSSID());
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// Location
		try {
			mainwifiinfoStringBuilder.append(Double.toString(location.getLongitude()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");
		try {
			mainwifiinfoStringBuilder.append(Double.toString(location.getLatitude()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");
		try {
			mainwifiinfoStringBuilder.append(Double.toString(location.getAltitude()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");
		try {
			mainwifiinfoStringBuilder.append(Float.toString(location.getSpeed()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");
		try {
			mainwifiinfoStringBuilder.append(Float.toString(location.getBearing()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");
		try {
			mainwifiinfoStringBuilder.append(Float.toString(location.getAccuracy()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");
		try {
			mainwifiinfoStringBuilder.append(simpleDateFormat.format(location.getTime()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// Providers
		try {
			mainwifiinfoStringBuilder.append(currentProviders);
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// SSID
		try {
			mainwifiinfoStringBuilder.append(info.getSSID());
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// ステルスモード
		try {
			mainwifiinfoStringBuilder.append(Boolean.toString(info.getHiddenSSID()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// IPアドレス
		try {
			int ipAdr = info.getIpAddress();
			mainwifiinfoStringBuilder.append(String.format("IP Adrress : %02d.%02d.%02d.%02d", ( ipAdr >> 0 ) & 0xff, ( ipAdr >> 8 ) & 0xff, ( ipAdr >> 16 ) & 0xff, ( ipAdr >> 24 ) & 0xff));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// MACアドレス
		try {
			mainwifiinfoStringBuilder.append(info.getMacAddress());
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// ネットワークID
		try {
			mainwifiinfoStringBuilder.append(Integer.toString(info.getNetworkId()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// リンクスピード
		try {
			mainwifiinfoStringBuilder.append(Integer.toString(info.getLinkSpeed()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		int rssi = Integer.MAX_VALUE;

		// 受信信号強度
		try {
			rssi = info.getRssi();
			mainwifiinfoStringBuilder.append(Integer.toString(rssi));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// 信号レベル
		try {
			if (rssi != Integer.MAX_VALUE) {
				int level = WifiManager.calculateSignalLevel(rssi, 5);
				mainwifiinfoStringBuilder.append(Integer.toString(level));
			} else {
				mainwifiinfoStringBuilder.append("NA");
			}
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\t");

		// 接続状況
		try {
			mainwifiinfoStringBuilder.append(supplicantStateToString(info.getSupplicantState()));
		} catch (Exception e) {
			mainwifiinfoStringBuilder.append("NA");
		}
		mainwifiinfoStringBuilder.append("\n");

		return mainwifiinfoStringBuilder.toString();
	}

	public void getNmeaInfos(long timestamp, String nmea) {
		String timeStamp = simpleDateFormat.format(new Date(System.currentTimeMillis()));

		String[] nmeaArray = nmea.split(",");

		// GPGSV,GPGSA,GPRMC,GPVTG,GPGGA
		if (nmeaArray[0].equals("$GPGSA")) {
			TextView tv = (TextView) findViewById(R.id.nmea_gpgsa);
			tv.setText(nmea.trim());
			currentNmeaGpgsa = nmea.trim();
		} else if (nmeaArray[0].equals("$GPRMC")) {
			TextView tv = (TextView) findViewById(R.id.nmea_gprmc);
			tv.setText(nmea.trim());
			currentNmeaGprmc = nmea.trim();
		} else if (nmeaArray[0].equals("$GPVTG")) {
			TextView tv = (TextView) findViewById(R.id.nmea_gpvtg);
			tv.setText(nmea.trim());
			currentNmeaGpvtg = nmea.trim();
		} else if (nmeaArray[0].equals("$GPGGA")) {
			TextView tv = (TextView) findViewById(R.id.nmea_gpgga);
			tv.setText(nmea.trim());
			currentNmeaGpgga = nmea.trim();
		} else if (nmeaArray[0].equals("$GPGSV")) {
			int maxNum = Integer.valueOf(nmeaArray[1]);
			int messageNum = Integer.valueOf(nmeaArray[2]);

			LinearLayout layout = (LinearLayout) findViewById(R.id.nmea_gpgsv_layout);
			TextView tv = (TextView) layout.findViewById(messageNum);
			if (tv == null) {
				tv = new TextView(this);
				layout.addView(tv);
				tv.setId(messageNum);
			}
			tv.setText(nmea.trim());

			if (( messageNum == 1 ) || ( currentNmeaGpgsv.split("/").length >= maxNum )) {
				currentNmeaGpgsv = nmea.trim();
			} else {
				currentNmeaGpgsv = "/" + nmea.trim();
			}
		}

		nmeaStringBuilder = new StringBuilder();

		try {
			nmeaStringBuilder.append(timeStamp);
		} catch (Exception e) {
			nmeaStringBuilder.append("NA");
		}
		nmeaStringBuilder.append("\t");
		try {
			nmeaStringBuilder.append(nmea.trim());
		} catch (NoSuchFieldError e) {
			nmeaStringBuilder.append("NA");
		} catch (Exception e) {
			nmeaStringBuilder.append("NA");
		}
		nmeaStringBuilder.append("\t");
		try {
			nmeaStringBuilder.append(simpleDateFormat.format(timestamp));
		} catch (NoSuchFieldError e) {
			nmeaStringBuilder.append("NA");
		} catch (Exception e) {
			nmeaStringBuilder.append("NA");
		}

		nmeaStringBuilder.append("\n");

		// 書き出し
		writeFile("nmea", startupTime, nmeaStringBuilder.toString());
	}

	//	private String getType2String(int type) {
	//		switch (type) {
	//		case Sensor.TYPE_ACCELEROMETER:
	//			return "TYPE_ACCELEROMETER";
	//		case Sensor.TYPE_ALL:
	//			return "TYPE_ALL";
	//		case Sensor.TYPE_AMBIENT_TEMPERATURE:
	//			return "TYPE_AMBIENT_TEMPERATURE";
	//		case Sensor.TYPE_GRAVITY:
	//			return "TYPE_GRAVITY";
	//		case Sensor.TYPE_GYROSCOPE:
	//			return "TYPE_GYROSCOPE";
	//		case Sensor.TYPE_LIGHT:
	//			return "TYPE_LIGHT";
	//		case Sensor.TYPE_LINEAR_ACCELERATION:
	//			return "TYPE_LINEAR_ACCELERATION";
	//		case Sensor.TYPE_MAGNETIC_FIELD:
	//			return "TYPE_MAGNETIC_FIELD";
	//		case Sensor.TYPE_PRESSURE:
	//			return "TYPE_PRESSURE";
	//		case Sensor.TYPE_PROXIMITY:
	//			return "TYPE_PROXIMITY";
	//		case Sensor.TYPE_RELATIVE_HUMIDITY:
	//			return "TYPE_RELATIVE_HUMIDITY";
	//		case Sensor.TYPE_ROTATION_VECTOR:
	//			return "TYPE_ROTATION_VECTOR";
	//		default:
	//			return "NA";
	//		}
	//	}

	public void getWifiInfos(Location location) {
		timeStamp = simpleDateFormat.format(new Date(System.currentTimeMillis()));

		WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
		if (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

			// APをスキャン
			manager.startScan();

			// スキャン結果を取得
			wifiinfosStringBuilder = new StringBuilder();
			List<ScanResult> apList = manager.getScanResults();
			for (int i = 0; i < apList.size(); i++) {

				try {
					wifiinfosStringBuilder.append(timeStamp);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append(apList.get(i).BSSID);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append(Double.toString(location.getLongitude()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(Double.toString(location.getLatitude()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(Double.toString(location.getAltitude()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(Float.toString(location.getSpeed()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(Float.toString(location.getBearing()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(Float.toString(location.getAccuracy()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(simpleDateFormat.format(location.getTime()));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append(currentProviders);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append(Integer.toString(apList.get(i).frequency) + "M");
				} catch (NoSuchFieldError e) {
					wifiinfosStringBuilder.append("NA");
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(Integer.toString(apList.get(i).level));
				} catch (NoSuchFieldError e) {
					wifiinfosStringBuilder.append("NA");
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
					try {
						wifiinfosStringBuilder.append(simpleDateFormat.format(apList.get(i).timestamp));
					} catch (NoSuchFieldError e) {
						wifiinfosStringBuilder.append("NA");
					} catch (Exception e) {
						wifiinfosStringBuilder.append("NA");
					}
				} else {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append(apList.get(i).SSID);
				} catch (NoSuchFieldError e) {
					wifiinfosStringBuilder.append("NA");
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(apList.get(i).capabilities);
				} catch (NoSuchFieldError e) {
					wifiinfosStringBuilder.append("NA");
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append(currentNmeaGpgsa);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(currentNmeaGprmc);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(currentNmeaGpvtg);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(currentNmeaGpgga);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");
				try {
					wifiinfosStringBuilder.append(currentNmeaGpgsv);
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append("Ax:" + ( accValues[0] ));
					wifiinfosStringBuilder.append(",");
					wifiinfosStringBuilder.append("Ay:" + ( accValues[1] ));
					wifiinfosStringBuilder.append(",");
					wifiinfosStringBuilder.append("Az:" + ( accValues[2] ));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				try {
					wifiinfosStringBuilder.append("Ox:" + ( oriValues[1] ));
					wifiinfosStringBuilder.append(",");
					wifiinfosStringBuilder.append("Oy:" + ( oriValues[2] ));
					wifiinfosStringBuilder.append(",");
					wifiinfosStringBuilder.append("Oz:" + ( oriValues[0] ));
				} catch (Exception e) {
					wifiinfosStringBuilder.append("NA");
				}
				wifiinfosStringBuilder.append("\t");

				if (currentRevGeocodeResult.equals("") == false) {
					try {
						wifiinfosStringBuilder.append(currentRevGeocodeResult);
					} catch (Exception e) {
						wifiinfosStringBuilder.append("NA");
					}

					tv_address.setText("");
					currentRevGeocodeResult = "";
				} else {
					wifiinfosStringBuilder.append("NA");
				}

				wifiinfosStringBuilder.append("\n");
			}

			// 書き出し
			writeFile("", startupTime, wifiinfosStringBuilder.toString());
		}
	}

	public byte[] http2data(String path) throws Exception {
		h2dBuffer = new byte[1024];
		httpURLConnection = null;
		inputStream = null;
		byteArrayOutputStream = null;
		try {
			//HTTP接続のオープン
			httpURLConnection = (HttpURLConnection) ( ( new URL(path) ).openConnection() );
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			inputStream = httpURLConnection.getInputStream();

			//バイト配列の読み込み
			byteArrayOutputStream = new ByteArrayOutputStream();
			while (true) {
				isSize = inputStream.read(h2dBuffer);
				if (isSize <= 0) {
					break;
				}
				byteArrayOutputStream.write(h2dBuffer, 0, isSize);
			}
			byteArrayOutputStream.close();

			//HTTP接続のクローズ
			inputStream.close();
			httpURLConnection.disconnect();
			return byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			try {
				if (httpURLConnection != null)
					httpURLConnection.disconnect();
				if (inputStream != null)
					inputStream.close();
				if (byteArrayOutputStream != null)
					byteArrayOutputStream.close();
			} catch (Exception e2) {
			}
			throw e;
		}
	}

	//センサー精度の変更時に呼ばれる
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

		super.onCreate(savedInstanceState);

		Log.v(TAG, "onCreate()");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		startupTime = simpleDateFormatShort.format(new Date(System.currentTimeMillis()));

		setContentView(R.layout.activity_main);
		tv_address = (TextView) findViewById(R.id.address);
		tv_error = (TextView) findViewById(R.id.error);
		tv_latlng = (TextView) findViewById(R.id.latlng);
		tv_output = (TextView) findViewById(R.id.output);
		tv_providers = (TextView) findViewById(R.id.providers);
		tv_sensor = (TextView) findViewById(R.id.tv_sensor);
		button_exit = (Button) findViewById(R.id.button_exit);
		button_map = (Button) findViewById(R.id.button_map);
		button_pref = (Button) findViewById(R.id.button_pref);
		button_revgeocode = (Button) findViewById(R.id.button_revgeocode);

		tv_latlng.setText("35.622581,140.103279");

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

		if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			Log.v(TAG, "(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED)");

			wifiManager.setWifiEnabled(true);

			int i = 0;
			while (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
				if (i > 11) {
					finish();
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}

				i++;
			}
		}

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.addNmeaListener(this);

		LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
		if (provider != null) {
			Log.v(TAG, "(provider != null)");

			long interval = 5000;
			try {
				interval = Long.parseLong(pref.getString("interval", "5000"));
			} catch (NumberFormatException e) {
				interval = 5000;
			}

			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0.0f, this);
			} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, 0.0f, this);
			}

			tv_providers.setText(provider.getName());
			currentProviders = provider.getName();
		} else {
			Log.v(TAG, "(provider == null)");

			tv_providers.setText("");
			currentProviders = "";

			enable_show_gpssetting_if_disable = pref.getBoolean("enable_show_gpssetting_if_disable", false);
			if (enable_show_gpssetting_if_disable) {
				try {
					startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
				} catch (ActivityNotFoundException e) {
				} catch (Exception e) {
				}
			}
		}

		//センサーの取得
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		for (Sensor sensor : sensors) {
			Log.v(TAG, "sensor: " + sensor);

			int type = sensor.getType();

			if (type == Sensor.TYPE_ACCELEROMETER) {
				sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
				accEnabled = true;
			} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
				sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
				magEnabled = true;
			}
		}

		button_revgeocode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				revGeocode();
			}
		});
		button_map.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(StumblerActivity.this);
				int map_zoom = Integer.parseInt(pref.getString("map_zoom", "20"));

				place_share(map_zoom);
			}
		});
		button_pref.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.gr.java_conf.ya.stumbler", "jp.gr.java_conf.ya.stumbler.Preference");
				startActivity(intent);
			}
		});
		button_exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

	}

	protected void onDestroy() {
		locationManager.removeUpdates(this);
		locationManager.removeNmeaListener(this);

		//センサーのリスナー解除
		if (accEnabled || magEnabled) {
			sensorManager.unregisterListener(this);
			accEnabled = false;
			magEnabled = false;
		}

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (currentProviders.equals("") == false) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			enable_show_gpssetting_on_destroy = pref.getBoolean("enable_show_gpssetting_on_destroy", false);

			if (enable_show_gpssetting_on_destroy) {
				try {
					startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
				} catch (ActivityNotFoundException e) {
				} catch (Exception e) {
				}
			}
		}

		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		tv_latlng.setText(String.format("%+03.8f", location.getLatitude()) + "," + String.format("%+03.8f", location.getLongitude()));

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		double min_distance = Integer.parseInt(pref.getString("min_distance", "1"));
		if (( location.getSpeed() < walkSpeed ) || ( CoordsUtil.calcDistHubeny(location.getLatitude(), location.getLongitude(), preLat, preLng) > min_distance )) {
			preLat = location.getLatitude();
			preLng = location.getLongitude();

			getWifiInfos(location);

			enable_revgeocode = pref.getBoolean("enable_revgeocode", false);
			if (enable_revgeocode) {
				counterRevGeocode++;

				int revgeocode_interval = Integer.parseInt(pref.getString("revgeocode_interval", "20"));
				if (counterRevGeocode % revgeocode_interval == 0) {
					counterRevGeocode = 0;

					revGeocode();
				}
			}
		}
	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) {
		getNmeaInfos(timestamp, nmea);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	// センサーイベント受信時に呼ばれる
	public void onSensorChanged(SensorEvent event) {
		Log.v(TAG, "onSensorChanged()");

		float[] tempOriValues = new float[3];
		float[] tempOriValuesDeg = new float[3];
		float[] oriValuesCor = new float[3];

		String timeStamp = simpleDateFormat.format(new Date(System.currentTimeMillis()));

		//センサー種別の取得
		int type = event.sensor.getType();

		if (type == Sensor.TYPE_ACCELEROMETER) {
			//加速度センサーの情報の格納
			accValues = event.values.clone();
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			//地磁気センサーの情報の格納
			magValues = event.values.clone();
		}

		//端末の傾きの計算
		if (accEnabled && magEnabled) {
			//回転行列の計算
			SensorManager.getRotationMatrix(inR, I, accValues, magValues);

			//端末向きに応じた軸の変更
			SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);

			//端末の傾きの取得
			SensorManager.getOrientation(outR, tempOriValues);

			//ラジアンを度に変換
			tempOriValuesDeg[0] = (float) Math.toDegrees(tempOriValues[0]);
			tempOriValuesDeg[1] = (float) Math.toDegrees(tempOriValues[1]);
			tempOriValuesDeg[2] = (float) Math.toDegrees(tempOriValues[2]);

			//重力加速度を考慮
			oriValuesCor[0] = tempOriValuesDeg[0] - ( tempOriValuesDeg[0] * 0.1f + oriValues[0] * 0.9f );
			oriValuesCor[1] = tempOriValuesDeg[1] - ( tempOriValuesDeg[1] * 0.1f + oriValues[0] * 0.9f );
			oriValuesCor[2] = tempOriValuesDeg[2] - ( tempOriValuesDeg[2] * 0.1f + oriValues[0] * 0.9f );

			oriValues[0] = tempOriValuesDeg[0];
			oriValues[1] = tempOriValuesDeg[1];
			oriValues[2] = tempOriValuesDeg[2];
		}
		try {
			sensorStringBuilder.append(timeStamp);
		} catch (Exception e) {
			sensorStringBuilder.append("NA");
		}
		sensorStringBuilder.append("\t");

		if (accEnabled) {
			sensorStringBuilder.append("Ax:" + ( accValues[0] ));
			sensorStringBuilder.append(",");
			sensorStringBuilder.append("Ay:" + ( accValues[1] ));
			sensorStringBuilder.append(",");
			sensorStringBuilder.append("Az:" + ( accValues[2] ));
			sensorStringBuilder.append("\t");

			if (magEnabled) {

				sensorStringBuilder.append("Ox:" + ( oriValues[1] ));
				sensorStringBuilder.append(",");
				sensorStringBuilder.append("Oy:" + ( oriValues[2] ));
				sensorStringBuilder.append(",");
				sensorStringBuilder.append("Oz:" + ( oriValues[0] ));
				sensorStringBuilder.append("\t");

				sensorStringBuilder.append("Ocx:" + ( oriValuesCor[1] ));
				sensorStringBuilder.append(",");
				sensorStringBuilder.append("Ocy:" + ( oriValuesCor[2] ));
				sensorStringBuilder.append(",");
				sensorStringBuilder.append("Ocz:" + ( oriValuesCor[0] ));
				sensorStringBuilder.append("\t");

				tv_sensor.setText(String.format("%+03.1f", accValues[0]) + "," + String.format("%+03.1f", accValues[1]) + "," + String.format("%+03.1f", accValues[2]) + "\t"
						+ String.format("%+03.1f", oriValues[1]) + "," + String.format("%+03.1f", oriValues[2]) + "," + String.format("%+03.1f", oriValues[0]));
			} else {
				sensorStringBuilder.append("NA\tNA\t");

				tv_sensor.setText(String.format("%+03.1f", accValues[0]) + "," + String.format("%+03.1f", accValues[1]) + "," + String.format("%+03.1f", accValues[2]));

			}

		} else {
			sensorStringBuilder.append("NA\tNA\tNA\t");
		}

		//				List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
		//				for (Sensor s : list) {
		//					sensorStringBuilder.append(s.getName() + ":" + getType2String(s.getType()) + ",");
		//				}
		//		sensorStringBuilder.append("\t");
		//
		//		sensorStringBuilder.append(getAccuracy2String(event.accuracy));
		//
		//		sensorStringBuilder.append("\n");
		//
		// 書き出し
		//		writeFile("sensor", startupTime, sensorStringBuilder.toString());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		statusString = "Unknown";
		if (status == LocationProvider.AVAILABLE) {
			tv_providers.setText(provider);
			currentProviders = provider;
			statusString = "AVAILABLE";
		} else if (status == LocationProvider.OUT_OF_SERVICE) {
			tv_providers.setText("");
			currentProviders = "";
			statusString = "OUT OF SERVICE";
		} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			tv_providers.setText("");
			currentProviders = "";
			statusString = "TEMP UNAVAILABLE";
		}
		Toast.makeText(this, "LocationProvider: " + statusString, Toast.LENGTH_SHORT).show();
	}

	public void place_share(int map_zoom) {
		if (tv_latlng.getText().equals("") == false) {
			try {
				String geo_uri = "geo:" + tv_latlng.getText();
				if (map_zoom > 0 && map_zoom < 24) {
					geo_uri += "?z=" + map_zoom;
				}

				Uri uri = Uri.parse(geo_uri);
				Intent intent_map = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent_map);
			} catch (ActivityNotFoundException e) {
			} catch (Exception e) {
			}
		}
	}

	public void revGeocode() {
		if (tv_latlng.getText().equals("") == false) {
			Log.v("GpsTest", "uri: " + "http://www.geocoding.jp/?q=" + tv_latlng.getText());
			String address = "";
			try {
				address = new String(http2data("http://www.geocoding.jp/?q=" + ( tv_latlng.getText().toString() ).replace(",", "%2C+")));
			} catch (Exception e) {
				Log.v("GpsTest", "e: " + e.toString());
			}
			Log.v("GpsTest", "address: " + address);
			if (address.equals("") == false) {
				Pattern pattern = Pattern.compile("<!-- google_ad_section_start -->.*?<b>(.+?)</b>.*?<!-- google_ad_section_end -->", Pattern.DOTALL);
				final Matcher matcher = pattern.matcher(address);
				if (matcher.find()) {
					Log.v("GpsTest", "matcher.group(1): " + matcher.group(1));
					currentRevGeocodeResult = matcher.group(1);
					tv_address.setText(getString(R.string.address) + matcher.group(1));
				} else {
					Log.v("GpsTest", "matcher.group(1): NULL");
					currentRevGeocodeResult = "NULL";
					tv_address.setText(getString(R.string.address) + "NULL");
				}
			}
		}
	}

	public String supplicantStateToString(SupplicantState supplicantState) {

		if (supplicantState == SupplicantState.ASSOCIATED) {
			return "ASSOCIATED";
		} else if (supplicantState == SupplicantState.ASSOCIATING) {
			return "ASSOCIATING";
		} else if (supplicantState == SupplicantState.AUTHENTICATING) {
			return "AUTHENTICATING";
		} else if (supplicantState == SupplicantState.COMPLETED) {
			return "COMPLETED";
		} else if (supplicantState == SupplicantState.DISCONNECTED) {
			return "DISCONNECTED";
		} else if (supplicantState == SupplicantState.DORMANT) {
			return "DORMANT";
		} else if (supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE) {
			return "FOUR_WAY_HANDSHAKE";
		} else if (supplicantState == SupplicantState.GROUP_HANDSHAKE) {
			return "GROUP_HANDSHAKE";
		} else if (supplicantState == SupplicantState.INACTIVE) {
			return "INACTIVE";
		} else if (supplicantState == SupplicantState.INTERFACE_DISABLED) {
			return "INTERFACE_DISABLED";
		} else if (supplicantState == SupplicantState.INVALID) {
			return "INVALID";
		} else if (supplicantState == SupplicantState.SCANNING) {
			return "SCANNING";
		} else if (supplicantState == SupplicantState.UNINITIALIZED) {
			return "UNINITIALIZED";
		} else {
			return "";
		}
	}

	public void writeFile(String type, String date, String str) {
		String filePath =
				( ( ( Environment.getExternalStorageState() ).equals(Environment.MEDIA_MOUNTED) ) ? Environment.getExternalStorageDirectory().getPath() : Environment.getDataDirectory().getPath() )
						+ "/" + getPackageName() + "/stumbler_" + type + "_" + date + ".txt";
		File file = new File(filePath);
		file.getParentFile().mkdir();

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(str);
			bw.flush();
			bw.close();
			tv_output.setTextColor(Color.BLACK);
			if (type.equals("")) {
				tv_output.setText(str);
			}
			tv_error.setTextColor(Color.GRAY);
			tv_error.setText("");

			try {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				String url = pref.getString("sound_update", "");
				if (url != null) {
					if (url.equals("") == false) {
						Uri uri = Uri.parse(url);
						MediaPlayer mp = MediaPlayer.create(this, uri);
						mp.setLooping(true);
						mp.seekTo(0);
						mp.start();

						long interval = Long.parseLong(pref.getString("interval", "5000"));

						try {
							Thread.sleep(interval / 2l);
						} catch (InterruptedException e2) {
						}

						mp.stop();
					}
				}
			} catch (Exception e1) {
			}
		} catch (Exception e) {
			tv_output.setTextColor(Color.RED);
			if (type.equals("")) {
				tv_output.setText(str);
			}
			tv_error.setTextColor(Color.RED);
			tv_error.setText(getString(R.string.error) + e.getMessage());

			try {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				String url = pref.getString("sound_error", "");
				if (url != null) {
					if (url.equals("") == false) {
						Uri uri = Uri.parse(url);
						MediaPlayer mp = MediaPlayer.create(this, uri);
						mp.setLooping(true);
						mp.seekTo(0);
						mp.start();

						long interval = Long.parseLong(pref.getString("interval", "5000"));

						try {
							Thread.sleep(interval);
						} catch (InterruptedException e2) {
						}

						mp.stop();
					}
				}
			} catch (Exception e1) {
			}
		}
	}
}
