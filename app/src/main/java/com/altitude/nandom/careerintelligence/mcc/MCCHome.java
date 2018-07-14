package com.altitude.nandom.careerintelligence.mcc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.altitude.nandom.careerintelligence.InformationQuery;
import com.altitude.nandom.careerintelligence.R;
import com.altitude.nandom.careerintelligence.apolloclient.MyApolloClient;
import com.altitude.nandom.careerintelligence.classes.ConnectivityReceiver;
import com.altitude.nandom.careerintelligence.classes.SessionManager;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.exception.ApolloException;

import java.util.HashMap;

import javax.annotation.Nonnull;


/**
 * A simple {@link Fragment} subclass.
 */
public class MCCHome extends Fragment {

    private CardView mccPaymentCard;

    private SessionManager sessionManager;

    private Boolean isActivated;

    private String token;


    public static MCCHome newInstance() {
        // Required empty public constructor
        MCCHome fragment = new MCCHome();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mcc_home, container, false);

        mccPaymentCard = (CardView)view.findViewById(R.id.mccPaymentCard);

        sessionManager = new SessionManager(getContext());

        // get user data from session
        HashMap<String, String> user = sessionManager.getUserDetails();

        // token
        token = user.get(SessionManager.KEY_JWT);


        mccPaymentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkConnection())
                    getSecret2(token);
                else
                    Toast.makeText(getContext(), "Please Check connection and try again", Toast.LENGTH_SHORT).show();

            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    private void getSecret2(String token) {

        MyApolloClient.getUsingTokenHeader(token).query(InformationQuery.builder().build()).enqueue(new ApolloCall.Callback<InformationQuery.Data>() {
            @Override
            public void onResponse(@Nonnull com.apollographql.apollo.api.Response<InformationQuery.Data> response) {


                isActivated = response.data().viewerCandidate().candidate().isActivated();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isActivated) {
                            Toast.makeText(getActivity(), "Please activate account to make payment", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent mccPaymentIntent = new Intent(getContext(), MCCPayment.class);
                            startActivity(mccPaymentIntent);
                        }

                    }
                });

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "This thing has failed ooo", Toast.LENGTH_SHORT).show();
                        Log.d("MainResponse", "" + e);
                    }
                });

            }
        });


    }

    // Method to manually check connection status
    private boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        return isConnected;

    }

}