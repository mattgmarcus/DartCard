package edu.dartmouth.cs.dartcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

public class DartCardDialogFragment extends DialogFragment {

	int dialogId = Globals.DIALOG_KEY_DEFAULT;
	DialogExitListener listener;

	public static DartCardDialogFragment newInstance(int dialogId) {
		DartCardDialogFragment frag = new DartCardDialogFragment();
		Bundle args = new Bundle();
		args.putInt(Globals.DIALOG_ID_KEY, dialogId);
		frag.setArguments(args);
		return frag;
	}

	public interface DialogExitListener {
		void onSavePhotoExit(boolean savePhoto);
		void onTrySaveAgainExit(boolean tryAgain);
		void onReturn();
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialogId = getArguments().getInt(Globals.DIALOG_ID_KEY);
		
		final Activity parent = getActivity();
		
		listener = (DialogExitListener) parent;

		AlertDialog.Builder builder = new AlertDialog.Builder(parent);

		switch (dialogId) {
		case Globals.DIALOG_KEY_SAVE_PHOTO:
			builder.setTitle(R.string.dialog_title_save_photo);
			builder.setPositiveButton(R.string.button_save_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onSavePhotoExit(true);
						}
					});
			builder.setNegativeButton(R.string.button_no_save_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onSavePhotoExit(false);
						}
					});
			break;
			
		case Globals.DIALOG_KEY_TRY_SAVE_AGAIN:
			builder.setTitle(R.string.dialog_title_try_save_again);
			builder.setPositiveButton(R.string.button_try_again_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onTrySaveAgainExit(true);
						}
					});
			builder.setNegativeButton(R.string.button_cancel_title,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onTrySaveAgainExit(false);
						}
					});
			break;
			
		case Globals.DIALOG_RECIPIENT_ERRORS:
			builder.setTitle(R.string.dialog_title_recipient_error);
			builder.setNeutralButton(R.string.dialog_button_recipient_error,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//Do nothing
						}
					});
			break;
			
		case Globals.DIALOG_PHOTO_MAP_ERROR:
			builder.setTitle(R.string.dialog_title_photo_map_error);
			builder.setMessage(R.string.dialog_message_photo_map_error);
			builder.setNeutralButton(R.string.dialog_button_photo_map_error,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onReturn();
						}
					});
			break;
			
		case Globals.DIALOG_LOB_ERRORS:
			builder.setTitle(R.string.dialog_title_lob_error);
			builder.setNeutralButton(R.string.dialog_button_photo_map_error,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onReturn();
						}
					});
			break;
			
		case Globals.DIALOG_MESSAGE_ERRORS:
			builder.setTitle(R.string.dialog_title_message_error);
			builder.setMessage(R.string.dialog_text_message_error);
			builder.setNeutralButton(R.string.dialog_button_photo_map_error,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							listener.onReturn();
						}
					});
			break;

		default:
			break;
		}
		return builder.create();

	}
}
