package com.totsp.bookworm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.totsp.bookworm.data.CsvManager;
import com.totsp.bookworm.data.DataConstants;
import com.totsp.bookworm.model.Book;
import com.totsp.bookworm.util.BookUtil;
import com.totsp.bookworm.util.ExternalStorageUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;

public class CsvImport extends Activity {

   private BookWormApplication application;

   private ArrayList<Book> books;
   private ListView listView;
   private BookListAdapter adapter;

   //private Button parseButton;
   private Button importButton;
   private Button helpButton;

   private TextView importMeta;

   private ProgressDialog progressDialogHorizontal;
   private ProgressDialog progressDialogSpinner;

   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.csvimport);
      application = (BookWormApplication) getApplication();

      progressDialogHorizontal = new ProgressDialog(this);
      progressDialogHorizontal.setCancelable(false);
      progressDialogHorizontal.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      
      progressDialogSpinner = new ProgressDialog(this);
      progressDialogSpinner.setCancelable(false);
      progressDialogSpinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);

      //parseButton = (Button) findViewById(R.id.bookimportparsebutton);
      importButton = (Button) findViewById(R.id.bookimportimportbutton);
//      importButton.setEnabled(false);

      helpButton = (Button) findViewById(R.id.bookimporthelpbutton);

      importMeta = (TextView) findViewById(R.id.bookimportmeta);

      books = new ArrayList<Book>();

      listView = (ListView) findViewById(R.id.bookimportlistview);
      //listView.setEmptyView(findViewById(R.id.bookimportlistviewempty));
      adapter = new BookListAdapter(this, books);
      listView.setAdapter(adapter);

      if (!ExternalStorageUtil.isExternalStorageAvail()) {
         Toast.makeText(this, getString(R.string.msgExternalStorageNAError), Toast.LENGTH_LONG).show();
         importButton.setEnabled(false);
      }

      importButton.setOnClickListener(new OnClickListener() {
         @SuppressWarnings("unchecked")
         public void onClick(View v) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("text/*");
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(chooseFile, 10);
         }
      });

      helpButton.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            new AlertDialog.Builder(CsvImport.this).setTitle(getString(R.string.menuCsvHelp)).setMessage(
                     Html.fromHtml(getString(R.string.msgCsvHelp))).setNeutralButton(getString(R.string.btnDismiss),
                     new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface d, final int i) {
                        }
                     }).show();
         }
      });

   }

   public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == 10 && resultCode == RESULT_OK)
      {
         Uri uri = data.getData();
         InputStream in = null;
         OutputStream out = null;
         File f = new File(DataConstants.EXTERNAL_DATA_PATH + File.separator + "/tempImport.csv");
         try {
            in = getContentResolver().openInputStream(uri);
            out = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
               out.write(buffer, 0, len);
            }
         } catch (Exception e)
         {
            Log.e("CsvImport", "Error loading file");
         }
         finally {
            if (in != null)
            {
               try {
                  in.close();
               } catch(IOException e)
               {
                  Log.e("CsvImport", e.getMessage());
               }
            }
            if (out != null) {
               try {
                  out.close();
               } catch(IOException e)
               {
                  Log.e("CsvImport", e.getMessage());
               }
            }
         }
         if ((f == null) || !f.exists() || !f.canRead()) {
            Toast.makeText(CsvImport.this, "File " + f.getPath() + " not found, cannot import.", Toast.LENGTH_LONG).show();
         }
         else
            new ParseTask().execute(f);
      }
   }

   @Override
   public void onPause() {
      if (progressDialogHorizontal.isShowing()) {
         progressDialogHorizontal.dismiss();
      }
      if (progressDialogSpinner.isShowing()) {
         progressDialogSpinner.dismiss();
      }
      super.onPause();
   }

   private void reset() {
      books.clear();
      adapter.clear();
      importMeta.setText("");
   }

   //
   // AsyncTasks
   //
   private class ParseTask extends AsyncTask<File, Void, ArrayList<Book>> {
      @Override
      protected void onPreExecute() {
         if (progressDialogSpinner.isShowing()) {
            progressDialogSpinner.dismiss();
         }
         // keep screen on, and prevent orientation change, during potentially long running task
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
      }

      @Override
      protected ArrayList<Book> doInBackground(final File... args) {
         publishProgress(new Void[] {});
         ArrayList<Book> books = CsvManager.parseCSVFile(application.bookDataSource, args[0]);
         args[0].delete();
         return books;
      }

      @Override
      protected void onProgressUpdate(Void... progress) {
         progressDialogSpinner.setMessage(getString(R.string.msgParsingCSVFile));
         progressDialogSpinner.setMax(1);
         progressDialogSpinner.show();
      }

      @Override
      protected void onPostExecute(final ArrayList<Book> books) {
         if (progressDialogSpinner.isShowing()) {
            progressDialogSpinner.dismiss();
         }

         if (books == null || books.isEmpty()) {
            Toast.makeText(CsvImport.this, getString(R.string.msgCsvUnableToParse), Toast.LENGTH_LONG).show();
            reset();
         } else {
            importButton.setEnabled(true);
            // TODO notifyDataSetChanged doesn't work here, again, something must be up with way I am using ListView
            //adapter.notifyDataSetChanged();
            adapter.clear();
            for (Book b : books) {
               adapter.add(b);
            }
            importMeta.setText(String.format(getString(R.string.msgCsvMeta), new Object[] { books.size() }));
            new ImportTask().execute(books);
         }
      }
   }

   private class ImportTask extends AsyncTask<ArrayList<Book>, String, Void> {
      @Override
      protected void onPreExecute() {
         if (progressDialogHorizontal.isShowing()) {
            progressDialogHorizontal.dismiss();
         }
         // keep screen on, and prevent orientation change, during potentially long running task
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
      }

      @Override
      protected Void doInBackground(final ArrayList<Book>... args) {
         ArrayList<Book> taskBooks = args[0];
         String[] progress = new String[3];
         progress[2] = Integer.toString(taskBooks.size());
                  
         for (int i = 0; i < taskBooks.size(); i++) {
            Book b = taskBooks.get(i);            
            boolean dupe = false;
            ArrayList<Book> potentialDupes = application.dataManager.selectAllBooksByTitle(b.title);
            if (potentialDupes != null) {
               // very poor algorithm to check dupes here, just re-linear search
               for (int j = 0; j < potentialDupes.size(); j++) {
                  Book b2 = potentialDupes.get(j);
                  if (BookUtil.areBooksEffectiveDupes(b, b2)) {
                     dupe = true;
                     break;
                  }
               }
            }
            if (dupe) {
               Log.i(Constants.LOG_TAG, "NOT Importing book: " + b.title + " because it appears to be a duplicate.");
               progress[0] = String.format(getString(R.string.msgCsvSkippingBook, b.title));
               progress[1] = Integer.toString(i);
               publishProgress(progress);
               // sleep because loop is too fast to see messages
               SystemClock.sleep(500);
            } else {
               Log.i(Constants.LOG_TAG, "Importing book: " + b.title);
               progress[0] = String.format(getString(R.string.msgCsvImportingBook, b.title));
               progress[1] = Integer.toString(i);
               publishProgress(progress);
               b.id = application.dataManager.insertBook(b);
               application.imageManager.resetCoverImage(b);
            }
         }
         return null;
      }

      @Override
      protected void onProgressUpdate(String... progress) {
         progressDialogHorizontal.setMessage(progress[0]);
         if ((progress[1].equals("1")) && !progressDialogHorizontal.isShowing()) {            
            progressDialogHorizontal.setMax(Integer.valueOf(progress[2]));
            progressDialogHorizontal.show();
         } else if (progress[1].equals(progress[2]) && progressDialogHorizontal.isShowing()) {
            progressDialogHorizontal.dismiss();
         }
         progressDialogHorizontal.setProgress(Integer.valueOf(progress[1]));
      }

      @Override
      protected void onPostExecute(final Void arg) {
         if (progressDialogHorizontal.isShowing()) {
            progressDialogHorizontal.dismiss();
         }

         reset();

         // reset screen and orientation params
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

         startActivity(new Intent(CsvImport.this, Main.class));
      }
   }
}