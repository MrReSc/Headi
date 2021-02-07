package com.example.headi.ui.export;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.headi.R;

public class ExportFragment extends Fragment {

    private ExportViewModel exportViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exportViewModel =
                new ViewModelProvider(this).get(ExportViewModel.class);
        View root = inflater.inflate(R.layout.fragment_export, container, false);
        final TextView textView = root.findViewById(R.id.text_export);
        exportViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}