package esme.transport.transport;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    RadioGroup rg;
    Button valid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rg = (RadioGroup)findViewById(R.id.ChoixTransport);
        valid = (Button) findViewById(R.id.button_main);

        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectId = rg.getCheckedRadioButtonId();

                if (R.id.radioButtonPied == selectId) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("choixTransport", "Marche");
                    startActivity(intent);
                }
                else if (R.id.radioButtonVelo == selectId) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("choixTransport", "Velo");
                    startActivity(intent);
                }
                else if (R.id.radioButtonVoiture == selectId) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("choixTransport", "Voiture");
                    startActivity(intent);
                }
                else if (R.id.radioButtonBus == selectId) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("choixTransport", "Bus");
                    startActivity(intent);
                }
                else if (R.id.radioButtonTrain == selectId) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("choixTransport", "Train");
                    startActivity(intent);
                }
            }
        });
    }
}