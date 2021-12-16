package com.example.printing;

import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;
import com.telpo.tps550.api.util.ShellUtils;
=======
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
>>>>>>> 687c1bd87053619cdd1e99b114ca8c34fcee2e35

import com.handpoint.api.HandpointCredentials;
import com.handpoint.api.Hapi;
import com.handpoint.api.HapiFactory;
import com.handpoint.api.shared.ConnectionMethod;
import com.handpoint.api.shared.ConnectionStatus;
import com.handpoint.api.shared.Currency;
import com.handpoint.api.shared.Device;
import com.handpoint.api.shared.Events;
import com.handpoint.api.shared.SignatureRequest;
import com.handpoint.api.shared.StatusInfo;
import com.handpoint.api.shared.TipConfiguration;
import com.handpoint.api.shared.TransactionResult;
import com.handpoint.api.shared.agreements.Acquirer;
import com.handpoint.api.shared.agreements.Credential;
import com.handpoint.api.shared.agreements.MerchantAuth;
import com.handpoint.api.shared.options.SaleOptions;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity implements Events.Required, Events.ConnectionStatusChanged, Events.CurrentTransactionStatus {

    Button mainButton;

    


    private Hapi api;

//    public MainActivity(){
//
//    }
//
//    public MainActivity(Context context) {
//        initApi(context);
//
//    }

    public void initApi(Context context){
        String sharedSecret = "0102030405060708091011121314151617181920212223242526272829303132";
        HandpointCredentials handpointCredentials = new HandpointCredentials(sharedSecret);
        this.api = HapiFactory.getAsyncInterface(this, context, handpointCredentials);
        // The api is now initialized. Yay! we've even set default credentials.
        // The shared secret is a unique string shared between the payment terminal and your application, it is unique per merchant.
        // You should replace this default shared secret with the one sent by the Handpoint support team.

        //Since we're running inside the terminal, we can create a device ourselves and connect to it
        Device device = new Device("some name", "address", "", ConnectionMethod.ANDROID_PAYMENT);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
        //handler = new MyHandler();
        print();
    }

    public void print(){
        //handler.sendMessage(handler.obtainMessage(PRINTPAPERWALK, 1, 0, null));
        //new paperWalkPrintThread().start();
        preference = getSharedPreferences("TPS390PRINTER", MODE_PRIVATE);
        editor = preference.edit();
        handler=new MyHandler();
        new contentPrintThread().start();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PRINTPAPERWALK:
                    new paperWalkPrintThread().start();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Print Error!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    String[] portNum = new String[20];
    String[] productNum = new String[20];
    String[] readerNum = new String[4];
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    UsbThermalPrinter mUsbThermalPrinter = new UsbThermalPrinter(MainActivity.this);
    boolean isHighSpeed = false;
    private String printVersion;
    private final int PRINTVERSION = 5;
    MyHandler handler;
    public static int paperWalk;
    private String Result;
    private Boolean nopaper = false;
    private final int PRINTERR = 11;
    private final int OVERHEAT = 12;
    private final int CANCELPROMPT = 10;
    private final int NOPAPER = 3;
    private final int PRINTPAPERWALK = 8;
    private int leftDistance=0;
    private int lineDistance=0;
    private boolean isBold=false;
    private int printGray=5;
    private int wordFont=24;
    private String printContent="Text \n Text  " +
            "\n Text";

    private String getUsbPrinterDev(){
        String msgSuccess = ShellUtils.execCommand("cat /proc/bus/usb/devices", false).successMsg;
        searchAllIndex(msgSuccess,"Dev#=",1);
        searchAllIndex(msgSuccess,"Product=",2);
        return checkPort(portNum,productNum);
    }

    private String checkPort(String[] port,String[] product) {
        int k = -1;
        for(int i=0;i<20;i++) {
            if(productNum[i] != null &&productNum[i].equals("USB Thermal Printer")) {
                k++;
                readerNum[k] = portNum[i];
                Log.d("tagg", "readnum[]:"+readerNum[k]);
                editor.putString("usbPrinterDev", readerNum[k]);
                editor.commit();
                return readerNum[k];
            }
        }
        editor.putString("usbPrinterDev", "-2");
        editor.commit();
        return "-2";
    }

    private void searchAllIndex(String str,String key,int type) {
        if(str != null && !str.equals("")){
            int a = str.indexOf(key);
            int i=-1;
            while (a != -1) {
                i++;
                if(type ==1) {
                    portNum[i] = str.substring(a+5, a+8);
                    Log.d("tagg", "portNum["+i+"]:"+portNum[i]);
                }
                else if(type ==2) {
                    productNum[i] = str.substring(a+8, a+27);
                    Log.d("tagg", "portNum["+i+"]:"+portNum[i]);
                }
                a = str.indexOf(key, a + 1);//*浠庤繖涓储寮曞線鍚庡紑濮嬬涓�涓嚭鐜扮殑浣嶇疆
            }
        }
    }

    private void getVersion(){
        try {
            Log.d("tagg", "getVersion");
            if(isHighSpeed){
                mUsbThermalPrinter.start(1);
            }else{
                mUsbThermalPrinter.start(0);
            }
            mUsbThermalPrinter.reset();
            printVersion = mUsbThermalPrinter.getVersion();
            Log.d("tagg", "printVersion:"+printVersion);
        } catch (TelpoException e) {
            e.printStackTrace();
        } finally {
            if (printVersion != null) {
                Message message = new Message();
                message.what = PRINTVERSION;
                message.obj = "1";
                handler.sendMessage(message);
            } else {
                Message message = new Message();
                message.what = PRINTVERSION;
                message.obj = "0";
                handler.sendMessage(message);
            }
        }
    }

    private class paperWalkPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                String temp = getUsbPrinterDev();
                Log.d("tagg", "getUsbPrinterDev():"+temp);
                Log.d("tagg", "preference:"+preference.getString("usbPrinterDev", "-1"));
                if(temp.equals("-2") || !temp.equals(preference.getString("usbPrinterDev", "-1"))){
                    mUsbThermalPrinter.stop();
                    Thread.sleep(250);
                    if(isHighSpeed){
                        mUsbThermalPrinter.start(1);
                    }else{
                        mUsbThermalPrinter.start(0);
                    }
                    getVersion();
                }
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.walkPaper(paperWalk);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class contentPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                String temp = getUsbPrinterDev();
                Log.d("tagg", "getUsbPrinterDev():"+temp);
                Log.d("tagg", "preference:"+preference.getString("usbPrinterDev", "-1"));
                if(temp.equals("-2") || !temp.equals(preference.getString("usbPrinterDev", "-1"))){
                    mUsbThermalPrinter.stop();
                    Thread.sleep(250);
                    if(isHighSpeed){
                        mUsbThermalPrinter.start(1);
                    }else{
                        mUsbThermalPrinter.start(0);
                    }
                    getVersion();
                }
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setLeftIndent(leftDistance);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                mUsbThermalPrinter.setBold(isBold);
                mUsbThermalPrinter.setTextSize(wordFont);
                //mUsbThermalPrinter.setHighlight(true);
				/*if (isSupportAutoBreak()) {
					mUsbThermalPrinter.autoBreakSet(button_auto_linefeed.isChecked());
				}*/
                mUsbThermalPrinter.setGray(printGray);
                mUsbThermalPrinter.addString(printContent);
                mUsbThermalPrinter.printString();
                mUsbThermalPrinter.walkPaper(20);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
=======
        trustEveryone();
        HttpsTrustManager.allowAllSSL();
        initApi(this);

        mainButton = findViewById(R.id.mainButton);

        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payWithOptions();

            }
        });
    }


    @Override
    public void connectionStatusChanged(ConnectionStatus connectionStatus, Device device) {
        if (connectionStatus == ConnectionStatus.Connected){
            //Connection Status connected
        }

    }

    @Override
    public void deviceDiscoveryFinished(List<Device> list) {
        //This event can be safely ignored for a PAX/Telpo integration

    }

    public boolean pay(){
        return this.api.sale(new BigInteger("1000"), Currency.GBP);
        //Let's start our first payment of 10 pounds
        //Use the currency of the country in which you will be deploying terminals

    }

    public boolean payWithOptions(){
        SaleOptions options = new SaleOptions();

        //Adding tipping
        TipConfiguration config = new TipConfiguration();

        //Optionally
        config.setHeaderName("Header");

        //Optionally
        config.setFooter("Footer");

        //Optionally
        config.setEnterAmountEnabled(true);

        //Optionally
        config.setSkipEnabled(true);

        //Optionally
        config.setTipPercentages(Arrays.asList(5,10,15,20));
        options.setTipConfiguration(config);

        //Adding Multi MID / Custom merchant Authentication
        MerchantAuth auth = new MerchantAuth();
        Credential credential = new Credential();

        //Optionally
        credential.setAcquirer(Acquirer.SANDBOX);

        //Optionally
        credential.setMid("mid");

        //Optionally
        credential.setTid("tid");

        //Add as many credentials as Acquirers your merchant have agreements with
        auth.add(credential);
        options.setMerchantAuth(auth);

        //Add a customer reference
        options.setCustomerReference("Your customer reference");

        //Enable pin bypass
        options.setPinBypass(true);

        //Enable signature bypass
        options.setSignatureBypass(true);

        //Define a budget number
        options.setBudgetNumber("YOUR_BUDGET_NUMBER");

        //this.api.sale(new BigInteger("1000"),Currency.GBP, options))
        //trustEveryone();

        return this.api.sale(new BigInteger("1000"),Currency.GBP, options);
    }

    @Override
    public void currentTransactionStatus(StatusInfo statusInfo, Device device) {
        if (statusInfo.getStatus() == StatusInfo.Status.InitialisationComplete){
            //The StatusInfo object holds the different transaction statuses like reading card, pin entry, etc.
            //Let's launch a payment
            pay();

        }

    }

    @Override
    public void signatureRequired(SignatureRequest signatureRequest, Device device) {
        //This event can be saely ignored for a PAX/Telpo integration
        //The complete signature capture process is already handled in the sdk, a dialog will prompt the user for a signature if required
        //If a signature was entered, it should be printed as receipts

>>>>>>> 687c1bd87053619cdd1e99b114ca8c34fcee2e35
    }




    @Override
    public void endOfTransaction(TransactionResult transactionResult, Device device) {
        //The TransactionResult object holds details about the transaction as well as the receipts
        //Useful information can be accessed through this object like the transaction ID, the amount etc

    }

    @Override
    public void transactionResultReady(TransactionResult transactionResult, Device device) {
        //Pending TransactionResult objects will eb received through this event if the EndOfTransaction
        //Event was not delivered during the transaction, for example because of a network issue
        //FOr this sample app we are not going to implement this event

    }

    public void disconnect(){
        this.api.disconnect();
        //THis disconnects the connection
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }



}