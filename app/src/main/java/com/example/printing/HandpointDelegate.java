package com.example.printing;

import android.content.Context;

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
import java.util.Arrays;
import java.util.List;

public class HandpointDelegate implements Events.Required, Events.ConnectionStatusChanged, Events.CurrentTransactionStatus  {

    private Hapi api;

    public HandpointDelegate(Context context) {
        initApi(context);
    }

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



}
