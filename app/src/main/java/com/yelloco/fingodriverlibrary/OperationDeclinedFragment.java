package com.yelloco.fingodriverlibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OperationDeclinedFragment extends Fragment
{
    // Members
    private Integer declinedTextResId;

    // Views
    private TextView declinedTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_operation_declined, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews(View view){
        declinedTextView = view.findViewById(R.id.operation_declined_screen_message_text_view);
        if(declinedTextResId != null){
            declinedTextView.setText(declinedTextResId);
        }
    }

    public void setDeclinedText(Integer declinedTextResId) {
        this.declinedTextResId = declinedTextResId;
    }
}