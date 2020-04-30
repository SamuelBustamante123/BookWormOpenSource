package com.totsp.bookworm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Toast;

import com.totsp.bookworm.data.DataManager;
import com.totsp.bookworm.model.Book;
import com.totsp.bookworm.model.Folder;
import com.totsp.bookworm.util.DateUtil;
import com.totsp.bookworm.util.StringUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class BookDetail extends Activity {

   private static final int MENU_EDIT = 0;
   private static final int MENU_WEB_GOOGLE = 1;
   private static final int MENU_WEB_AMAZON = 2;

   private BookWormApplication application;

   private ImageView bookCover;
   private TextView bookTitle;
   private TextView bookSubTitle;
   private TextView bookAuthors;
   private TextView bookSubject;
   private TextView bookDatePub;
   private TextView bookPublisher;
   private Spinner spinnerf;
   private Button updateFolder;

   private CheckBox readStatus;
   private RatingBar ratingBar;
   private EditText bookmark;

   private TextView bookDetailTitle;
   private EditText bookDetailNote;
   private Button bookDetailButton;

   @Override
   public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.bookdetail);
      application = (BookWormApplication) getApplication();

      bookCover = (ImageView) findViewById(R.id.bookcover);
      bookTitle = (TextView) findViewById(R.id.booktitle);
      bookSubTitle = (TextView) findViewById(R.id.booksubtitle);
      bookAuthors = (TextView) findViewById(R.id.bookauthors);
      bookSubject = (TextView) findViewById(R.id.booksubject);
      bookDatePub = (TextView) findViewById(R.id.bookdatepub);
      bookPublisher = (TextView) findViewById(R.id.bookpublisher);

      readStatus = (CheckBox) findViewById(R.id.bookreadstatus);
      ratingBar = (RatingBar) findViewById(R.id.bookrating);
      bookmark = (EditText) findViewById(R.id.bookmark);

      // detail slider


      bookDetailTitle = (TextView) findViewById(R.id.bookdetailtitle);
      bookDetailNote = (EditText) findViewById(R.id.bookdetailnote);
      bookDetailNote.setEnabled(true);

      bookDetailButton = (Button) findViewById(R.id.bookdetailbutton);
      bookDetailButton.setText(getString(R.string.btnEdit));
      bookDetailButton.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            if (bookDetailNote.isEnabled()) {
               if (bookDetailNote.getText() != null) {
                  Book book = application.selectedBook;
                  book.bookUserData.blurb = bookDetailNote.getText().toString();
                  application.dataManager.updateBook(book);
               }
               bookDetailNote.setEnabled(false);               
               bookDetailButton.setText(getString(R.string.btnEdit));
            } else {
               bookDetailNote.setEnabled(true);
               bookDetailButton.setText(getString(R.string.btnSave));
            }
         }
      });

      readStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         public void onCheckedChanged(final CompoundButton button, final boolean isChecked) {
            // TODO not sure why change listener fires when onCreate is init, but does
            saveReadStatusEdit();
         }
      });

      ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
         public void onRatingChanged(final RatingBar rb, final float val, final boolean b) {
            saveRatingEdit();
         }
      });

       setViewData();

      bookmark.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             //if (!bookmark.getText().toString().equals(""))
             //bookmark.setText("72");
                 //bookmark.setText(String.valueOf(Long.parseLong(bookmark.getText().toString())));
         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
             if (s.length() != 0) {
                 bookmark.removeTextChangedListener(this);
                 bookmark.setText(String.valueOf(Long.parseLong(bookmark.getText().toString())));
                 bookmark.setSelection(start + count + bookmark.getText().toString().length() - s.length());
                 bookmark.addTextChangedListener(this);
             }
         }

         @Override
         public void afterTextChanged(Editable s) {
             //if (!bookmark.getText().toString().equals(""))
                //bookmark.setText(String.valueOf(Long.parseLong(bookmark.getText().toString())));
             saveBookmarkEdit();
         }
      });


      spinnerf = (Spinner) findViewById(R.id.spinnerF);
      ArrayList<Folder> x = application.dataManager.selectAllFolders();
      ArrayList<String> fnames = new ArrayList<String>();

      for(int i = 0; i < x.size(); i++){
         fnames.add(x.get(i).name);
      }
      ArrayAdapter<String> dataAdapter = new ArrayAdapter <String>(this,
              android.R.layout.simple_spinner_item, fnames);
      dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinnerf.setAdapter(dataAdapter);
      updateFolder = (Button) findViewById((R.id.updateFolderButton));
      updateFolder.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            ArrayList<Folder> objects = application.dataManager.selectFolderByName(String.valueOf(spinnerf.getSelectedItem()));
            Log.d("Object in return = ", "---" + objects.get(0).name + " ID = " + objects.get(0).fid);
            Book sel = application.selectedBook;
            Log.d("Object in return = ", "---" + sel.title + " ID = " + sel.id);
            application.dataManager.updateBookFolder(sel, objects.get(0).fid);
         }
      });

   }

   @Override
   public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == 1)
      {
         Book book = application.selectedBook;
         if (book != null)
         {
            Bitmap coverImage = application.imageManager.retrieveBitmap(book.title, book.id, false);
            if (coverImage != null) {
               bookCover.setImageBitmap(coverImage);
            } else {
               bookCover.setImageResource(R.drawable.book_cover_missing);
            }
         }
      }
   }

   @Override
   public void onPause() {
      bookTitle = null;
      super.onPause();
   }

   // go back to Main on back from here
   @Override
   public boolean onKeyDown(final int keyCode, final KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
         startActivity(new Intent(BookDetail.this, Main.class));
         return true;
      }
      return super.onKeyDown(keyCode, event);
   }

   private void saveRatingEdit() {
      Book book = application.selectedBook;
      if (book != null) {
         book.bookUserData.rating = (Math.round(ratingBar.getRating()));
         application.dataManager.updateBook(book);
      }
   }

   private void saveReadStatusEdit() {
      Book book = application.selectedBook;
      if (book != null) {
         book.bookUserData.read = (readStatus.isChecked());
         application.dataManager.updateBook(book);
      }
   }

   private void saveBookmarkEdit() {
      Book book = application.selectedBook;
      if (book != null) {
         if (!bookmark.getText().toString().equals(""))
            book.bookUserData.bookmark = Long.parseLong(bookmark.getText().toString());
         else
             book.bookUserData.bookmark = 0;
         Log.d("Bookmark", "hey: " + book.bookUserData.bookmark);
         application.dataManager.updateBook(book);
      }
   }

   private void setViewData() {
      Book book = application.selectedBook;
      if (book != null) {
         if (application.debugEnabled) {
            Log.d(Constants.LOG_TAG, "BookDetail book present, will be displayed: " + book.toStringFull());
         }
         Bitmap coverImage = application.imageManager.retrieveBitmap(book.title, book.id, false);
         if (coverImage != null) {
            bookCover.setImageBitmap(coverImage);
         } else {
            bookCover.setImageResource(R.drawable.book_cover_missing);
         }

         bookTitle.setText(book.title);
         bookSubTitle.setText(book.subTitle);
         ratingBar.setRating(book.bookUserData.rating);
         readStatus.setChecked(book.bookUserData.read);
         bookmark.setText(String.valueOf(book.bookUserData.bookmark));
         Log.d("Bookmark", "YES " + String.valueOf(book.bookUserData.bookmark));
         bookDatePub.setText(DateUtil.format(new Date(book.datePubStamp)));
         bookAuthors.setText(StringUtil.contractAuthors(book.authors));

         bookDetailTitle.setText(book.title);
         bookDetailNote.setText(book.bookUserData.blurb);

         // we leave publisher and subject out of landscape layout         
         if (bookSubject != null) {
            bookSubject.setText(book.subject);
         }

         if (bookPublisher != null) {
            bookPublisher.setText(book.publisher);
         }
      }
   }

   @Override
   protected void onRestoreInstanceState(final Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      if (application.selectedBook == null) {
         Long id = savedInstanceState.getLong(Constants.BOOK_ID);
         if (id != null) {
            application.establishSelectedBook(id);
            if (application.selectedBook != null) {
               setViewData();
            } else {
               startActivity(new Intent(this, Main.class));
            }
         } else {
            startActivity(new Intent(this, Main.class));
         }
      }
   }

   @Override
   protected void onSaveInstanceState(final Bundle saveState) {
      if (application.selectedBook != null) {
         saveState.putLong(Constants.BOOK_ID, application.selectedBook.id);
      }
      super.onSaveInstanceState(saveState);
   }

   @Override
   public boolean onCreateOptionsMenu(final Menu menu) {
      menu.add(0, BookDetail.MENU_EDIT, 0, getString(R.string.menuEdit)).setIcon(android.R.drawable.ic_menu_edit);
      menu.add(0, BookDetail.MENU_WEB_GOOGLE, 1, null).setIcon(R.drawable.google);
      menu.add(0, BookDetail.MENU_WEB_AMAZON, 2, null).setIcon(R.drawable.amazon);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(final MenuItem item) {
      Uri uri = null;
      switch (item.getItemId()) {
         case MENU_EDIT:
            startActivityForResult(new Intent(this, BookForm.class), 1);
            return true;
         case MENU_WEB_GOOGLE:
            // TODO other Locales for GOOG URL?
            uri = Uri.parse("http://books.google.com/books?isbn=" + application.selectedBook.isbn10);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
         case MENU_WEB_AMAZON:
            uri =
                     Uri.parse("http://www.amazon.com/gp/search?keywords=" + application.selectedBook.isbn10
                              + "&index=books");
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}

