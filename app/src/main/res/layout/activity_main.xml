<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/rootLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#0D1B2A"
    android:padding="16dp">

    <!-- نوار جستجوی شهر -->
    <LinearLayout
        android:id="@+id/searchLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginBottom="16dp">

        <EditText
            android:id="@+id/etSearchCity"
            android:layout_width="0dp"
            android:layout_height="50dp"
            android:layout_weight="1"
            android:background="#1B263B"
            android:hint="نام شهر (مثل تهران، خرم‌آباد...)"
            android:textColor="#FFFFFF"
            android:textColorHint="#A0A0A0"
            android:paddingHorizontal="12dp"
            android:textSize="14sp" />

        <Button
            android:id="@+id/btnSearch"
            android:layout_width="wrap_content"
            android:layout_height="50dp"
            android:layout_marginStart="8dp"
            backgroundTint="#415A77"
            android:text="جستجو"
            android:textColor="#FFFFFF" />
    </LinearLayout>

    <!-- اطلاعات اصلی آب و هوا -->
    <ImageView
        android:id="@+id/ivWeatherIcon"
        android:layout_width="90dp"
        android:layout_height="90dp"
        android:layout_below="@id/searchLayout"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="10dp"
        android:src="@android:drawable/ic_menu_compass" />

    <TextView
        android:id="@+id/tvCityName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/ivWeatherIcon"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="8dp"
        android:text="خرم آباد"
        android:textColor="#FFFFFF"
        android:textSize="26sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/tvTemp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/tvCityName"
        android:layout_centerHorizontal="true"
        android:text="23°C"
        android:textColor="#E0E1DD"
        android:textSize="48sp"
        android:textStyle="bold" />

    <!-- کارت شیشه‌ای جزئیات جوی -->
    <androidx.cardview.widget.CardView
        android:id="@+id/cardDetails"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/tvTemp"
        android:layout_marginTop="20dp"
        app:cardBackgroundColor="#1B263B"
        app:cardCornerRadius="16dp"
        app:cardElevation="6dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:id="@+id/tvHumidity"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="رطوبت هوا: --"
                android:textColor="#FFFFFF"
                android:textSize="14sp"
                android:layout_marginBottom="8dp"/>

            <TextView
                android:id="@+id/tvWindSpeed"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="سرعت باد: --"
                android:textColor="#FFFFFF"
                android:textSize="14sp"
                android:layout_marginBottom="8dp"/>

            <TextView
                android:id="@+id/tvRainfall"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="میزان بارندگی: --"
                android:textColor="#FFFFFF"
                android:textSize="14sp"
                android:layout_marginBottom="8dp"/>

            <TextView
                android:id="@+id/tvDataSource"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="پیش‌بینی از MET Norway"
                android:textColor="#8D99AE"
                android:textSize="12sp"
                android:layout_marginTop="6dp"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

</RelativeLayout>
