package com.airoha.android.spp.headset.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.airoha.android.spp.headset.service.ConnService;


public class BaseFragment extends Fragment {

    // ref: http://www.jianshu.com/p/9d75e328f1de
    private  Context mCtx;

    @Override
    public void onDestroyView() {
        hideKeyboard();

        super.onDestroyView();
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCtx = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCtx = null;
    }

    protected void sendCommandToService(byte[] commond) {
        Intent intent = new Intent(ConnService.ACTION_SEND_QUEUED);
        intent.putExtra(ConnService.EXTRA_CMD, commond);
//        this.getActivity().sendBroadcast(intent);

        // 2017.1.18 Daniel: Mantis#9440, null pointer after Activity destroyed
        if(mCtx != null){
            mCtx.sendBroadcast(intent);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}