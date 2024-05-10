package bku.iot.iotclient;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.DayNightSwitch;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi;
    DayNightSwitch toggle_light;
    LabeledSwitch toggle_limit;
    LinearLayout background;
    EditText set_limit;
    EditText time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        background = findViewById(R.id.main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        toggle_light = findViewById(R.id.toggle_light);
        toggle_limit = findViewById(R.id.toggle_limit);
        set_limit = findViewById(R.id.set_limit);
        time = findViewById(R.id.time);
        toggle_light.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {
                    sendDataMQTT("lxa_dashboard_iot/feeds/V3", "1");
                    background.setBackgroundResource(R.drawable.day);
                } else {
                    sendDataMQTT("lxa_dashboard_iot/feeds/V3", "0");
                    background.setBackgroundResource(R.drawable.night);
                }
            }
        });

        toggle_limit.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {
                    sendDataMQTT("lxa_dashboard_iot/feeds/V4", "1");
                } else {
                    sendDataMQTT("lxa_dashboard_iot/feeds/V4", "0");
                }
            }
        });

        set_limit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String text = set_limit.getText().toString();
                    sendDataMQTT("lxa_dashboard_iot/feeds/V5", text);
                    return true;
                }
                return false;
            }
        });

        time.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String text = time.getText().toString();
                    sendDataMQTT("lxa_dashboard_iot/feeds/V6", text);
                    return true;
                }
                return false;
            }
        });

        startMQTT();
    }

    public void sendDataMQTT(String topic, String value) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(StandardCharsets.UTF_8);
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        } catch (MqttException ignored){
        }
    }
    public void startMQTT() {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + ":::" + message.toString());
                if(topic.contains("V1")) {
                    txtTemp.setText(message.toString() + "°C");
                } else if (topic.contains("V2")) {
                    txtHumi.setText(message.toString() + "%");
                } else if (topic.contains("V3")) {
                    if (message.toString().equals("1")) {
                        toggle_light.setOn(true);
                        background.setBackgroundResource(R.drawable.day);
                    } else {
                        toggle_light.setOn(false);
                        background.setBackgroundResource(R.drawable.night);
                    }
                } else if (topic.contains("V4")) {
                    if (message.toString().equals("1")) {
                        toggle_limit.setOn(true);
                    } else {
                        toggle_limit.setOn(false);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}