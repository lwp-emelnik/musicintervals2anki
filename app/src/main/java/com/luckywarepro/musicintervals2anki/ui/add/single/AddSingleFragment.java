package com.luckywarepro.musicintervals2anki.ui.add.single;

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

import com.luckywarepro.musicintervals2anki.R;

public class AddSingleFragment extends Fragment {

    private AddSingleViewModel addSingleViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        addSingleViewModel =
                new ViewModelProvider(this).get(AddSingleViewModel.class);
        View root = inflater.inflate(R.layout.fragment_add_single, container, false);
        final TextView textView = root.findViewById(R.id.text_add_single);
        addSingleViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}