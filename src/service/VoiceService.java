package service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.LocalBroadcastManager;

public class VoiceService extends Service {

	private SpeechRecognizer speech = null;
	private Intent recognizerIntent;
	private AudioManager audioManager;

	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;
	protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));
	protected LocalBroadcastManager localBroadcastManager;

	public static final int MSG_RECOGNIZER_START_LISTENING = 1;
	public static final int MSG_RECOGNIZER_CANCEL          = 2;

	private ControllerBind controller = new ControllerBind();

	public class ControllerBind extends Binder {
		public VoiceService getService() {
			return VoiceService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return controller;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Message msg = new Message();
		msg.what = MSG_RECOGNIZER_START_LISTENING;

		try {
			mServerMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {

		super.onCreate();

		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		//remove beep
//		audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
//		audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
//		audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
//		audioManager.setStreamMute(AudioManager.STREAM_RING, true);
//		audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

		speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
		speech.setRecognitionListener(new SpeechRecognitionListener());

//		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
//		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
//		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

		localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		if (mIsCountDownOn) {
			mNoSpeechCountDown.cancel();
		}

		if (speech != null) {
			speech.destroy();
		}

	}

	protected class SpeechRecognitionListener implements RecognitionListener {

		@Override
		public void onReadyForSpeech(Bundle params) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				mIsCountDownOn = true;
				mNoSpeechCountDown.start();

				audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, -100, 0);
			}
		}

		@Override
		public void onBeginningOfSpeech() {
			if (mIsCountDownOn) {
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			
		}

		@Override
		public void onBufferReceived(byte[] buffer) {
			
		}

		@Override
		public void onEndOfSpeech() {
			
		}

		@Override
		public void onError(int error) {

			//Toast.makeText(VoiceService.this, getErrorText(error), Toast.LENGTH_SHORT).show();

			if (mIsCountDownOn) {
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}

			Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);

			try {
				mIsListening = false;
				mServerMessenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onResults(Bundle results) {

			ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			if (matches.get(0).equals("sair")) {
				System.exit(0);
				stopSelf();
			} else {
				localBroadcastManager.sendBroadcast(new Intent(matches.get(0)));
			}

			mIsListening = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);

			try {
				mServerMessenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			
		}
		
	}

	public static String getErrorText(int errorCode) {

		switch (errorCode) {
		case SpeechRecognizer.ERROR_AUDIO:
			return "Erro de gravação de áudio.";
		
		case SpeechRecognizer.ERROR_CLIENT:
			return "Outros erros do lado cliente.";

		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			return "Permissões insuficientes.";

		case SpeechRecognizer.ERROR_NETWORK:
			return "Outros erros relacionados à rede.";

		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			return "Operação de rede expirou.";

		case SpeechRecognizer.ERROR_NO_MATCH:
			return "Nenhum resultado encontrado.";

		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			return "Serviço de reconhecimento indisponível.";

		case SpeechRecognizer.ERROR_SERVER:
			return "Servidor envia estado de erro.";

		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			return "Nenhuma entrada de fala.";

		default:
			return "Reconhecimento falhou, por favor, tente novamente";
		}

	}

	protected static class IncomingHandler extends Handler {

		private WeakReference<VoiceService> mTarget;

		public IncomingHandler(VoiceService target) {
			mTarget = new WeakReference<VoiceService>(target);
		}

		@Override
		public void handleMessage(Message msg) {

			final VoiceService target = mTarget.get();

			switch (msg.what) {
			case MSG_RECOGNIZER_START_LISTENING:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					target.audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, -100, 0);
				}

				if (!target.mIsListening) {
					target.speech = SpeechRecognizer.createSpeechRecognizer(target.getBaseContext());
					target.speech.setRecognitionListener(target.new SpeechRecognitionListener());

					target.recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					target.recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
					target.recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, target.getPackageName());
					target.recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					target.recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

					target.speech.startListening(target.recognizerIntent);
					target.mIsListening = true;
				}

				break;

			case MSG_RECOGNIZER_CANCEL:
				target.speech.cancel();
				target.mIsListening = false;

				break;

			default:
				break;
			}

		}

	}

	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {
		
		@Override
		public void onTick(long millisUntilFinished) {
			
		}
		
		@Override
		public void onFinish() {
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);

			try {
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

}
