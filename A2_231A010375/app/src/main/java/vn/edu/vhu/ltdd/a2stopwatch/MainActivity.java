package vn.edu.vhu.ltdd.a2stopwatch;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // MSSV Võ Thành An - 231A010375
    private static final String TAG = "A2_231A010375";

    // Khóa lưu trạng thái vào Bundle
    private static final String KEY_RUNNING = "running";
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_START = "start";
    private static final String KEY_RECREATE = "recreate";
    private static final String KEY_LAPS = "laps";                    // NC1
    private static final String KEY_PAUSE_ON_STOP = "pause_on_stop";  // NC2

    private TextView tvTime, tvStatus, tvRecreate, tvLaps;
    private Button btnStartPause, btnReset, btnLap;
    private CheckBox cbPauseOnStop;

    // Trạng thái của đồng hồ
    private boolean running = false;   // đang chạy hay không
    private long accumulated = 0L;     // số mili-giây đã tích lũy trước lần chạy hiện tại
    private long startTime = 0L;       // mốc elapsedRealtime() lúc bắt đầu lần chạy hiện tại
    private int recreateCount = 0;     // số lần Activity được tạo lại

    // Nâng cao NC1: Danh sách ghi vòng
    private ArrayList<String> lapList = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTimeText();
            handler.postDelayed(this, 100); // cập nhật 10 lần/giây (mỗi 100ms)
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Ánh xạ View từ XML Layout
        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecreate = findViewById(R.id.tvRecreate);
        tvLaps = findViewById(R.id.tvLaps);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnLap = findViewById(R.id.btnLap);
        cbPauseOnStop = findViewById(R.id.cbPauseOnStop);

        // Khôi phục trạng thái từ Bundle nếu Activity được tái tạo
        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(KEY_RUNNING);
            accumulated = savedInstanceState.getLong(KEY_ACCUMULATED);
            startTime = savedInstanceState.getLong(KEY_START);
            recreateCount = savedInstanceState.getInt(KEY_RECREATE) + 1;

            // Nâng cao NC1: Khôi phục danh sách Lap
            ArrayList<String> savedLaps = savedInstanceState.getStringArrayList(KEY_LAPS);
            if (savedLaps != null) {
                lapList = savedLaps;
            }

            // Nâng cao NC2: Khôi phục CheckBox Dừng khi ra nền
            cbPauseOnStop.setChecked(savedInstanceState.getBoolean(KEY_PAUSE_ON_STOP, false));

            Log.d(TAG, "onCreate: KHÔI PHỤC trạng thái, running=" + running
                    + ", accumulated=" + accumulated + "ms, lapsCount=" + lapList.size());
        } else {
            Log.d(TAG, "onCreate: khởi tạo mới (savedInstanceState = null)");
        }

        // Bắt sự kiện Click nút
        btnStartPause.setOnClickListener(v -> {
            if (running) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnReset.setOnClickListener(v -> {
            vibrateDevice(); // NC3: Rung nhẹ khi bấm Đặt lại
            resetStopwatch();
        });

        btnLap.setOnClickListener(v -> addLap()); // NC1: Ghi vòng

        updateUi();
    }

    // ---------------- Logic đồng hồ ----------------

    /** Tổng thời gian đã trôi qua (ms). */
    private long elapsed() {
        return running ? accumulated + (SystemClock.elapsedRealtime() - startTime) : accumulated;
    }

    private void startStopwatch() {
        running = true;
        startTime = SystemClock.elapsedRealtime();
        startTicking();
        updateUi();
        Log.i(TAG, "BẮT ĐẦU đếm giờ");
    }

    private void pauseStopwatch() {
        accumulated += SystemClock.elapsedRealtime() - startTime;
        running = false;
        stopTicking();
        updateUi();
        Log.i(TAG, "TẠM DỪNG tại " + accumulated + "ms");
    }

    private void resetStopwatch() {
        running = false;
        accumulated = 0L;
        startTime = 0L;
        lapList.clear(); // Xóa danh sách Lap khi Reset
        stopTicking();
        updateUi();
        Log.i(TAG, "ĐẶT LẠI về 00:00.0");
    }

    private void startTicking() {
        handler.removeCallbacks(ticker);   // tránh chạy chồng nhiều ticker
        handler.post(ticker);
    }

    private void stopTicking() {
        handler.removeCallbacks(ticker);
    }

    // Nâng cao NC1: Thêm một vòng đếm thời gian hiện tại vào danh sách
    private void addLap() {
        long ms = elapsed();
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;
        String lapTime = String.format(Locale.getDefault(), "Vòng %d: %02d:%02d.%d",
                lapList.size() + 1, phut, giay, phanMuoi);
        lapList.add(0, lapTime); // Đưa vòng mới nhất lên đầu danh sách
        updateLapsDisplay();
        Log.i(TAG, "ĐÃ GHI VÒNG: " + lapTime);
    }

    // NC1: Cập nhật TextView hiển thị danh sách Vòng
    private void updateLapsDisplay() {
        if (lapList.isEmpty()) {
            tvLaps.setText("Chưa có vòng nào được ghi.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String lap : lapList) {
                sb.append(lap).append("\n");
            }
            tvLaps.setText(sb.toString().trim());
        }
    }

    // NC3: Rung nhẹ thiết bị khi bấm nút Reset
    private void vibrateDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.Vibrator_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    Vibrator vibrator = vibratorManager.getDefaultVibrator();
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(100);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Không thể kích hoạt rung: " + e.getMessage());
        }
    }

    // ---------------- Cập nhật giao diện ----------------

    private void updateTimeText() {
        long ms = elapsed();
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;

        tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d.%d", phut, giay, phanMuoi));

        // Nâng cao NC3: Đổi màu chữ con số thành màu đỏ nếu vượt quá 60 giây (60000ms)
        if (ms >= 60000) {
            tvTime.setTextColor(Color.parseColor("#FF1744")); // Màu đỏ rực
        } else {
            tvTime.setTextColor(Color.parseColor("#000000")); // Màu đen mặc định
        }
    }

    private void updateUi() {
        updateTimeText();
        btnStartPause.setText(running ? R.string.pause : R.string.start);
        tvStatus.setText(running ? R.string.status_running : R.string.status_paused);
        tvRecreate.setText(getString(R.string.recreate_count, recreateCount));
        updateLapsDisplay();
    }

    // ---------------- Vòng đời ----------------

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume – bật lại việc cập nhật giao diện nếu đồng hồ đang chạy");
        if (running) {
            startTicking();
        }
        updateUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dừng cập nhật giao diện để tiết kiệm pin; đồng hồ vẫn tính đúng
        // vì thời gian được suy ra từ mốc SystemClock.elapsedRealtime().
        stopTicking();
        Log.d(TAG, "onPause – tạm dừng cập nhật giao diện");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        // Nâng cao NC2: Nếu CheckBox "Dừng khi ra nền" được chọn, tự động tạm dừng đồng hồ
        if (cbPauseOnStop.isChecked() && running) {
            pauseStopwatch();
            Log.i(TAG, "onStop – NC2: Tự động tạm dừng đồng hồ vì CheckBox [Dừng khi ra nền] được tích.");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        stopTicking();               // luôn gỡ callback để tránh rò rỉ bộ nhớ
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // ---------------- Lưu & khôi phục trạng thái ----------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_RUNNING, running);
        outState.putLong(KEY_ACCUMULATED, accumulated);
        outState.putLong(KEY_START, startTime);
        outState.putInt(KEY_RECREATE, recreateCount);

        // Nâng cao NC1: Lưu danh sách Vòng
        outState.putStringArrayList(KEY_LAPS, lapList);

        // Nâng cao NC2: Lưu trạng thái CheckBox
        outState.putBoolean(KEY_PAUSE_ON_STOP, cbPauseOnStop.isChecked());

        Log.d(TAG, "onSaveInstanceState – đã lưu " + elapsed() + "ms vào Bundle (NC1 Laps: " + lapList.size() + ")");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState – được gọi sau onStart()");
    }
}
