package com.totsp.bookworm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.totsp.bookworm.model.Book;
import com.totsp.bookworm.model.Folder;

public class FolderEntryResult extends Activity {


    BookWormApplication application;

    // package scope for use in inner class (Android optimization)
    Button folderAdd;
    EditText folderName;



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folderentryresult);
        application = (BookWormApplication) getApplication();

        folderName = (EditText) findViewById(R.id.FolderForm);
        folderAdd = (Button) findViewById(R.id.folderentrybutton);

        folderAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                if (folderAdd != null){

                   saveFolder();
                } else {
                    Toast.makeText(FolderEntryResult.this, getString(R.string.msgMinimumSave), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveFolder(){
        String name = folderName.getText().toString();
        Toast.makeText(FolderEntryResult.this, "Folder Succesfully Added = " + name , Toast.LENGTH_LONG).show();
        Folder tobecreated = new Folder (name);
        Log.d("name", "name");
        new SaveFolderTask().execute(tobecreated);

    }


    private class SaveFolderTask extends AsyncTask<Folder, String, Boolean> {
        private boolean newFolder;

        @Override
        protected void onPreExecute() {
            //start
        }

        @Override
        protected Boolean doInBackground(final Folder... args) {
            Folder folder = args[0];
            long folderID = application.dataManager.insertFolder(folder);
            if(folderID > 0 ){
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {

        }

        @Override
        protected void onPostExecute(final Boolean b) {
            //start
        }
    }







}
