package com.luckywarepro.musicintervals2anki.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luckywarepro.musicintervals2anki.BuildConfig;
import com.luckywarepro.musicintervals2anki.R;

import java.util.Objects;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Copyright (c) 2021 LuckyWare Pro. (Apache-2.0 License)
 */

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbarAbout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layoutAboutRoot, new AboutFragment())
                .commit();
    }

    public static class AboutFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Element versionElement = new Element()
                    .setTitle(getString(R.string.version, BuildConfig.VERSION_NAME));

            Uri uri = Uri.parse(getString(R.string.uri_github));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            Element repositoryElement = new Element()
                    .setTitle(getString(R.string.github))
                    .setIconDrawable(R.drawable.ic_external_link)
                    .setIntent(intent);

            Element copyrightElement = new Element()
                    .setTitle(getString(R.string.copyright));

            Element licenseElement = new Element()
                    .setTitle(getString(R.string.license));

            return new AboutPage(getContext(), true)
                    .setImage(R.drawable.ic_launcher_foreground)
                    .setDescription(getString(R.string.description))
                    .addItem(versionElement)
                    .addItem(repositoryElement)
                    .addItem(copyrightElement)
                    .addItem(licenseElement)
                    .create();
        }
    }
}