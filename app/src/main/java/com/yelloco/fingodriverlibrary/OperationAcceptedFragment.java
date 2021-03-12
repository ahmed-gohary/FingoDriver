package com.yelloco.fingodriverlibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class OperationAcceptedFragment extends Fragment
{
    // Members
    private Integer acceptedTextResId;

    // Views
    private TextView welcomeTextView;
    private TextView nameTextView;
    private TextView acceptedTextView;

    private String name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_operation_accepted, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews(View view){
        acceptedTextView = view.findViewById(R.id.operationAccepted);

        if (name != null)
        {
            nameTextView.setText(name);
        }
        else {
            welcomeTextView.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
        }

        if(acceptedTextResId != null){
            acceptedTextView.setText(acceptedTextResId);
        }
    }

    public void setNameText(String name)
    {
        this.name = name;
    }

    public void setAcceptedText(Integer acceptedText) {
        this.acceptedTextResId = acceptedText;
    }
}