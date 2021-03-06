package com.headi.app.ui.about;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.headi.app.BuildConfig;
import com.headi.app.R;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class about extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Element versionElement = new Element();
        versionElement.setTitle(BuildConfig.VERSION_NAME);

        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean nightModeEnabled = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        return new AboutPage(getContext())
                .isRTL(false)
                .enableDarkMode(nightModeEnabled)
                .setImage(R.drawable.ic_headi_about)
                .setDescription(getString(R.string.app_description))
                .addGroup(getString(R.string.contact_group))
                .addEmail(getString(R.string.contact_email), getString(R.string.title_email))
                .addGitHub(getString(R.string.contact_github), getString(R.string.title_github))
                .addGroup(getString(R.string.contribute_group))
                .addWebsite(getString(R.string.contact_website), getString(R.string.title_website))
                .addWebsite(getString(R.string.translation_crowdin), getString(R.string.title_translate))
                .addGroup(getString(R.string.app_information_group))
                .addItem(versionElement)
                .addItem(getCopyRightsElement())
                .create();
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        String copyrights = getString(R.string.copy_right) + " " + Calendar.getInstance().get(Calendar.YEAR);
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.about_item_icon_color);
        copyRightsElement.setAutoApplyIconTint(true);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MrReSc/Headi#license"));
            startActivity(browserIntent);
        });
        return copyRightsElement;
    }
}