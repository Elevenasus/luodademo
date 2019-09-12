package com.airoha.android.spp.headset.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.airoha.android.spp.headset.OTA.OtaActivity;
import com.airoha.android.spp.headset.R;

public class FragmentSelectMode extends Fragment{
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_selectmode, container, false);

        Button updaterombutton = (Button) view
                .findViewById(R.id.updaterombutton);
        updaterombutton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), OtaActivity.class));
            }
        });

//        Button btnVoiceCommand = (Button) view.findViewById(R.id.btnVoiceCmd);
//        btnVoiceCommand.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                invokeVoiceCommand();
//            }
//        });
        return view;
    }


//    private void invokeVoiceCommand() {
//        startActivity(new Intent(this.getActivity(), SpeakerAdaptationActivity.class));
//    }
}