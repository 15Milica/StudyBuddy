package com.example.studybuddy.ui.pages;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class PagesFragment extends Fragment {

    private ImageButton newPageButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pages, container, false);

        newPageButton = (ImageButton) root.findViewById(R.id.imageButtonNewPage);
        newPageButton.setOnClickListener(view->onNewPageClick());
        return root;
    }
    private void onNewPageClick()
    {
        Intent intent =new Intent(getActivity(), PageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

}