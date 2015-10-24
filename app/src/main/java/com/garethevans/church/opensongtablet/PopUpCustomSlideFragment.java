package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;

public class PopUpCustomSlideFragment extends DialogFragment {

   static PopUpCustomSlideFragment newInstance() {
        PopUpCustomSlideFragment frag;
        frag = new PopUpCustomSlideFragment();
        return frag;
    }

    public interface MyInterface {
        void addSlideToSet();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    // Declare views
    static View V;
    static RadioGroup customRadioGroup;
    static RadioButton noteRadioButton;
    static RadioButton slideRadioButton;
    static RadioButton imageRadioButton;
    static TextView slideTitleTextView;
    static TextView slideContentTextView;
    static EditText slideTitleEditText;
    static EditText slideContentEditText;
    static Button customSlideCancel;
    static Button customSlideAdd;
    static Button loadReusableButton;
    static CheckBox saveReusableCheckBox;
    static Button addPageButton;
    static TableLayout slideImageTable;
    static CheckBox loopCheckBox;
    static TextView timeTextView;
    static EditText timeEditText;
    static TextView warningTextView;

    // Declare variables used
    static String whattype = "note";

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_song_edit));
        V = inflater.inflate(R.layout.popup_customslidecreator, container, false);

        // Initialise the basic views
        customRadioGroup = (RadioGroup) V.findViewById(R.id.customRadioGroup);
        noteRadioButton = (RadioButton) V.findViewById(R.id.noteRadioButton);
        slideRadioButton = (RadioButton) V.findViewById(R.id.slideRadioButton);
        imageRadioButton = (RadioButton) V.findViewById(R.id.imageRadioButton);
        slideTitleTextView = (TextView) V.findViewById(R.id.slideTitleTextView);
        slideContentTextView = (TextView) V.findViewById(R.id.slideContentTextView);
        slideTitleEditText = (EditText) V.findViewById(R.id.slideTitleEditText);
        slideContentEditText = (EditText) V.findViewById(R.id.slideContentEditText);
        customSlideCancel = (Button) V.findViewById(R.id.customSlideCancel);
        customSlideAdd = (Button) V.findViewById(R.id.customSlideAdd);
        addPageButton = (Button) V.findViewById(R.id.addPageButton);
        loadReusableButton = (Button) V.findViewById(R.id.loadReusableButton);
        saveReusableCheckBox = (CheckBox) V.findViewById(R.id.saveReusableCheckBox);
        slideImageTable = (TableLayout) V.findViewById(R.id.slideImageTable);
        loopCheckBox = (CheckBox) V.findViewById(R.id.loopCheckBox);
        timeTextView = (TextView) V.findViewById(R.id.timeTextView);
        timeEditText = (EditText) V.findViewById(R.id.timeEditText);
        warningTextView = (TextView) V.findViewById(R.id.warningTextView);

       if (FullscreenActivity.whattodo.contains("customreusable_")) {
           updateFields();
       } else {
           // By default we want to make a brief note/placeholder
           noteRadioButton.setChecked(true);
           FullscreenActivity.whattodo = "customnote";
           slideRadioButton.setChecked(false);
           imageRadioButton.setChecked(false);
           saveReusableCheckBox.setChecked(false);
           switchViewToNote();
       }

        // Set button listeners
        addPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (whattype.equals("slide")) {
                    String newText = slideContentEditText.getText().toString().trim() + "\n---\n";
                    newText = newText.trim() + "\n";
                    slideContentEditText.setText(newText);
                } else if (whattype.equals("image")) {
                    // Call file browser
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("file/");
                    try {
                        startActivityForResult(i, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        FullscreenActivity.myToastMessage = getResources().getString(R.string.no_filemanager);
                        ShowToast.showToast(getActivity());
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.estrongs.android.pop")));
                        } catch (Exception anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.estrongs.android.pop")));
                        }
                    }

                }
            }
        });
        customSlideCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        customSlideAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.noteorslide = whattype;
                String text = slideContentEditText.getText().toString().trim();
                FullscreenActivity.customreusable = saveReusableCheckBox.isChecked();
                String imagecontents;
                if (whattype.equals("image")) {
                    imagecontents = "";
                    // Go through images in list and extract the full location and the filename
                    Log.d("table", "getChildCount=" + slideImageTable.getChildCount());
                    for (int r = 0; r < slideImageTable.getChildCount(); r++) {
                        // Look for image file location
                        if (slideImageTable.getChildAt(r) instanceof TableRow) {
                            TextView tv = (TextView) ((TableRow) slideImageTable.getChildAt(r)).getChildAt(0);
                            String tv_text = tv.getText().toString();
                            imagecontents = imagecontents + tv_text + "\n";
                        }
                    }

                    while (imagecontents.contains("\n\n")) {
                        imagecontents = imagecontents.replace("\n\n", "\n");
                    }
                    imagecontents = imagecontents.trim();
                    String[] individual_images = imagecontents.split("\n");

                    // Prepare the lyrics
                    text = "";
                    for (int t = 0; t < individual_images.length; t++) {
                        text = text + "[" + FullscreenActivity.image + "_" + (t + 1) + "]\n" + individual_images[t] + "\n\n";
                    }
                    text = text.trim();

                } else {
                    imagecontents = "";
                }
                FullscreenActivity.customslide_title = slideTitleEditText.getText().toString();
                FullscreenActivity.customslide_content = text;
                FullscreenActivity.customimage_list = imagecontents;
                FullscreenActivity.customimage_loop = "" + loopCheckBox.isChecked() + "";
                FullscreenActivity.customimage_time = timeEditText.getText().toString();
                // Check the slide has a title.  If not, use _
                if (FullscreenActivity.customslide_title == null || FullscreenActivity.customslide_title.equals("") || FullscreenActivity.customslide_title.isEmpty()) {
                    FullscreenActivity.customslide_title = "_";
                }
                mListener.addSlideToSet();
                dismiss();
            }
        });
        loadReusableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This reopens the choose backgrounds popupFragment
                dismiss();
                DialogFragment newFragment = PopUpFileChooseFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
            }
        });
        customRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (noteRadioButton.isChecked()) {
                    switchViewToNote();
                } else if (slideRadioButton.isChecked()) {
                    switchViewToSlide();
                } else {
                    switchViewToImage();
                }
            }
        });

       return V;
    }

   public void updateFields() {
        switch (FullscreenActivity.whattodo) {
            case "customreusable_note":
                // Fill in the details
                noteRadioButton.setChecked(true);
                slideRadioButton.setChecked(false);
                imageRadioButton.setChecked(false);
                switchViewToNote();
                slideTitleEditText.setText(FullscreenActivity.customslide_title);
                slideContentEditText.setText(FullscreenActivity.customslide_content);
                break;
            case "customreusable_slide":
                // Fill in the details
                noteRadioButton.setChecked(false);
                slideRadioButton.setChecked(true);
                imageRadioButton.setChecked(false);
                switchViewToSlide();
                slideTitleEditText.setText(FullscreenActivity.customslide_title);
                slideContentEditText.setText(FullscreenActivity.customslide_content);
                timeEditText.setText(FullscreenActivity.customimage_time);
                if (FullscreenActivity.customimage_loop.equals("true")) {
                    loopCheckBox.setChecked(true);
                } else {
                    loopCheckBox.setChecked(false);
                }
                break;
            case "customreusable_image":
                // Fill in the details
                noteRadioButton.setChecked(false);
                slideRadioButton.setChecked(false);
                imageRadioButton.setChecked(true);
                switchViewToImage();
                slideTitleEditText.setText(FullscreenActivity.customslide_title);
                slideContentEditText.setText("");
                timeEditText.setText(FullscreenActivity.customimage_time);
                if (FullscreenActivity.customimage_loop.equals("true")) {
                    loopCheckBox.setChecked(true);
                } else {
                    loopCheckBox.setChecked(false);
                }
                // Now parse the list of images...
                String imgs[] = FullscreenActivity.customimage_list.split("\n");
                slideImageTable.removeAllViews();
                for (String img : imgs) {
                    addRow(img);
                }
                break;
        }
    }

    public static void switchViewToNote() {
        whattype = "note";
        FullscreenActivity.whattodo ="customnote";
        addPageButton.setVisibility(View.GONE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        timeEditText.setVisibility(View.GONE);
        warningTextView.setVisibility(View.GONE);
    }

    public static void switchViewToSlide() {
        whattype = "slide";
        FullscreenActivity.whattodo ="customslide";
        addPageButton.setVisibility(View.VISIBLE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        timeEditText.setVisibility(View.VISIBLE);
        warningTextView.setVisibility(View.GONE);
    }

    public static void switchViewToImage() {
        whattype = "image";
        FullscreenActivity.whattodo ="customimage";
        addPageButton.setVisibility(View.VISIBLE);
        slideContentEditText.setVisibility(View.GONE);
        slideImageTable.setVisibility(View.VISIBLE);
        loopCheckBox.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        timeEditText.setVisibility(View.VISIBLE);
        warningTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent!=null) {
            Uri uri = intent.getData();
            Cursor cursor = null;
            String fullpath = null;
            try {
                String[] proj = { MediaStore.Images.Media.DATA };
                cursor = getActivity().getContentResolver().query(uri,  proj, null, null, null);
                int column_index = 0;
                if (cursor != null) {
                    column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                }
                try {
                    cursor.moveToFirst();
                    fullpath = cursor.getString(column_index);
                } catch (Exception e) { }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            if (requestCode==0) {
                // Create a new row in the table
                // Each row has the file name, an image thumbnail and a delete button
                if (fullpath!=null) {
                    addRow(fullpath);
                }
            }
        }
    }

    public void addRow(String fullpath) {
        TableRow row = new TableRow(getActivity());
        TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutRow);
        row.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        row.setTag(fullpath);
        TextView filename = new TextView(getActivity());
        filename.setText(fullpath);
        filename.setTextSize(0.0f); // Make it take up no space (user doesn't need to see it).
        filename.setVisibility(View.GONE);
        ImageView thumbnail = new ImageView(getActivity());
        Bitmap ThumbImage;
        Resources res = getResources();
        BitmapDrawable bd;
        File checkfile = new File(fullpath);
        if (!checkfile.exists()) {
            Drawable notfound = getResources().getDrawable(R.drawable.notfound);
            thumbnail.setImageDrawable(notfound);
        } else {
            ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(fullpath), 200, 150);
            bd = new BitmapDrawable(res, ThumbImage);
            thumbnail.setImageDrawable(bd);
        }
        thumbnail.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_black));
        thumbnail.setMaxWidth(200);
        thumbnail.setMaxHeight(150);
        TableRow.LayoutParams layoutImage = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutImage.width = 200;
        layoutImage.height = 150;
        thumbnail.setLayoutParams(layoutImage);
        ImageButton delete = new ImageButton (getActivity());
        delete.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_discard));
        delete.setTag(fullpath + "_delete");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rowtag = v.getTag().toString();
                rowtag = rowtag.replace("_delete", "");
                try {
                    slideImageTable.removeView(getView().findViewWithTag(rowtag));
                } catch (Exception e) {
                    // oh well
                    Log.d("error", "No table row with this tag");
                }
                Log.d("remove", "rowtag=" + rowtag);
            }
        });
        row.addView(filename);
        row.addView(thumbnail);
        row.addView(delete);
        slideImageTable.addView(row);
    }
}