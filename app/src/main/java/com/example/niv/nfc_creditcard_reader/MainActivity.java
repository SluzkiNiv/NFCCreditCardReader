package com.example.niv.nfc_creditcard_reader;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    TextView et;
    public static NfcAdapter nfcAdapter;
    public static PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initalization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et = (TextView) findViewById(R.id.printCard);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);


        // Set NFC listener
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set NFC event handler
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }


    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NFC event handler
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){

            // Get tag object
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // Declare provider
            Provider prov = new Provider();
            prov.setmTagCom(IsoDep.get(tag));

            // Create parser (true for contactless false otherwise)
            EmvParser parser = new EmvParser(prov, true);

            // Read card
            try {
                EmvCard card = parser.readEmvCard();
                String msg = card.getCardNumber().toString() + "   " + card.getType() + "  " + getDate(toCalendar(card.getExpireDate()));
                Object printCard = null;
                et.setText(et.getText() + "\n\n" + msg);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            } catch (CommunicationException e) {
                Toast.makeText(this,"Reader moved too fast. Please attach again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public static String getDate(Calendar c){
        int Month = c.get(Calendar.MONTH);
        if (Month != 12 ) {
            Month++;
        }
        int Year = c.get(Calendar.YEAR) % 100;

        return (Integer.toString(Month) + "/" + Integer.toString(Year));
    }

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}

